/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.configurator;

import org.flowable.cmmn.engine.CmmnEngineConfiguration;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.AbstractEngineConfigurator;
import org.flowable.common.engine.impl.EngineDeployer;
import org.flowable.common.engine.impl.db.DbSqlSessionFactory;
import org.flowable.common.engine.impl.interceptor.EngineConfigurationConstants;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.configurator.impl.deployer.BpmnDeployer;
import org.flowable.engine.configurator.impl.process.DefaultCaseInstanceService;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.impl.db.EntityDependencyOrder;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.VariableByteArrayEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.VariableInstanceEntityImpl;

import java.util.Collections;
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class ProcessEngineConfigurator extends AbstractEngineConfigurator {

    protected ProcessEngineConfiguration processEngineConfiguration;

    @Override
    public int getPriority() {
        return EngineConfigurationConstants.PRIORITY_ENGINE_PROCESS;
    }

    @Override
    protected List<EngineDeployer> getCustomDeployers() {
        return Collections.<EngineDeployer>singletonList(new BpmnDeployer());
    }

    @Override
    protected String getMybatisCfgPath() {
        return ProcessEngineConfigurationImpl.DEFAULT_MYBATIS_MAPPING_FILE;
    }

    @Override
    public void configure(AbstractEngineConfiguration engineConfiguration) {
        if (processEngineConfiguration == null) {
            processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        }

        initialiseCommonProperties(engineConfiguration, processEngineConfiguration);

        CmmnEngineConfiguration cmmnEngineConfiguration = getCmmnEngineConfiguration(engineConfiguration);
        if (cmmnEngineConfiguration != null) {
            copyProcessEngineProperties(cmmnEngineConfiguration);

        }

        initProcessEngine();

        initServiceConfigurations(engineConfiguration, processEngineConfiguration);
    }
    
    @Override
    protected void initDbSqlSessionFactory(AbstractEngineConfiguration engineConfiguration, AbstractEngineConfiguration targetEngineConfiguration) {
        DbSqlSessionFactory dbSqlSessionFactory = engineConfiguration.getDbSqlSessionFactory();
        targetEngineConfiguration.setDbSqlSessionFactory(engineConfiguration.getDbSqlSessionFactory());
        targetEngineConfiguration.setSqlSessionFactory(engineConfiguration.getSqlSessionFactory());

        if (getEntityInsertionOrder() != null) {
            // remove identity link and variable entity classes due to foreign key handling
            dbSqlSessionFactory.getInsertionOrder().remove(IdentityLinkEntityImpl.class);
            dbSqlSessionFactory.getInsertionOrder().remove(VariableInstanceEntityImpl.class);
            dbSqlSessionFactory.getInsertionOrder().remove(VariableByteArrayEntityImpl.class);
            for (Class<? extends Entity> clazz : getEntityInsertionOrder()) {
                dbSqlSessionFactory.getInsertionOrder().add(clazz);
            }
        }

        if (getEntityDeletionOrder() != null) {
            // remove identity link and variable entity classes due to foreign key handling
            dbSqlSessionFactory.getDeletionOrder().remove(IdentityLinkEntityImpl.class);
            dbSqlSessionFactory.getDeletionOrder().remove(VariableInstanceEntityImpl.class);
            dbSqlSessionFactory.getDeletionOrder().remove(VariableByteArrayEntityImpl.class);
            for (Class<? extends Entity> clazz : getEntityDeletionOrder()) {
                dbSqlSessionFactory.getDeletionOrder().add(clazz);
            }
        }
    }

    @Override
    protected List<Class<? extends Entity>> getEntityInsertionOrder() {
        return EntityDependencyOrder.INSERT_ORDER;
    }

    @Override
    protected List<Class<? extends Entity>> getEntityDeletionOrder() {
        return EntityDependencyOrder.DELETE_ORDER;
    }

    protected synchronized ProcessEngine initProcessEngine() {
        if (processEngineConfiguration == null) {
            throw new FlowableException("ProcessEngineConfiguration is required");
        }

        return processEngineConfiguration.buildProcessEngine();
    }

    public ProcessEngineConfiguration getProcessEngineConfiguration() {
        return processEngineConfiguration;
    }

    public ProcessEngineConfigurator setProcessEngineConfiguration(ProcessEngineConfiguration processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
        return this;
    }

    protected CmmnEngineConfiguration getCmmnEngineConfiguration(AbstractEngineConfiguration engineConfiguration) {
        if (engineConfiguration.getEngineConfigurations().containsKey(EngineConfigurationConstants.KEY_CMMN_ENGINE_CONFIG)) {
            return (CmmnEngineConfiguration) engineConfiguration.getEngineConfigurations()
                    .get(EngineConfigurationConstants.KEY_CMMN_ENGINE_CONFIG);
        }
        return null;
    }

    protected void copyProcessEngineProperties(CmmnEngineConfiguration cmmnEngineConfiguration) {
        initCaseInstanceService(cmmnEngineConfiguration);
        initCaseInstanceStateChangedCallbacks(cmmnEngineConfiguration);

        /*processEngineConfiguration.setEnableTaskRelationshipCounts(cmmnEngineConfiguration.getPerformanceSettings().isEnableTaskRelationshipCounts());
        processEngineConfiguration.setTaskQueryLimit(cmmnEngineConfiguration.getTaskQueryLimit());
        processEngineConfiguration.setHistoricTaskQueryLimit(cmmnEngineConfiguration.getHistoricTaskQueryLimit());
        // use the same query limit for executions/processes and cases
        processEngineConfiguration.setCaseQueryLimit(cmmnEngineConfiguration.getExecutionQueryLimit());
        processEngineConfiguration.setHistoricCaseQueryLimit(cmmnEngineConfiguration.getHistoricProcessInstancesQueryLimit());

        if (cmmnEngineConfiguration.isAsyncHistoryEnabled()) {
            AsyncExecutor asyncHistoryExecutor = cmmnEngineConfiguration.getAsyncHistoryExecutor();

            // Inject the async history executor from the process engine.
            // The job handlers will be added in the CmmnEngineConfiguration itself
            processEngineConfiguration.setAsyncHistoryEnabled(true);
            processEngineConfiguration.setAsyncHistoryExecutor(asyncHistoryExecutor);
            processEngineConfiguration.setAsyncHistoryJsonGroupingEnabled(cmmnEngineConfiguration.isAsyncHistoryJsonGroupingEnabled());
            processEngineConfiguration.setAsyncHistoryJsonGroupingThreshold(cmmnEngineConfiguration.getAsyncHistoryJsonGroupingThreshold());
            processEngineConfiguration.setAsyncHistoryJsonGzipCompressionEnabled(cmmnEngineConfiguration.isAsyncHistoryJsonGzipCompressionEnabled());

            // See the beforeInit
            ((CmmnEngineConfiguration) cmmnEngineConfiguration).setHistoryJobExecutionScope(JobServiceConfiguration.JOB_EXECUTION_SCOPE_ALL);
        }*/
    }

    private void initCaseInstanceStateChangedCallbacks(CmmnEngineConfiguration cmmnEngineConfiguration) {
        /*if (cmmnEngineConfiguration.getCaseInstanceStateChangeCallbacks() == null) {
            cmmnEngineConfiguration.setCaseInstanceStateChangeCallbacks(new HashMap<>());
        }
        Map<String, List<RuntimeInstanceStateChangeCallback>> callbacks = cmmnEngineConfiguration.getCaseInstanceStateChangeCallbacks();
        if (!callbacks.containsKey(CallbackTypes.PLAN_ITEM_CHILD_PROCESS)) {
            callbacks.put(CallbackTypes.PLAN_ITEM_CHILD_PROCESS, new ArrayList<>());
        }
        callbacks.get(CallbackTypes.PLAN_ITEM_CHILD_PROCESS).add(new ChildCaseInstanceStateChangeCallback(processEngineConfiguration));*/
    }

    protected void initCaseInstanceService(CmmnEngineConfiguration cmmnEngineConfiguration) {
        processEngineConfiguration.setCaseInstanceService(new DefaultCaseInstanceService(cmmnEngineConfiguration.getCmmnRuntimeService()));
    }
}
