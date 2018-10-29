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
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.cmmn.CaseInstanceService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.SubProcessActivityBehavior;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.List;

/**
 * @author Avinash Gosi
 */
public class CaseTaskActivityBehavior extends AbstractBpmnActivityBehavior implements SubProcessActivityBehavior {

    private static final long serialVersionUID = 1L;

    protected CaseTask caseTask;

    public CaseTaskActivityBehavior(ServiceTask caseTask) {
        this.caseTask = (CaseTask) caseTask;
    }

    @Override
    public void execute(DelegateExecution execution) {


        CaseInstanceService caseInstanceService = CommandContextUtil.getProcessEngineConfiguration().getCaseInstanceService();

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

        String processInstanceId = caseInstanceService.startCaseInstanceByKey(externalRef, null);

        //leave(execution);
    }

    @Override
    public void trigger(DelegateExecution execution, String signalName, Object signalData) {
        List<TaskEntity> taskEntities = CommandContextUtil.getTaskService().findTasksByExecutionId(execution.getId()); // Should be only one
        for (TaskEntity taskEntity : taskEntities) {
            if (!taskEntity.isDeleted()) {
                throw new FlowableException("CaseTask should not be signalled before complete");
            }
        }

        leave(execution);
    }

    @Override
    public void completing(DelegateExecution execution, DelegateExecution subProcessInstance) throws Exception {

    }

    @Override
    public void completed(DelegateExecution execution) throws Exception {
        // only control flow. no sub process instance data available
        leave(execution);
    }

}
