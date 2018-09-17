package org.flowable.engine.configurator.impl.process;

import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstanceBuilder;
import org.flowable.engine.cmmn.CaseInstanceService;

public class DefaultCaseInstanceService implements CaseInstanceService {

    protected CmmnRuntimeService cmmnEngineRuntimeService;
    public DefaultCaseInstanceService(CmmnRuntimeService cmmnRuntimeService) {
        this.cmmnEngineRuntimeService = cmmnRuntimeService;
    }

    @Override
    public String startCaseInstanceByKey(String caseDefinitionKey, String tenantId) {
        return startCaseInstanceByKey(caseDefinitionKey, null, tenantId);
    }

    @Override
    public String startCaseInstanceByKey(String caseDefinitionKey, String planItemInstanceId, String tenantId) {

        CaseInstanceBuilder caseInstanceBuilder = cmmnEngineRuntimeService.createCaseInstanceBuilder();
        caseInstanceBuilder.caseDefinitionKey(caseDefinitionKey);

        if (tenantId != null) {
            caseInstanceBuilder.tenantId(tenantId);
        }

        /*if (planItemInstanceId != null) {
            caseInstanceBuilder.callbackId(planItemInstanceId);
            caseInstanceBuilder.callbackType(CallbackTypes.PLAN_ITEM_CHILD_CASE);
        }*/

        CaseInstance caseInstance = caseInstanceBuilder.start();

        return caseInstance.getId();
    }
}
