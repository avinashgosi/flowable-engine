package org.flowable.engine.cmmn;

public interface CaseInstanceService {


    String startCaseInstanceByKey(String caseDefinitionKey, String tenantId);

    String startCaseInstanceByKey(String caseDefinitionKey, String executionId, String tenantId);
}
