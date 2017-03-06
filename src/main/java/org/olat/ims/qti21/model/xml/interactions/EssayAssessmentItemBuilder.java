/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.model.xml.interactions;

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultItemBody;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultOutcomeDeclarations;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendExtendedTextInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createExtendedTextResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EssayAssessmentItemBuilder extends LobAssessmentItemBuilder {

	private ExtendedTextInteraction extendedTextInteraction;
	
	public EssayAssessmentItemBuilder(QtiSerializer qtiSerializer) {
		super(createAssessmentItem(), qtiSerializer);
	}
	
	public EssayAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem() {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.essay, "Essay");
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		ResponseDeclaration responseDeclaration = createExtendedTextResponseDeclaration(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
	
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		appendExtendedTextInteraction(itemBody, responseDeclarationId);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}
	
	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.essay;
	}
	
	@Override
	public void extract() {
		super.extract();
		extractExtendedTextInteraction();
	}
	
	private void extractExtendedTextInteraction() {
		StringOutput sb = new StringOutput();
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		for(Block block:blocks) {
			if(block instanceof ExtendedTextInteraction) {
				extendedTextInteraction = (ExtendedTextInteraction)block;
				responseIdentifier = extendedTextInteraction.getResponseIdentifier();
				break;
			} else {
				qtiSerializer.serializeJqtiObject(block, new StreamResult(sb));
			}
		}
		question = sb.toString();
	}
	
	public String getPlaceholder() {
		return extendedTextInteraction.getPlaceholderText();
	}
	
	public void setPlaceholder(String placeholder) {
		if(StringHelper.containsNonWhitespace(placeholder)) {
			extendedTextInteraction.setPlaceholderText(placeholder);
		} else {
			extendedTextInteraction.setPlaceholderText(null);
		}
	}
	
	public Integer getExpectedLength() {
		return extendedTextInteraction.getExpectedLength();
	}
	
	public void setExpectedLength(Integer length) {
		extendedTextInteraction.setExpectedLength(length);
	}
	
	public Integer getExpectedLines() {
		return extendedTextInteraction.getExpectedLines();
	}
	
	public void setExpectedLines(Integer lines) {
		extendedTextInteraction.setExpectedLines(lines);
	}
	
	public ExtendedTextInteraction getExtendedTextInteraction() {
		return extendedTextInteraction;
	}
	
	public Integer getMinStrings() {
		return extendedTextInteraction.getMinStrings();
	}
	
	public void setMinStrings(Integer minStrings) {
		extendedTextInteraction.setMinStrings(minStrings);
	}
	
	public Integer getMaxStrings() {
		return extendedTextInteraction.getMaxStrings();
	}
	
	public void setMaxStrings(Integer maxStrings) {
		extendedTextInteraction.setMaxStrings(maxStrings);
	}
	
	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		ResponseDeclaration responseDeclaration =
				createExtendedTextResponseDeclaration(assessmentItem, responseIdentifier);
		assessmentItem.getResponseDeclarations().add(responseDeclaration);
	}
	
	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
		
		//add interaction
		blocks.add(extendedTextInteraction);
	}
}