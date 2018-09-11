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
package org.flowable.bpmn.converter;

import org.flowable.bpmn.converter.util.BpmnXMLUtil;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.CaseTask;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class CaseTaskXMLConverter extends BaseBpmnXMLConverter {

    @Override
    protected Class<? extends BaseElement> getBpmnElementType() {
        return CaseTask.class;
    }

    @Override
    protected BaseElement convertXMLToElement(XMLStreamReader xtr, BpmnModel model) throws Exception {

        CaseTask caseTask = new CaseTask();
        String caseRef = BpmnXMLUtil.getAttributeValue(ATTRIBUTE_CASE_REF, xtr);

        caseTask.setCaseRef(caseRef);

        return caseTask;
    }

    @Override
    protected String getXMLElementName() {
        return ELEMENT_TASK_CASE;
    }

    @Override
    protected void writeAdditionalAttributes(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {

        CaseTask caseTask = (CaseTask) element;
        BpmnXMLUtil.writeQualifiedAttribute(ATTRIBUTE_CASE_REF, caseTask.getCaseRef(), xtw);
    }

    @Override
    protected void writeAdditionalChildElements(BaseElement element, BpmnModel model, XMLStreamWriter xtw) throws Exception {

    }
}
