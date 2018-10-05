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

package org.flowable.engine.impl.bpmn.behavior;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.CaseTask;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstanceBuilder;
import org.flowable.cmmn.engine.impl.runtime.CaseInstanceBuilderImpl;
import org.flowable.cmmn.engine.impl.runtime.CaseInstanceHelper;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.cmmn.CaseInstanceService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.util.CommandContextUtil;

/**
 * @author Avinash Gosi
 */
public class CaseTaskActivityBehavior extends TaskActivityBehavior {

    private static final long serialVersionUID = 1L;

    protected CaseTask caseTask;

    public CaseTaskActivityBehavior(ServiceTask caseTask) {
        this.caseTask = (CaseTask) caseTask;
    }

    @Override
    public void execute(DelegateExecution execution) {

        /*CommandContext commandContext = CommandContextUtil.getCommandContext();
        CaseInstanceHelper caseInstanceHelper = CommandContextUtil.getCaseInstanceHelper(commandContext);
        CaseInstanceBuilder caseInstanceBuilder = new CaseInstanceBuilderImpl().
                caseDefinitionKey(caseTask.getCaseRef());

        caseInstanceHelper.startCaseInstance(caseInstanceBuilder);*/

//        CaseInstanceService caseInstanceService = CommandContextUtil.getProcessEngineConfiguration().getCaseInstanceService();
        CmmnRuntimeService cmmnRuntimeService = CommandContextUtil.getProcessEngineConfiguration().getCmmnRuntimeService();

        if (cmmnRuntimeService == null) {
            throw new FlowableException("Could not start case instance: no " + CmmnRuntimeService.class + " implementation found");
        }

        String externalRef = null;
        if (caseTask != null) {
            for (FieldExtension fieldExtension : caseTask.getFieldExtensions()) {
                if ("caseref".equals(fieldExtension.getFieldName()) && StringUtils.isNotEmpty(fieldExtension.getStringValue())) {
                    externalRef = fieldExtension.getStringValue();
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(externalRef)) {
            throw new FlowableException("Could not start case instance: no externalRef defined");
        }

        CaseInstanceBuilder caseInstanceBuilder = cmmnRuntimeService.createCaseInstanceBuilder();
        caseInstanceBuilder.caseDefinitionKey(externalRef);

        CaseInstance caseInstance = caseInstanceBuilder.start();

        String processInstanceId = caseInstance.getId();//caseInstanceService.startCaseInstanceByKey(externalRef, null);
    }

}
