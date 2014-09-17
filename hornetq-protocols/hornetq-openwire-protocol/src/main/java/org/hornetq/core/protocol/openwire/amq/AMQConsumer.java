/*
 * Copyright 2005-2014 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.hornetq.core.protocol.openwire.amq;

import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.MessageAck;
import org.apache.activemq.command.MessageDispatch;
import org.apache.activemq.command.MessageId;
import org.apache.activemq.wireformat.WireFormat;
import org.hornetq.api.core.SimpleString;
import org.hornetq.core.protocol.openwire.OpenWireMessageConverter;
import org.hornetq.core.protocol.openwire.OpenWireUtil;
import org.hornetq.core.server.QueueQueryResult;
import org.hornetq.core.server.ServerMessage;
import org.hornetq.jms.client.HornetQDestination;

public class AMQConsumer implements BrowserListener
{
   private AMQSession session;
   private ActiveMQDestination actualDest;
   private ConsumerInfo info;
   private long nativeId = -1;
   private SimpleString subQueueName = null;

   private final int prefetchSize;
   private AtomicInteger currentSize;
   private final java.util.Queue<MessageInfo> deliveringRefs = new ConcurrentLinkedQueue<MessageInfo>();

   public AMQConsumer(AMQSession amqSession, ActiveMQDestination d, ConsumerInfo info)
   {
      this.session = amqSession;
      this.actualDest = d;
      this.info = info;
      this.prefetchSize = info.getPrefetchSize();
      this.currentSize = new AtomicInteger(0);
   }

   public void init() throws Exception
   {
      AMQServerSession coreSession = session.getCoreSession();

      SimpleString selector = info.getSelector() == null ? null : new SimpleString(info.getSelector());

      nativeId = session.getCoreServer().getStorageManager().generateUniqueID();

      SimpleString address = new SimpleString(this.actualDest.getPhysicalName());

      if (this.actualDest.isTopic())
      {
         String physicalName = this.actualDest.getPhysicalName();
         if (physicalName.contains(".>"))
         {
            //wildcard
            physicalName = OpenWireUtil.convertWildcard(physicalName);
         }

         // on recreate we don't need to create queues
         address = new SimpleString("jms.topic." + physicalName);
         if (info.isDurable())
         {
            subQueueName = new SimpleString(
                  HornetQDestination.createQueueNameForDurableSubscription(
                        true, info.getClientId(), info.getSubscriptionName()));

            QueueQueryResult result = coreSession.executeQueueQuery(subQueueName);
            if (result.isExists())
            {
               // Already exists
               if (result.getConsumerCount() > 0)
               {
                  throw new IllegalStateException(
                        "Cannot create a subscriber on the durable subscription since it already has subscriber(s)");
               }

               SimpleString oldFilterString = result.getFilterString();

               boolean selectorChanged = selector == null
                     && oldFilterString != null || oldFilterString == null
                     && selector != null || oldFilterString != null
                     && selector != null && !oldFilterString.equals(selector);

               SimpleString oldTopicName = result.getAddress();

               boolean topicChanged = !oldTopicName.equals(address);

               if (selectorChanged || topicChanged)
               {
                  // Delete the old durable sub
                  coreSession.deleteQueue(subQueueName);

                  // Create the new one
                  coreSession.createQueue(address, subQueueName, selector,
                        false, true);
               }

            }
            else
            {
               coreSession.createQueue(address, subQueueName, selector, false,
                     true);
            }
         }
         else
         {
            subQueueName = new SimpleString(UUID.randomUUID().toString());

            coreSession.createQueue(address, subQueueName, selector, true, false);
         }

         coreSession.createConsumer(nativeId, subQueueName, null, info.isBrowser(), false, Integer.MAX_VALUE);
      }
      else
      {
         SimpleString queueName = new SimpleString("jms.queue." + this.actualDest.getPhysicalName());
         coreSession.createConsumer(nativeId, queueName, selector, info.isBrowser(), false, Integer.MAX_VALUE);
      }

      if (info.isBrowser())
      {
         AMQServerConsumer coreConsumer = coreSession.getConsumer(nativeId);
         coreConsumer.setBrowserListener(this);
      }

   }

   public long getNativeId()
   {
      return this.nativeId;
   }

   public ConsumerId getId()
   {
      return info.getConsumerId();
   }

   public WireFormat getMarshaller()
   {
      return this.session.getMarshaller();
   }

   public void acquireCredit(int n) throws Exception
   {
      this.currentSize.addAndGet(-n);
      if (currentSize.get() < prefetchSize)
      {
         AtomicInteger credits = session.getCoreSession().getConsumerCredits(nativeId);
         credits.set(0);
         session.getCoreSession().receiveConsumerCredits(nativeId, Integer.MAX_VALUE);
      }
   }

   public void checkCreditOnDelivery() throws Exception
   {
      this.currentSize.incrementAndGet();

      if (currentSize.get() == prefetchSize)
      {
         //stop because reach prefetchSize
         session.getCoreSession().receiveConsumerCredits(nativeId, 0);
      }
   }

   public int handleDeliver(ServerMessage message, int deliveryCount)
   {
      MessageDispatch dispatch;
      try
      {
         //decrement deliveryCount as AMQ client tends to add 1.
         dispatch = OpenWireMessageConverter.createMessageDispatch(message, deliveryCount - 1, this);
         int size = dispatch.getMessage().getSize();
         this.deliveringRefs.add(new MessageInfo(dispatch.getMessage().getMessageId(), message.getMessageID(), size));
         session.deliverMessage(dispatch);
         checkCreditOnDelivery();
         return size;
      }
      catch (IOException e)
      {
         return 0;
      }
      catch (Throwable t)
      {
         return 0;
      }
   }

   public void acknowledge(MessageAck ack) throws Exception
   {
      MessageId last = ack.getLastMessageId();
      MessageInfo mi;
      int n = 0;

      byte ackType = ack.getAckType();

      if (ackType == MessageAck.INDIVIDUAL_ACK_TYPE)
      {
         n = 1;
         Iterator<MessageInfo> iter = deliveringRefs.iterator();
         while (iter.hasNext())
         {
            mi = iter.next();
            if (mi.amqId.equals(last))
            {
               iter.remove();
               session.getCoreSession().individualAcknowledge(nativeId, mi.nativeId);
               session.getCoreSession().commit();
               break;
            }
         }
      }
      else if (ackType == MessageAck.REDELIVERED_ACK_TYPE)
      {
         //client tells that this message is for redlivery.
         //do nothing until poisoned.
      }
      else if (ackType == MessageAck.POSION_ACK_TYPE)
      {
         //send to dlq
         n = 1;
         Iterator<MessageInfo> iter = deliveringRefs.iterator();
         while (iter.hasNext())
         {
            mi = iter.next();
            if (mi.amqId.equals(last))
            {
               iter.remove();
               session.getCoreSession().moveToDeadLetterAddress(nativeId, mi.nativeId, ack.getPoisonCause());
               session.getCoreSession().commit();
               break;
            }
         }
      }
      else
      {
         do
         {
            mi = deliveringRefs.poll();

            if (mi == null)
            {
               throw new IllegalStateException("No messages in the list for ack " + last);
            }
            n++;
         }
         while (!mi.amqId.equals(last));

         session.getCoreSession().acknowledge(nativeId, mi.nativeId);
      }

      acquireCredit(n);
   }

   @Override
   public void browseFinished()
   {
      MessageDispatch md = new MessageDispatch();
      md.setConsumerId(info.getConsumerId());
      md.setMessage(null);
      md.setDestination(null);

      session.deliverMessage(md);
   }

   public boolean handledTransactionalMsg()
   {
      // TODO Auto-generated method stub
      return false;
   }
}
