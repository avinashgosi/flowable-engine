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
package org.flowable.editor.language;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.CaseTask;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseTaskConverterTest extends AbstractConverterTest {

    Map<String, String> caseModelMap = new HashMap<>();

    @Before
    public void setup(){
        caseModelMap.put("d40eb926-c545-11e8-bd8b-ce2f7111fc6a", "case");
    }
    @Test
    public void convertJsonToModel() throws Exception {
        BpmnModel bpmnModel = readJsonFile();
        validateModel(bpmnModel);
    }

    @Test
    public void doubleConversionValidation() throws Exception {
        BpmnModel bpmnModel = readJsonFile();
        bpmnModel = convertToJsonAndBack(bpmnModel);
        validateModel(bpmnModel);
    }

    @Override
    protected BpmnModel readJsonFile() throws Exception {
        InputStream jsonStream = this.getClass().getClassLoader().getResourceAsStream(getResource());
        JsonNode modelNode = new ObjectMapper().readTree(jsonStream);
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode, null, null, caseModelMap);
        return bpmnModel;
    }

    @Override
    protected BpmnModel convertToJsonAndBack(BpmnModel bpmnModel) {
        ObjectNode modelNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode, null, null, caseModelMap);
        return bpmnModel;
    }

    @Override
    protected String getResource() {
        return "test.casetaskmodel.json";
    }

    private void validateModel(BpmnModel model) {
        FlowElement flowElement = model.getMainProcess().getFlowElement("casetask", true);
        assertNotNull(flowElement);
        assertTrue(flowElement instanceof ServiceTask);
        assertEquals("casetask", flowElement.getId());
        ServiceTask caseTask = (ServiceTask) flowElement;
        assertEquals("casetask", caseTask.getId());
        assertEquals("CaseTask", caseTask.getName());

        List<FieldExtension> fields = caseTask.getFieldExtensions();
        assertEquals(1, fields.size());
        FieldExtension field = fields.get(0);
        assertEquals("caseref", field.getFieldName());
        assertEquals("case", field.getStringValue());
    }
}
