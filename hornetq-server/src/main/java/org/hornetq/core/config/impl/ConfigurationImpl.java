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
package org.hornetq.core.config.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hornetq.api.config.HornetQDefaultConfiguration;
import org.hornetq.api.core.BroadcastGroupConfiguration;
import org.hornetq.api.core.DiscoveryGroupConfiguration;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.BridgeConfiguration;
import org.hornetq.core.config.ClusterConnectionConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.ConnectorServiceConfiguration;
import org.hornetq.core.config.CoreQueueConfiguration;
import org.hornetq.core.config.DivertConfiguration;
import org.hornetq.core.config.HAPolicyConfiguration;
import org.hornetq.core.config.ha.ReplicaPolicyConfiguration;
import org.hornetq.core.config.ha.ReplicatedPolicyConfiguration;
import org.hornetq.core.config.ha.SharedStoreMasterPolicyConfiguration;
import org.hornetq.core.config.ha.SharedStoreSlavePolicyConfiguration;
import org.hornetq.core.security.Role;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.group.impl.GroupingHandlerConfiguration;
import org.hornetq.core.settings.impl.AddressSettings;

/**
 * @author <a href="mailto:ataylor@redhat.com>Andy Taylor</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 */
public class ConfigurationImpl implements Configuration
{
   // Constants ------------------------------------------------------------------------------

   public static final JournalType DEFAULT_JOURNAL_TYPE = JournalType.ASYNCIO;

   private static final long serialVersionUID = 4077088945050267843L;

   // Attributes -----------------------------------------------------------------------------

   private String name = "ConfigurationImpl::" + System.identityHashCode(this);

   protected boolean fileDeploymentEnabled = HornetQDefaultConfiguration.isDefaultFileDeploymentEnabled();

   private boolean persistenceEnabled = HornetQDefaultConfiguration.isDefaultPersistenceEnabled();

   protected long fileDeploymentScanPeriod = HornetQDefaultConfiguration.getDefaultFileDeployerScanPeriod();

   private boolean persistDeliveryCountBeforeDelivery =
            HornetQDefaultConfiguration.isDefaultPersistDeliveryCountBeforeDelivery();

   private int scheduledThreadPoolMaxSize = HornetQDefaultConfiguration.getDefaultScheduledThreadPoolMaxSize();

   private int threadPoolMaxSize = HornetQDefaultConfiguration.getDefaultThreadPoolMaxSize();

   private long securityInvalidationInterval = HornetQDefaultConfiguration.getDefaultSecurityInvalidationInterval();

   private boolean securityEnabled = HornetQDefaultConfiguration.isDefaultSecurityEnabled();

   protected boolean jmxManagementEnabled = HornetQDefaultConfiguration.isDefaultJmxManagementEnabled();

   protected String jmxDomain = HornetQDefaultConfiguration.getDefaultJmxDomain();

   protected long connectionTTLOverride = HornetQDefaultConfiguration.getDefaultConnectionTtlOverride();

   protected boolean asyncConnectionExecutionEnabled = HornetQDefaultConfiguration.isDefaultAsyncConnectionExecutionEnabled();

   private long messageExpiryScanPeriod = HornetQDefaultConfiguration.getDefaultMessageExpiryScanPeriod();

   private int messageExpiryThreadPriority = HornetQDefaultConfiguration.getDefaultMessageExpiryThreadPriority();

   protected int idCacheSize = HornetQDefaultConfiguration.getDefaultIdCacheSize();

   private boolean persistIDCache = HornetQDefaultConfiguration.isDefaultPersistIdCache();

   private List<String> incomingInterceptorClassNames = new ArrayList<String>();

   private List<String> outgoingInterceptorClassNames = new ArrayList<String>();

   protected Map<String, TransportConfiguration> connectorConfigs = new HashMap<String, TransportConfiguration>();

   private Set<TransportConfiguration> acceptorConfigs = new HashSet<TransportConfiguration>();

   protected List<BridgeConfiguration> bridgeConfigurations = new ArrayList<BridgeConfiguration>();

   protected List<DivertConfiguration> divertConfigurations = new ArrayList<DivertConfiguration>();

   protected List<ClusterConnectionConfiguration> clusterConfigurations = new ArrayList<ClusterConnectionConfiguration>();

   private List<CoreQueueConfiguration> queueConfigurations = new ArrayList<CoreQueueConfiguration>();

   protected List<BroadcastGroupConfiguration> broadcastGroupConfigurations = new ArrayList<BroadcastGroupConfiguration>();

   protected Map<String, DiscoveryGroupConfiguration> discoveryGroupConfigurations = new LinkedHashMap<String, DiscoveryGroupConfiguration>();

   // Paging related attributes ------------------------------------------------------------

   private String pagingDirectory = HornetQDefaultConfiguration.getDefaultPagingDir();

   // File related attributes -----------------------------------------------------------

   private int maxConcurrentPageIO = HornetQDefaultConfiguration.getDefaultMaxConcurrentPageIo();

   protected String largeMessagesDirectory = HornetQDefaultConfiguration.getDefaultLargeMessagesDir();

   protected String bindingsDirectory = HornetQDefaultConfiguration.getDefaultBindingsDirectory();

   protected boolean createBindingsDir = HornetQDefaultConfiguration.isDefaultCreateBindingsDir();

   protected String journalDirectory = HornetQDefaultConfiguration.getDefaultJournalDir();

   protected boolean createJournalDir = HornetQDefaultConfiguration.isDefaultCreateJournalDir();

   public JournalType journalType = ConfigurationImpl.DEFAULT_JOURNAL_TYPE;

   protected boolean journalSyncTransactional = HornetQDefaultConfiguration.isDefaultJournalSyncTransactional();

   protected boolean journalSyncNonTransactional = HornetQDefaultConfiguration.isDefaultJournalSyncNonTransactional();

   protected int journalCompactMinFiles = HornetQDefaultConfiguration.getDefaultJournalCompactMinFiles();

   protected int journalCompactPercentage = HornetQDefaultConfiguration.getDefaultJournalCompactPercentage();

   protected int journalFileSize = HornetQDefaultConfiguration.getDefaultJournalFileSize();

   protected int journalMinFiles = HornetQDefaultConfiguration.getDefaultJournalMinFiles();

   // AIO and NIO need different values for these attributes

   protected int journalMaxIO_AIO = HornetQDefaultConfiguration.getDefaultJournalMaxIoAio();

   protected int journalBufferTimeout_AIO = HornetQDefaultConfiguration.getDefaultJournalBufferTimeoutAio();

   protected int journalBufferSize_AIO = HornetQDefaultConfiguration.getDefaultJournalBufferSizeAio();

   protected int journalMaxIO_NIO = HornetQDefaultConfiguration.getDefaultJournalMaxIoNio();

   protected int journalBufferTimeout_NIO = HornetQDefaultConfiguration.getDefaultJournalBufferTimeoutNio();

   protected int journalBufferSize_NIO = HornetQDefaultConfiguration.getDefaultJournalBufferSizeNio();

   protected boolean logJournalWriteRate = HornetQDefaultConfiguration.isDefaultJournalLogWriteRate();

   protected int journalPerfBlastPages = HornetQDefaultConfiguration.getDefaultJournalPerfBlastPages();

   protected boolean runSyncSpeedTest = HornetQDefaultConfiguration.isDefaultRunSyncSpeedTest();

   private boolean wildcardRoutingEnabled = HornetQDefaultConfiguration.isDefaultWildcardRoutingEnabled();

   private boolean messageCounterEnabled = HornetQDefaultConfiguration.isDefaultMessageCounterEnabled();

   private long messageCounterSamplePeriod = HornetQDefaultConfiguration.getDefaultMessageCounterSamplePeriod();

   private int messageCounterMaxDayHistory = HornetQDefaultConfiguration.getDefaultMessageCounterMaxDayHistory();

   private long transactionTimeout = HornetQDefaultConfiguration.getDefaultTransactionTimeout();

   private long transactionTimeoutScanPeriod = HornetQDefaultConfiguration.getDefaultTransactionTimeoutScanPeriod();

   private SimpleString managementAddress = HornetQDefaultConfiguration.getDefaultManagementAddress();

   private SimpleString managementNotificationAddress = HornetQDefaultConfiguration.getDefaultManagementNotificationAddress();

   protected String clusterUser = HornetQDefaultConfiguration.getDefaultClusterUser();

   protected String clusterPassword = HornetQDefaultConfiguration.getDefaultClusterPassword();

   private long serverDumpInterval = HornetQDefaultConfiguration.getDefaultServerDumpInterval();

   protected boolean failoverOnServerShutdown = HornetQDefaultConfiguration.isDefaultFailoverOnServerShutdown();

   // percentage of free memory which triggers warning from the memory manager
   private int memoryWarningThreshold = HornetQDefaultConfiguration.getDefaultMemoryWarningThreshold();

   private long memoryMeasureInterval = HornetQDefaultConfiguration.getDefaultMemoryMeasureInterval();

   protected GroupingHandlerConfiguration groupingHandlerConfiguration;

   private Map<String, AddressSettings> addressesSettings = new HashMap<String, AddressSettings>();

   private Map<String, Set<Role>> securitySettings = new HashMap<String, Set<Role>>();

   protected List<ConnectorServiceConfiguration> connectorServiceConfigurations = new ArrayList<ConnectorServiceConfiguration>();

   private boolean maskPassword = HornetQDefaultConfiguration.isDefaultMaskPassword();

   private transient String passwordCodec;

   private boolean resolveProtocols = HornetQDefaultConfiguration.isDefaultResolveProtocols();

   private long journalLockAcquisitionTimeout = HornetQDefaultConfiguration.getDefaultJournalLockAcquisitionTimeout();

   private HAPolicyConfiguration haPolicyConfiguration;

   // Public -------------------------------------------------------------------------

   public boolean isClustered()
   {
      return !getClusterConfigurations().isEmpty();
   }

   public boolean isFileDeploymentEnabled()
   {
      return fileDeploymentEnabled;
   }

   public ConfigurationImpl setFileDeploymentEnabled(final boolean enable)
   {
      fileDeploymentEnabled = enable;
      return this;
   }

   public boolean isPersistenceEnabled()
   {
      return persistenceEnabled;
   }

   public ConfigurationImpl setPersistenceEnabled(final boolean enable)
   {
      persistenceEnabled = enable;
      return this;
   }

   public long getFileDeployerScanPeriod()
   {
      return fileDeploymentScanPeriod;
   }

   public ConfigurationImpl setFileDeployerScanPeriod(final long period)
   {
      fileDeploymentScanPeriod = period;
      return this;
   }

   /**
    * @return the persistDeliveryCountBeforeDelivery
    */
   public boolean isPersistDeliveryCountBeforeDelivery()
   {
      return persistDeliveryCountBeforeDelivery;
   }

   public ConfigurationImpl setPersistDeliveryCountBeforeDelivery(final boolean persistDeliveryCountBeforeDelivery)
   {
      this.persistDeliveryCountBeforeDelivery = persistDeliveryCountBeforeDelivery;
      return this;
   }

   public int getScheduledThreadPoolMaxSize()
   {
      return scheduledThreadPoolMaxSize;
   }

   public ConfigurationImpl setScheduledThreadPoolMaxSize(final int maxSize)
   {
      scheduledThreadPoolMaxSize = maxSize;
      return this;
   }

   public int getThreadPoolMaxSize()
   {
      return threadPoolMaxSize;
   }

   public ConfigurationImpl setThreadPoolMaxSize(final int maxSize)
   {
      threadPoolMaxSize = maxSize;
      return this;
   }

   public long getSecurityInvalidationInterval()
   {
      return securityInvalidationInterval;
   }

   public ConfigurationImpl setSecurityInvalidationInterval(final long interval)
   {
      securityInvalidationInterval = interval;
      return this;
   }

   public long getConnectionTTLOverride()
   {
      return connectionTTLOverride;
   }

   public ConfigurationImpl setConnectionTTLOverride(final long ttl)
   {
      connectionTTLOverride = ttl;
      return this;
   }

   public boolean isAsyncConnectionExecutionEnabled()
   {
      return asyncConnectionExecutionEnabled;
   }

   public ConfigurationImpl setEnabledAsyncConnectionExecution(final boolean enabled)
   {
      asyncConnectionExecutionEnabled = enabled;
      return this;
   }

   @Deprecated
   @Override
   public List<String> getInterceptorClassNames()
   {
      return getIncomingInterceptorClassNames();
   }

   @Deprecated
   @Override
   public ConfigurationImpl setInterceptorClassNames(final List<String> interceptors)
   {
      setIncomingInterceptorClassNames(interceptors);
      return this;
   }

   public List<String> getIncomingInterceptorClassNames()
   {
      return incomingInterceptorClassNames;
   }

   public ConfigurationImpl setIncomingInterceptorClassNames(final List<String> interceptors)
   {
      incomingInterceptorClassNames = interceptors;
      return this;
   }

   public List<String> getOutgoingInterceptorClassNames()
   {
      return outgoingInterceptorClassNames;
   }

   public ConfigurationImpl setOutgoingInterceptorClassNames(final List<String> interceptors)
   {
      outgoingInterceptorClassNames = interceptors;
      return this;
   }

   public Set<TransportConfiguration> getAcceptorConfigurations()
   {
      return acceptorConfigs;
   }

   public ConfigurationImpl setAcceptorConfigurations(final Set<TransportConfiguration> infos)
   {
      acceptorConfigs = infos;
      return this;
   }

   public ConfigurationImpl addAcceptorConfiguration(final TransportConfiguration infos)
   {
      acceptorConfigs.add(infos);
      return this;
   }

   public ConfigurationImpl clearAcceptorConfigurations()
   {
      acceptorConfigs.clear();
      return this;
   }

   public Map<String, TransportConfiguration> getConnectorConfigurations()
   {
      return connectorConfigs;
   }

   public ConfigurationImpl setConnectorConfigurations(final Map<String, TransportConfiguration> infos)
   {
      connectorConfigs = infos;
      return this;
   }

   public ConfigurationImpl addConnectorConfiguration(final String key, final TransportConfiguration info)
   {
      connectorConfigs.put(key, info);
      return this;
   }

   public GroupingHandlerConfiguration getGroupingHandlerConfiguration()
   {
      return groupingHandlerConfiguration;
   }

   public ConfigurationImpl setGroupingHandlerConfiguration(final GroupingHandlerConfiguration groupingHandlerConfiguration)
   {
      this.groupingHandlerConfiguration = groupingHandlerConfiguration;
      return this;
   }

   public List<BridgeConfiguration> getBridgeConfigurations()
   {
      return bridgeConfigurations;
   }

   public ConfigurationImpl setBridgeConfigurations(final List<BridgeConfiguration> configs)
   {
      bridgeConfigurations = configs;
      return this;
   }

   public ConfigurationImpl addBridgeConfiguration(final BridgeConfiguration config)
   {
      bridgeConfigurations.add(config);
      return this;
   }

   public List<BroadcastGroupConfiguration> getBroadcastGroupConfigurations()
   {
      return broadcastGroupConfigurations;
   }

   public ConfigurationImpl setBroadcastGroupConfigurations(final List<BroadcastGroupConfiguration> configs)
   {
      broadcastGroupConfigurations = configs;
      return this;
   }

   public ConfigurationImpl addBroadcastGroupConfiguration(final BroadcastGroupConfiguration config)
   {
      broadcastGroupConfigurations.add(config);
      return this;
   }

   public List<ClusterConnectionConfiguration> getClusterConfigurations()
   {
      return clusterConfigurations;
   }

   public ConfigurationImpl setClusterConfigurations(final List<ClusterConnectionConfiguration> configs)
   {
      clusterConfigurations = configs;
      return this;
   }

   public ConfigurationImpl addClusterConfiguration(final ClusterConnectionConfiguration config)
   {
      clusterConfigurations.add(config);
      return this;
   }

   public ConfigurationImpl clearClusterConfigurations()
   {
      clusterConfigurations.clear();
      return this;
   }

   public List<DivertConfiguration> getDivertConfigurations()
   {
      return divertConfigurations;
   }

   public ConfigurationImpl setDivertConfigurations(final List<DivertConfiguration> configs)
   {
      divertConfigurations = configs;
      return this;
   }

   public ConfigurationImpl addDivertConfiguration(final DivertConfiguration config)
   {
      divertConfigurations.add(config);
      return this;
   }

   public List<CoreQueueConfiguration> getQueueConfigurations()
   {
      return queueConfigurations;
   }

   public ConfigurationImpl setQueueConfigurations(final List<CoreQueueConfiguration> configs)
   {
      queueConfigurations = configs;
      return this;
   }

   public ConfigurationImpl addQueueConfiguration(final CoreQueueConfiguration config)
   {
      queueConfigurations.add(config);
      return this;
   }

   public Map<String, DiscoveryGroupConfiguration> getDiscoveryGroupConfigurations()
   {
      return discoveryGroupConfigurations;
   }

   public ConfigurationImpl setDiscoveryGroupConfigurations(final Map<String, DiscoveryGroupConfiguration> discoveryGroupConfigurations)
   {
      this.discoveryGroupConfigurations = discoveryGroupConfigurations;
      return this;
   }

   public ConfigurationImpl addDiscoveryGroupConfiguration(final String key, DiscoveryGroupConfiguration discoveryGroupConfiguration)
   {
      this.discoveryGroupConfigurations.put(key, discoveryGroupConfiguration);
      return this;
   }

   public int getIDCacheSize()
   {
      return idCacheSize;
   }

   public ConfigurationImpl setIDCacheSize(final int idCacheSize)
   {
      this.idCacheSize = idCacheSize;
      return this;
   }

   public boolean isPersistIDCache()
   {
      return persistIDCache;
   }

   public ConfigurationImpl setPersistIDCache(final boolean persist)
   {
      persistIDCache = persist;
      return this;
   }

   public String getBindingsDirectory()
   {
      return bindingsDirectory;
   }

   public ConfigurationImpl setBindingsDirectory(final String dir)
   {
      bindingsDirectory = dir;
      return this;
   }


   @Override
   public int getPageMaxConcurrentIO()
   {
      return maxConcurrentPageIO;
   }

   @Override
   public ConfigurationImpl setPageMaxConcurrentIO(int maxIO)
   {
      this.maxConcurrentPageIO = maxIO;
      return this;
   }


   public String getJournalDirectory()
   {
      return journalDirectory;
   }

   public ConfigurationImpl setJournalDirectory(final String dir)
   {
      journalDirectory = dir;
      return this;
   }

   public JournalType getJournalType()
   {
      return journalType;
   }

   public ConfigurationImpl setPagingDirectory(final String dir)
   {
      pagingDirectory = dir;
      return this;
   }

   public String getPagingDirectory()
   {
      return pagingDirectory;
   }

   public ConfigurationImpl setJournalType(final JournalType type)
   {
      journalType = type;
      return this;
   }

   public boolean isJournalSyncTransactional()
   {
      return journalSyncTransactional;
   }

   public ConfigurationImpl setJournalSyncTransactional(final boolean sync)
   {
      journalSyncTransactional = sync;
      return this;
   }

   public boolean isJournalSyncNonTransactional()
   {
      return journalSyncNonTransactional;
   }

   public ConfigurationImpl setJournalSyncNonTransactional(final boolean sync)
   {
      journalSyncNonTransactional = sync;
      return this;
   }

   public int getJournalFileSize()
   {
      return journalFileSize;
   }

   public ConfigurationImpl setJournalFileSize(final int size)
   {
      journalFileSize = size;
      return this;
   }

   public int getJournalMinFiles()
   {
      return journalMinFiles;
   }

   public ConfigurationImpl setJournalMinFiles(final int files)
   {
      journalMinFiles = files;
      return this;
   }

   public boolean isLogJournalWriteRate()
   {
      return logJournalWriteRate;
   }

   public ConfigurationImpl setLogJournalWriteRate(final boolean logJournalWriteRate)
   {
      this.logJournalWriteRate = logJournalWriteRate;
      return this;
   }

   public int getJournalPerfBlastPages()
   {
      return journalPerfBlastPages;
   }

   public ConfigurationImpl setJournalPerfBlastPages(final int journalPerfBlastPages)
   {
      this.journalPerfBlastPages = journalPerfBlastPages;
      return this;
   }

   public boolean isRunSyncSpeedTest()
   {
      return runSyncSpeedTest;
   }

   public ConfigurationImpl setRunSyncSpeedTest(final boolean run)
   {
      runSyncSpeedTest = run;
      return this;
   }

   public boolean isCreateBindingsDir()
   {
      return createBindingsDir;
   }

   public ConfigurationImpl setCreateBindingsDir(final boolean create)
   {
      createBindingsDir = create;
      return this;
   }

   public boolean isCreateJournalDir()
   {
      return createJournalDir;
   }

   public ConfigurationImpl setCreateJournalDir(final boolean create)
   {
      createJournalDir = create;
      return this;
   }

   public boolean isWildcardRoutingEnabled()
   {
      return wildcardRoutingEnabled;
   }

   public ConfigurationImpl setWildcardRoutingEnabled(final boolean enabled)
   {
      wildcardRoutingEnabled = enabled;
      return this;
   }

   public long getTransactionTimeout()
   {
      return transactionTimeout;
   }

   public ConfigurationImpl setTransactionTimeout(final long timeout)
   {
      transactionTimeout = timeout;
      return this;
   }

   public long getTransactionTimeoutScanPeriod()
   {
      return transactionTimeoutScanPeriod;
   }

   public ConfigurationImpl setTransactionTimeoutScanPeriod(final long period)
   {
      transactionTimeoutScanPeriod = period;
      return this;
   }

   public long getMessageExpiryScanPeriod()
   {
      return messageExpiryScanPeriod;
   }

   public ConfigurationImpl setMessageExpiryScanPeriod(final long messageExpiryScanPeriod)
   {
      this.messageExpiryScanPeriod = messageExpiryScanPeriod;
      return this;
   }

   public int getMessageExpiryThreadPriority()
   {
      return messageExpiryThreadPriority;
   }

   public ConfigurationImpl setMessageExpiryThreadPriority(final int messageExpiryThreadPriority)
   {
      this.messageExpiryThreadPriority = messageExpiryThreadPriority;
      return this;
   }

   public boolean isSecurityEnabled()
   {
      return securityEnabled;
   }

   public ConfigurationImpl setSecurityEnabled(final boolean enabled)
   {
      securityEnabled = enabled;
      return this;
   }

   public boolean isJMXManagementEnabled()
   {
      return jmxManagementEnabled;
   }

   public ConfigurationImpl setJMXManagementEnabled(final boolean enabled)
   {
      jmxManagementEnabled = enabled;
      return this;
   }

   public String getJMXDomain()
   {
      return jmxDomain;
   }

   public ConfigurationImpl setJMXDomain(final String domain)
   {
      jmxDomain = domain;
      return this;
   }

   public String getLargeMessagesDirectory()
   {
      return largeMessagesDirectory;
   }

   public ConfigurationImpl setLargeMessagesDirectory(final String directory)
   {
      largeMessagesDirectory = directory;
      return this;
   }

   public boolean isMessageCounterEnabled()
   {
      return messageCounterEnabled;
   }

   public ConfigurationImpl setMessageCounterEnabled(final boolean enabled)
   {
      messageCounterEnabled = enabled;
      return this;
   }

   public long getMessageCounterSamplePeriod()
   {
      return messageCounterSamplePeriod;
   }

   public ConfigurationImpl setMessageCounterSamplePeriod(final long period)
   {
      messageCounterSamplePeriod = period;
      return this;
   }

   public int getMessageCounterMaxDayHistory()
   {
      return messageCounterMaxDayHistory;
   }

   public ConfigurationImpl setMessageCounterMaxDayHistory(final int maxDayHistory)
   {
      messageCounterMaxDayHistory = maxDayHistory;
      return this;
   }

   public SimpleString getManagementAddress()
   {
      return managementAddress;
   }

   public ConfigurationImpl setManagementAddress(final SimpleString address)
   {
      managementAddress = address;
      return this;
   }

   public SimpleString getManagementNotificationAddress()
   {
      return managementNotificationAddress;
   }

   public ConfigurationImpl setManagementNotificationAddress(final SimpleString address)
   {
      managementNotificationAddress = address;
      return this;
   }

   public String getClusterUser()
   {
      return clusterUser;
   }

   public ConfigurationImpl setClusterUser(final String user)
   {
      clusterUser = user;
      return this;
   }

   public String getClusterPassword()
   {
      return clusterPassword;
   }

   public boolean isFailoverOnServerShutdown()
   {
      return failoverOnServerShutdown;
   }

   public ConfigurationImpl setFailoverOnServerShutdown(boolean failoverOnServerShutdown)
   {
      this.failoverOnServerShutdown = failoverOnServerShutdown;
      return this;
   }

   public ConfigurationImpl setClusterPassword(final String theclusterPassword)
   {
      clusterPassword = theclusterPassword;
      return this;
   }

   public int getJournalCompactMinFiles()
   {
      return journalCompactMinFiles;
   }

   public int getJournalCompactPercentage()
   {
      return journalCompactPercentage;
   }

   public ConfigurationImpl setJournalCompactMinFiles(final int minFiles)
   {
      journalCompactMinFiles = minFiles;
      return this;
   }

   public ConfigurationImpl setJournalCompactPercentage(final int percentage)
   {
      journalCompactPercentage = percentage;
      return this;
   }

   public long getServerDumpInterval()
   {
      return serverDumpInterval;
   }

   public ConfigurationImpl setServerDumpInterval(final long intervalInMilliseconds)
   {
      serverDumpInterval = intervalInMilliseconds;
      return this;
   }

   public int getMemoryWarningThreshold()
   {
      return memoryWarningThreshold;
   }

   public ConfigurationImpl setMemoryWarningThreshold(final int memoryWarningThreshold)
   {
      this.memoryWarningThreshold = memoryWarningThreshold;
      return this;
   }

   public long getMemoryMeasureInterval()
   {
      return memoryMeasureInterval;
   }

   public ConfigurationImpl setMemoryMeasureInterval(final long memoryMeasureInterval)
   {
      this.memoryMeasureInterval = memoryMeasureInterval;
      return this;
   }

   public int getJournalMaxIO_AIO()
   {
      return journalMaxIO_AIO;
   }

   public ConfigurationImpl setJournalMaxIO_AIO(final int journalMaxIO)
   {
      journalMaxIO_AIO = journalMaxIO;
      return this;
   }

   public int getJournalBufferTimeout_AIO()
   {
      return journalBufferTimeout_AIO;
   }

   public ConfigurationImpl setJournalBufferTimeout_AIO(final int journalBufferTimeout)
   {
      journalBufferTimeout_AIO = journalBufferTimeout;
      return this;
   }

   public int getJournalBufferSize_AIO()
   {
      return journalBufferSize_AIO;
   }

   public ConfigurationImpl setJournalBufferSize_AIO(final int journalBufferSize)
   {
      journalBufferSize_AIO = journalBufferSize;
      return this;
   }

   public int getJournalMaxIO_NIO()
   {
      return journalMaxIO_NIO;
   }

   public ConfigurationImpl setJournalMaxIO_NIO(final int journalMaxIO)
   {
      journalMaxIO_NIO = journalMaxIO;
      return this;
   }

   public int getJournalBufferTimeout_NIO()
   {
      return journalBufferTimeout_NIO;
   }

   public ConfigurationImpl setJournalBufferTimeout_NIO(final int journalBufferTimeout)
   {
      journalBufferTimeout_NIO = journalBufferTimeout;
      return this;
   }

   public int getJournalBufferSize_NIO()
   {
      return journalBufferSize_NIO;
   }

   public ConfigurationImpl setJournalBufferSize_NIO(final int journalBufferSize)
   {
      journalBufferSize_NIO = journalBufferSize;
      return this;
   }

   @Override
   public Map<String, AddressSettings> getAddressesSettings()
   {
      return addressesSettings;
   }

   @Override
   public ConfigurationImpl setAddressesSettings(final Map<String, AddressSettings> addressesSettings)
   {
      this.addressesSettings = addressesSettings;
      return this;
   }

   @Override
   public ConfigurationImpl addAddressesSetting(String key, AddressSettings addressesSetting)
   {
      this.addressesSettings.put(key, addressesSetting);
      return this;
   }

   @Override
   public Map<String, Set<Role>> getSecurityRoles()
   {
      return securitySettings;
   }

   @Override
   public ConfigurationImpl setSecurityRoles(final Map<String, Set<Role>> securitySettings)
   {
      this.securitySettings = securitySettings;
      return this;
   }

   public List<ConnectorServiceConfiguration> getConnectorServiceConfigurations()
   {
      return this.connectorServiceConfigurations;
   }

   public boolean isCheckForLiveServer()
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         return ((ReplicatedPolicyConfiguration)haPolicyConfiguration).isCheckForLiveServer();
      }
      else
      {
         return false;
      }
   }

   public ConfigurationImpl setCheckForLiveServer(boolean checkForLiveServer)
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ((ReplicatedPolicyConfiguration)haPolicyConfiguration).setCheckForLiveServer(checkForLiveServer);
      }

      return this;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("HornetQ Configuration (");
      sb.append("clustered=").append(isClustered()).append(",");
      sb.append("journalDirectory=").append(journalDirectory).append(",");
      sb.append("bindingsDirectory=").append(bindingsDirectory).append(",");
      sb.append("largeMessagesDirectory=").append(largeMessagesDirectory).append(",");
      sb.append("pagingDirectory=").append(pagingDirectory);
      sb.append(")");
      return sb.toString();
   }

   public ConfigurationImpl setConnectorServiceConfigurations(final List<ConnectorServiceConfiguration> configs)
   {
      this.connectorServiceConfigurations = configs;
      return this;
   }

   public ConfigurationImpl addConnectorServiceConfiguration(final ConnectorServiceConfiguration config)
   {
      this.connectorServiceConfigurations.add(config);
      return this;
   }

   public boolean isMaskPassword()
   {
      return maskPassword;
   }

   public ConfigurationImpl setMaskPassword(boolean maskPassword)
   {
      this.maskPassword = maskPassword;
      return this;
   }

   public ConfigurationImpl setPasswordCodec(String codec)
   {
      passwordCodec = codec;
      return this;
   }

   public String getPasswordCodec()
   {
      return passwordCodec;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public ConfigurationImpl setName(String name)
   {
      this.name = name;
      return this;
   }


   /*
   * All these operations are now deprecated and may or my not work, best efforts is all we can do, an HA Policy needs to be used
   * */
   @Deprecated
   public ConfigurationImpl setBackup(final boolean backup)
   {
      return this;
   }

   @Deprecated
   public boolean isSharedStore()
   {
      return haPolicyConfiguration instanceof SharedStoreMasterPolicyConfiguration || haPolicyConfiguration instanceof SharedStoreSlavePolicyConfiguration;
   }

   @Deprecated
   public ConfigurationImpl setSharedStore(final boolean sharedStore)
   {
      return this;
   }

   @Deprecated
   public long getFailbackDelay()
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         return hapc.getFailbackDelay();
      }
      else if (haPolicyConfiguration instanceof ReplicatedPolicyConfiguration)
      {
         ReplicatedPolicyConfiguration hapc = (ReplicatedPolicyConfiguration) haPolicyConfiguration;
         return hapc.getFailbackDelay();
      }
      else if (haPolicyConfiguration instanceof SharedStoreMasterPolicyConfiguration)
      {
         SharedStoreMasterPolicyConfiguration hapc = (SharedStoreMasterPolicyConfiguration) haPolicyConfiguration;
         return hapc.getFailbackDelay();
      }
      else if (haPolicyConfiguration instanceof SharedStoreSlavePolicyConfiguration)
      {
         SharedStoreSlavePolicyConfiguration hapc = (SharedStoreSlavePolicyConfiguration) haPolicyConfiguration;
         return hapc.getFailbackDelay();
      }
      return -1;
   }

   @Deprecated
   public ConfigurationImpl setFailbackDelay(long failbackDelay)
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         hapc.setFailbackDelay(failbackDelay);
      }
      else if (haPolicyConfiguration instanceof ReplicatedPolicyConfiguration)
      {
         ReplicatedPolicyConfiguration hapc = (ReplicatedPolicyConfiguration) haPolicyConfiguration;
         hapc.setFailbackDelay(failbackDelay);
      }
      else if (haPolicyConfiguration instanceof SharedStoreMasterPolicyConfiguration)
      {
         SharedStoreMasterPolicyConfiguration hapc = (SharedStoreMasterPolicyConfiguration) haPolicyConfiguration;
         hapc.setFailbackDelay(failbackDelay);
      }
      else if (haPolicyConfiguration instanceof SharedStoreSlavePolicyConfiguration)
      {
         SharedStoreSlavePolicyConfiguration hapc = (SharedStoreSlavePolicyConfiguration) haPolicyConfiguration;
         hapc.setFailbackDelay(failbackDelay);
      }

      return this;
   }

   @Deprecated
   public String getBackupGroupName()
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         return hapc.getGroupName();
      }
      else if (haPolicyConfiguration instanceof ReplicatedPolicyConfiguration)
      {
         ReplicatedPolicyConfiguration hapc = (ReplicatedPolicyConfiguration) haPolicyConfiguration;
         return hapc.getGroupName();
      }
      return null;
   }

   @Deprecated
   public ConfigurationImpl setBackupGroupName(String nodeGroupName)
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         hapc.setGroupName(nodeGroupName);
      }
      else if (haPolicyConfiguration instanceof ReplicatedPolicyConfiguration)
      {
         ReplicatedPolicyConfiguration hapc = (ReplicatedPolicyConfiguration) haPolicyConfiguration;
         hapc.setGroupName(nodeGroupName);
      }
      return this;
   }

   @Override
   @Deprecated
   public ConfigurationImpl setReplicationClustername(String clusterName)
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         hapc.setClusterName(clusterName);
      }
      else if (haPolicyConfiguration instanceof ReplicatedPolicyConfiguration)
      {
         ReplicatedPolicyConfiguration hapc = (ReplicatedPolicyConfiguration) haPolicyConfiguration;
         hapc.setClusterName(clusterName);
      }
      return this;
   }

   @Override
   @Deprecated
   public String getReplicationClustername()
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         return hapc.getClusterName();
      }
      else if (haPolicyConfiguration instanceof ReplicatedPolicyConfiguration)
      {
         ReplicatedPolicyConfiguration hapc = (ReplicatedPolicyConfiguration) haPolicyConfiguration;
         return hapc.getClusterName();
      }
      return null;
   }

   @Override
   @Deprecated
   public ConfigurationImpl setMaxSavedReplicatedJournalSize(int maxSavedReplicatedJournalsSize)
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         hapc.setMaxSavedReplicatedJournalsSize(maxSavedReplicatedJournalsSize);
      }

      return this;
   }

   @Override
   @Deprecated
   public int getMaxSavedReplicatedJournalsSize()
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         return hapc.getMaxSavedReplicatedJournalsSize();
      }
      return -1;
   }

   @Deprecated
   public boolean isAllowFailBack()
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         return hapc.isAllowFailBack();
      }
      else if (haPolicyConfiguration instanceof SharedStoreSlavePolicyConfiguration)
      {
         SharedStoreSlavePolicyConfiguration hapc = (SharedStoreSlavePolicyConfiguration) haPolicyConfiguration;
         return hapc.isAllowFailBack();
      }
      return false;
   }

   @Deprecated
   public void setAllowFailBack(boolean allowAutoFailBack)
   {
      if (haPolicyConfiguration instanceof ReplicaPolicyConfiguration)
      {
         ReplicaPolicyConfiguration hapc = (ReplicaPolicyConfiguration) haPolicyConfiguration;
         hapc.setAllowFailBack(allowAutoFailBack);
      }
      else if (haPolicyConfiguration instanceof SharedStoreSlavePolicyConfiguration)
      {
         SharedStoreSlavePolicyConfiguration hapc = (SharedStoreSlavePolicyConfiguration) haPolicyConfiguration;
         hapc.setAllowFailBack(allowAutoFailBack);
      }
   }

   @Deprecated
   public boolean isBackup()
   {
      return haPolicyConfiguration instanceof ReplicaPolicyConfiguration || haPolicyConfiguration instanceof SharedStoreSlavePolicyConfiguration;
   }

   @Override
   public ConfigurationImpl setResolveProtocols(boolean resolveProtocols)
   {
      this.resolveProtocols = resolveProtocols;
      return this;
   }

   @Override
   public boolean isResolveProtocols()
   {
      return resolveProtocols;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((acceptorConfigs == null) ? 0 : acceptorConfigs.hashCode());
      result = prime * result + ((addressesSettings == null) ? 0 : addressesSettings.hashCode());
      result = prime * result + (asyncConnectionExecutionEnabled ? 1231 : 1237);
      result = prime * result + ((bindingsDirectory == null) ? 0 : bindingsDirectory.hashCode());
      result = prime * result + ((bridgeConfigurations == null) ? 0 : bridgeConfigurations.hashCode());
      result = prime * result + ((broadcastGroupConfigurations == null) ? 0 : broadcastGroupConfigurations.hashCode());
      result = prime * result + ((clusterConfigurations == null) ? 0 : clusterConfigurations.hashCode());
      result = prime * result + ((clusterPassword == null) ? 0 : clusterPassword.hashCode());
      result = prime * result + ((clusterUser == null) ? 0 : clusterUser.hashCode());
      result = prime * result + (int)(connectionTTLOverride ^ (connectionTTLOverride >>> 32));
      result = prime * result + ((connectorConfigs == null) ? 0 : connectorConfigs.hashCode());
      result =
               prime * result +
                        ((connectorServiceConfigurations == null) ? 0 : connectorServiceConfigurations.hashCode());
      result = prime * result + (createBindingsDir ? 1231 : 1237);
      result = prime * result + (createJournalDir ? 1231 : 1237);
      result = prime * result + ((discoveryGroupConfigurations == null) ? 0 : discoveryGroupConfigurations.hashCode());
      result = prime * result + ((divertConfigurations == null) ? 0 : divertConfigurations.hashCode());
      result = prime * result + (failoverOnServerShutdown ? 1231 : 1237);
      result = prime * result + (fileDeploymentEnabled ? 1231 : 1237);
      result = prime * result + (int)(fileDeploymentScanPeriod ^ (fileDeploymentScanPeriod >>> 32));
      result = prime * result + ((groupingHandlerConfiguration == null) ? 0 : groupingHandlerConfiguration.hashCode());
      result = prime * result + idCacheSize;
      result =
               prime * result +
                        ((incomingInterceptorClassNames == null) ? 0 : incomingInterceptorClassNames.hashCode());
      result = prime * result + ((jmxDomain == null) ? 0 : jmxDomain.hashCode());
      result = prime * result + (jmxManagementEnabled ? 1231 : 1237);
      result = prime * result + journalBufferSize_AIO;
      result = prime * result + journalBufferSize_NIO;
      result = prime * result + journalBufferTimeout_AIO;
      result = prime * result + journalBufferTimeout_NIO;
      result = prime * result + journalCompactMinFiles;
      result = prime * result + journalCompactPercentage;
      result = prime * result + ((journalDirectory == null) ? 0 : journalDirectory.hashCode());
      result = prime * result + journalFileSize;
      result = prime * result + journalMaxIO_AIO;
      result = prime * result + journalMaxIO_NIO;
      result = prime * result + journalMinFiles;
      result = prime * result + journalPerfBlastPages;
      result = prime * result + (journalSyncNonTransactional ? 1231 : 1237);
      result = prime * result + (journalSyncTransactional ? 1231 : 1237);
      result = prime * result + ((journalType == null) ? 0 : journalType.hashCode());
      result = prime * result + ((largeMessagesDirectory == null) ? 0 : largeMessagesDirectory.hashCode());
      result = prime * result + (logJournalWriteRate ? 1231 : 1237);
      result = prime * result + ((managementAddress == null) ? 0 : managementAddress.hashCode());
      result =
               prime * result +
                        ((managementNotificationAddress == null) ? 0 : managementNotificationAddress.hashCode());
      result = prime * result + (maskPassword ? 1231 : 1237);
      result = prime * result + maxConcurrentPageIO;
      result = prime * result + (int)(memoryMeasureInterval ^ (memoryMeasureInterval >>> 32));
      result = prime * result + memoryWarningThreshold;
      result = prime * result + (messageCounterEnabled ? 1231 : 1237);
      result = prime * result + messageCounterMaxDayHistory;
      result = prime * result + (int)(messageCounterSamplePeriod ^ (messageCounterSamplePeriod >>> 32));
      result = prime * result + (int)(messageExpiryScanPeriod ^ (messageExpiryScanPeriod >>> 32));
      result = prime * result + messageExpiryThreadPriority;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result =
               prime * result +
                        ((outgoingInterceptorClassNames == null) ? 0 : outgoingInterceptorClassNames.hashCode());
      result = prime * result + ((pagingDirectory == null) ? 0 : pagingDirectory.hashCode());
      result = prime * result + (persistDeliveryCountBeforeDelivery ? 1231 : 1237);
      result = prime * result + (persistIDCache ? 1231 : 1237);
      result = prime * result + (persistenceEnabled ? 1231 : 1237);
      result = prime * result + ((queueConfigurations == null) ? 0 : queueConfigurations.hashCode());
      result = prime * result + (runSyncSpeedTest ? 1231 : 1237);
      result = prime * result + scheduledThreadPoolMaxSize;
      result = prime * result + (securityEnabled ? 1231 : 1237);
      result = prime * result + (int)(securityInvalidationInterval ^ (securityInvalidationInterval >>> 32));
      result = prime * result + ((securitySettings == null) ? 0 : securitySettings.hashCode());
      result = prime * result + (int)(serverDumpInterval ^ (serverDumpInterval >>> 32));
      result = prime * result + threadPoolMaxSize;
      result = prime * result + (int)(transactionTimeout ^ (transactionTimeout >>> 32));
      result = prime * result + (int)(transactionTimeoutScanPeriod ^ (transactionTimeoutScanPeriod >>> 32));
      result = prime * result + (wildcardRoutingEnabled ? 1231 : 1237);
      result = prime * result + (resolveProtocols ? 1231 : 1237);
      result = prime * result + (int) (journalLockAcquisitionTimeout ^ (journalLockAcquisitionTimeout >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ConfigurationImpl))
         return false;
      ConfigurationImpl other = (ConfigurationImpl)obj;
      if (acceptorConfigs == null)
      {
         if (other.acceptorConfigs != null)
            return false;
      }
      else if (!acceptorConfigs.equals(other.acceptorConfigs))
         return false;
      if (addressesSettings == null)
      {
         if (other.addressesSettings != null)
            return false;
      }
      else if (!addressesSettings.equals(other.addressesSettings))
         return false;
      if (asyncConnectionExecutionEnabled != other.asyncConnectionExecutionEnabled)
         return false;

      if (bindingsDirectory == null)
      {
         if (other.bindingsDirectory != null)
            return false;
      }
      else if (!bindingsDirectory.equals(other.bindingsDirectory))
         return false;
      if (bridgeConfigurations == null)
      {
         if (other.bridgeConfigurations != null)
            return false;
      }
      else if (!bridgeConfigurations.equals(other.bridgeConfigurations))
         return false;
      if (broadcastGroupConfigurations == null)
      {
         if (other.broadcastGroupConfigurations != null)
            return false;
      }
      else if (!broadcastGroupConfigurations.equals(other.broadcastGroupConfigurations))
         return false;
      if (clusterConfigurations == null)
      {
         if (other.clusterConfigurations != null)
            return false;
      }
      else if (!clusterConfigurations.equals(other.clusterConfigurations))
         return false;
      if (clusterPassword == null)
      {
         if (other.clusterPassword != null)
            return false;
      }
      else if (!clusterPassword.equals(other.clusterPassword))
         return false;
      if (clusterUser == null)
      {
         if (other.clusterUser != null)
            return false;
      }
      else if (!clusterUser.equals(other.clusterUser))
         return false;
      if (connectionTTLOverride != other.connectionTTLOverride)
         return false;
      if (connectorConfigs == null)
      {
         if (other.connectorConfigs != null)
            return false;
      }
      else if (!connectorConfigs.equals(other.connectorConfigs))
         return false;
      if (connectorServiceConfigurations == null)
      {
         if (other.connectorServiceConfigurations != null)
            return false;
      }
      else if (!connectorServiceConfigurations.equals(other.connectorServiceConfigurations))
         return false;
      if (createBindingsDir != other.createBindingsDir)
         return false;
      if (createJournalDir != other.createJournalDir)
         return false;
      if (discoveryGroupConfigurations == null)
      {
         if (other.discoveryGroupConfigurations != null)
            return false;
      }
      else if (!discoveryGroupConfigurations.equals(other.discoveryGroupConfigurations))
         return false;
      if (divertConfigurations == null)
      {
         if (other.divertConfigurations != null)
            return false;
      }
      else if (!divertConfigurations.equals(other.divertConfigurations))
         return false;
      if (failoverOnServerShutdown != other.failoverOnServerShutdown)
         return false;
      if (fileDeploymentEnabled != other.fileDeploymentEnabled)
         return false;
      if (fileDeploymentScanPeriod != other.fileDeploymentScanPeriod)
         return false;
      if (groupingHandlerConfiguration == null)
      {
         if (other.groupingHandlerConfiguration != null)
            return false;
      }
      else if (!groupingHandlerConfiguration.equals(other.groupingHandlerConfiguration))
         return false;
      if (idCacheSize != other.idCacheSize)
         return false;
      if (incomingInterceptorClassNames == null)
      {
         if (other.incomingInterceptorClassNames != null)
            return false;
      }
      else if (!incomingInterceptorClassNames.equals(other.incomingInterceptorClassNames))
         return false;
      if (jmxDomain == null)
      {
         if (other.jmxDomain != null)
            return false;
      }
      else if (!jmxDomain.equals(other.jmxDomain))
         return false;
      if (jmxManagementEnabled != other.jmxManagementEnabled)
         return false;
      if (journalBufferSize_AIO != other.journalBufferSize_AIO)
         return false;
      if (journalBufferSize_NIO != other.journalBufferSize_NIO)
         return false;
      if (journalBufferTimeout_AIO != other.journalBufferTimeout_AIO)
         return false;
      if (journalBufferTimeout_NIO != other.journalBufferTimeout_NIO)
         return false;
      if (journalCompactMinFiles != other.journalCompactMinFiles)
         return false;
      if (journalCompactPercentage != other.journalCompactPercentage)
         return false;
      if (journalDirectory == null)
      {
         if (other.journalDirectory != null)
            return false;
      }
      else if (!journalDirectory.equals(other.journalDirectory))
         return false;
      if (journalFileSize != other.journalFileSize)
         return false;
      if (journalMaxIO_AIO != other.journalMaxIO_AIO)
         return false;
      if (journalMaxIO_NIO != other.journalMaxIO_NIO)
         return false;
      if (journalMinFiles != other.journalMinFiles)
         return false;
      if (journalPerfBlastPages != other.journalPerfBlastPages)
         return false;
      if (journalSyncNonTransactional != other.journalSyncNonTransactional)
         return false;
      if (journalSyncTransactional != other.journalSyncTransactional)
         return false;
      if (journalType != other.journalType)
         return false;
      if (largeMessagesDirectory == null)
      {
         if (other.largeMessagesDirectory != null)
            return false;
      }
      else if (!largeMessagesDirectory.equals(other.largeMessagesDirectory))
         return false;
      if (logJournalWriteRate != other.logJournalWriteRate)
         return false;
      if (managementAddress == null)
      {
         if (other.managementAddress != null)
            return false;
      }
      else if (!managementAddress.equals(other.managementAddress))
         return false;
      if (managementNotificationAddress == null)
      {
         if (other.managementNotificationAddress != null)
            return false;
      }
      else if (!managementNotificationAddress.equals(other.managementNotificationAddress))
         return false;
      if (maskPassword != other.maskPassword)
         return false;
      if (maxConcurrentPageIO != other.maxConcurrentPageIO)
         return false;
      if (memoryMeasureInterval != other.memoryMeasureInterval)
         return false;
      if (memoryWarningThreshold != other.memoryWarningThreshold)
         return false;
      if (messageCounterEnabled != other.messageCounterEnabled)
         return false;
      if (messageCounterMaxDayHistory != other.messageCounterMaxDayHistory)
         return false;
      if (messageCounterSamplePeriod != other.messageCounterSamplePeriod)
         return false;
      if (messageExpiryScanPeriod != other.messageExpiryScanPeriod)
         return false;
      if (messageExpiryThreadPriority != other.messageExpiryThreadPriority)
         return false;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;

      if (outgoingInterceptorClassNames == null)
      {
         if (other.outgoingInterceptorClassNames != null)
            return false;
      }
      else if (!outgoingInterceptorClassNames.equals(other.outgoingInterceptorClassNames))
         return false;
      if (pagingDirectory == null)
      {
         if (other.pagingDirectory != null)
            return false;
      }
      else if (!pagingDirectory.equals(other.pagingDirectory))
         return false;
      if (persistDeliveryCountBeforeDelivery != other.persistDeliveryCountBeforeDelivery)
         return false;
      if (persistIDCache != other.persistIDCache)
         return false;
      if (persistenceEnabled != other.persistenceEnabled)
         return false;
      if (queueConfigurations == null)
      {
         if (other.queueConfigurations != null)
            return false;
      }
      else if (!queueConfigurations.equals(other.queueConfigurations))
         return false;
      if (runSyncSpeedTest != other.runSyncSpeedTest)
         return false;
      if (scheduledThreadPoolMaxSize != other.scheduledThreadPoolMaxSize)
         return false;
      if (securityEnabled != other.securityEnabled)
         return false;
      if (securityInvalidationInterval != other.securityInvalidationInterval)
         return false;
      if (securitySettings == null)
      {
         if (other.securitySettings != null)
            return false;
      }
      else if (!securitySettings.equals(other.securitySettings))
         return false;
      if (serverDumpInterval != other.serverDumpInterval)
         return false;
      if (threadPoolMaxSize != other.threadPoolMaxSize)
         return false;
      if (transactionTimeout != other.transactionTimeout)
         return false;
      if (transactionTimeoutScanPeriod != other.transactionTimeoutScanPeriod)
         return false;
      if (wildcardRoutingEnabled != other.wildcardRoutingEnabled)
         return false;
      if (resolveProtocols != other.resolveProtocols)
         return false;
      if (journalLockAcquisitionTimeout != other.journalLockAcquisitionTimeout)
         return false;
      return true;
   }

   @Override
   public Configuration copy() throws Exception
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(bos);
      os.writeObject(this);
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
      return (Configuration) ois.readObject();
   }

   @Override
   public ConfigurationImpl setJournalLockAcquisitionTimeout(long journalLockAcquisitionTimeout)
   {
      this.journalLockAcquisitionTimeout = journalLockAcquisitionTimeout;
      return this;
   }

   @Override
   public long getJournalLockAcquisitionTimeout()
   {
      return journalLockAcquisitionTimeout;
   }

   @Override
   public HAPolicyConfiguration getHAPolicyConfiguration()
   {
      return haPolicyConfiguration;
   }

   @Override
   public ConfigurationImpl setHAPolicyConfiguration(HAPolicyConfiguration haPolicyConfiguration)
   {
      this.haPolicyConfiguration = haPolicyConfiguration;
      return this;
   }
}
