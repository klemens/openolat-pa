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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.HTMLPart;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageController extends FormBasicController implements TooledController {

	private Link editLink;
	protected final TooledStackedPanel stackPanel;
	private List<HTMLFragment> fragments = new ArrayList<>();
	
	private PageEditorController editCtrl;
	
	private Page page;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Page page) {
		super(ureq, wControl, "page_content");
		this.page = page;
		this.stackPanel = stackPanel;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	public void initTools() {
		editLink = LinkFactory.createToolLink("edit.page", translate("edit.page"), this);
		editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
		stackPanel.addTool(editLink, Align.left);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.getFormItemComponent().contextPut("pageTitle", page.getTitle());
		}
	}
	
	private void loadModel() {
		List<PagePart> parts = portfolioService.getPageParts(page);
		List<HTMLFragment> newFragments = new ArrayList<>(parts.size());
		for(PagePart part:parts) {
			HTMLFragment fragment = createFragment(part);
			if(fragment != null) {
				newFragments.add(fragment);
			}
		}
		fragments = newFragments;
		flc.getFormItemComponent().contextPut("fragments", fragments);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			stackPanel.popUpToController(this);
			loadModel();
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			doEditPage(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//ok -> set container dirty
		super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEditPage(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);
		
		editCtrl = new PageEditorController(ureq, getWindowControl(), page);
		listenTo(editCtrl);
		
		stackPanel.pushController("Edit", editCtrl);
	}
	
	private HTMLFragment createFragment(PagePart part) {
		if(part instanceof HTMLPart) {
			HTMLPart htmlPart = (HTMLPart)part;
			HTMLFragment editorFragment = new HTMLFragment(htmlPart);
			return editorFragment;
		}
		return null;
		
	}
	
	public static class HTMLFragment {
		
		private PagePart part;
		
		public HTMLFragment(HTMLPart part) {
			this.part = part;
		}
		
		public String getContent() {
			return part.getContent();
		}

		public PagePart getPart() {
			return part;
		}
		
		public void setPart(PagePart part) {
			this.part = part;
		}
	}
}