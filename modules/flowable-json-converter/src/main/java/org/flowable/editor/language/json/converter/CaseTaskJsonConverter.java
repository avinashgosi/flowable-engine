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
package org.flowable.editor.language.json.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.CaseTask;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.editor.language.json.model.ModelInfo;

import java.util.Map;

/**
 * @author Avinash Gosi
 */
public class CaseTaskJsonConverter extends BaseBpmnJsonConverter implements CaseModelAwareConverter {
    
    protected Map<String, String> caseModelMap;
    
    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_TASK_CASE, CaseTaskJsonConverter.class);
    }

    public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    }

    @Override
    protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
        // done in service task
    }

    @Override
    protected BaseElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
        CaseTask task = new CaseTask();
        task.setType("case");

        FieldExtension field = new FieldExtension();
        field.setFieldName("caseref");
        JsonNode caseModelReferenceNode = getProperty(PROPERTY_CASE_REFERENCE, elementNode);
        if (caseModelReferenceNode != null && caseModelReferenceNode.has("id") && !caseModelReferenceNode.get("id").isNull()) {
            String caseModelId = caseModelReferenceNode.get("id").asText();
            if (StringUtils.isNotEmpty(caseModelId)) {
                String caseModelKey = caseModelMap.get(caseModelId);
                field.setStringValue(caseModelKey);
                task.getFieldExtensions().add(field);
            }
        }

        return task;
    }

    @Override
    protected String getStencilId(BaseElement baseElement) {
        return STENCIL_TASK_CASE;
    }

    @Override
    public void setCaseModelMap(Map<String, String> caseModelMap) {
        this.caseModelMap = caseModelMap;
    }
}
