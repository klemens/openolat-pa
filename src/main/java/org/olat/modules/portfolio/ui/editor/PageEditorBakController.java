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
package org.olat.modules.portfolio.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.HTMLPart;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.MediaCenterController;
import org.olat.modules.portfolio.ui.PageRunController;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.olat.modules.portfolio.ui.media.CollectFileMediaController;
import org.olat.modules.portfolio.ui.media.CollectImageMediaController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorBakController extends FormBasicController {

	private FormLink addHtmlLink, addMediaLink, addFileLink, addImageLink;
	private List<EditorFragment> fragments = new ArrayList<>();

	private CloseableModalController cmc;
	private MediaCenterController mediaListCtrl;
	private CollectFileMediaController fileUploadCtrl;
	private CollectImageMediaController imageUploadCtrl;
	
	private Page page;
	private int counter = 0;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageEditorBakController(UserRequest ureq, WindowControl wControl, Page page) {
		super(ureq, wControl, "page_editor_bak");
		this.page = page;
		setTranslator(Util.createPackageTranslator(PageRunController.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addHtmlLink = uifactory.addFormLink("add.html", "add.html", "add.html", null, formLayout, Link.BUTTON);
		addHtmlLink.setIconLeftCSS("o_icon o_icon_add_html");

		addMediaLink = uifactory.addFormLink("add.media", "add.media", "add.media", null, formLayout, Link.BUTTON);
		addMediaLink.setIconLeftCSS("o_icon o_icon_portfolio");
		
		addFileLink = uifactory.addFormLink("add.file", "add.file", "add.file", null, formLayout, Link.BUTTON);
		addFileLink.setIconLeftCSS("o_icon o_icon_portfolio");
		
		addImageLink = uifactory.addFormLink("add.image", "add.image", "add.image", null, formLayout, Link.BUTTON);
		addImageLink.setIconLeftCSS("o_icon o_icon_portfolio");
			
		uifactory.addFormSubmitButton("save", formLayout);

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.getFormItemComponent().contextPut("pageTitle", page.getTitle());
		}
	}
	
	private void loadModel(UserRequest ureq) {
		List<PagePart> parts = portfolioService.getPageParts(page);
		List<EditorFragment> newFragments = new ArrayList<>(parts.size());
		for(PagePart part:parts) {
			EditorFragment fragment = createFragment(ureq, part);
			if(fragment != null) {
				newFragments.add(fragment);
			}
		}
		fragments = newFragments;
		flc.getFormItemComponent().contextPut("fragments", newFragments);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addHtmlLink == source) {
			doAddHTMLFragment(ureq);
		} else if(addMediaLink == source) {
			doOpenMediaBrowser(ureq);
		} else if(addImageLink == source) {
			doAddImageMedia(ureq);
		} else if(addFileLink == source) {
			doAddFileMedia(ureq);
		} else if(source.getUserObject() instanceof HTMLEditorFragment) {
			HTMLEditorFragment fragment = (HTMLEditorFragment)source.getUserObject();
			if(fragment.getFormItem() == source) {
				String htmlVal = fragment.getFormItem().getValue();
				PagePart part = fragment.getPart();
				part.setContent(htmlVal);
				fragment.setPart(portfolioService.updatePart(part));
				flc.setDirty(true);	
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//ok -> set container dirty
		super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(EditorFragment fragment:fragments) {
			if(fragment instanceof HTMLEditorFragment) {
				HTMLEditorFragment htmlFragment = (HTMLEditorFragment)fragment;
				String htmlVal = htmlFragment.getFormItem().getValue();
				String currentHtmlVal = fragment.getPart().getContent();
				
				if((currentHtmlVal == null && StringHelper.containsNonWhitespace(htmlVal))
						|| (htmlVal == null && StringHelper.containsNonWhitespace(currentHtmlVal))
						|| (currentHtmlVal != null && !currentHtmlVal.equals(htmlVal))) {
					PagePart part = fragment.getPart();
					part.setContent(htmlVal);
					htmlFragment.setPart(portfolioService.updatePart(part));
				}
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(mediaListCtrl == source) {
			if(event instanceof MediaSelectionEvent) {
				MediaSelectionEvent mse = (MediaSelectionEvent)event;
				if(mse.getMedia() != null) {
					doAddMedia(ureq, mse.getMedia());
				}
				loadModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(fileUploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(fileUploadCtrl.getMediaReference() != null) {
					doAddMedia(ureq, fileUploadCtrl.getMediaReference());
				}
				loadModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(imageUploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(imageUploadCtrl.getMediaReference() != null) {
					doAddMedia(ureq, imageUploadCtrl.getMediaReference());
				}
				loadModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(imageUploadCtrl);
		removeAsListenerAndDispose(fileUploadCtrl);
		removeAsListenerAndDispose(mediaListCtrl);
		removeAsListenerAndDispose(cmc);
		imageUploadCtrl = null;
		fileUploadCtrl = null;
		mediaListCtrl = null;
		cmc = null;
	}
	
	private void doAddFileMedia(UserRequest ureq) {
		if(fileUploadCtrl != null) return;
		
		fileUploadCtrl = new CollectFileMediaController(ureq, getWindowControl());
		listenTo(fileUploadCtrl);
		
		String title = translate("add.media");
		cmc = new CloseableModalController(getWindowControl(), null, fileUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddImageMedia(UserRequest ureq) {
		if(imageUploadCtrl != null) return;
		
		imageUploadCtrl = new CollectImageMediaController(ureq, getWindowControl());
		listenTo(imageUploadCtrl);
		
		String title = translate("add.image");
		cmc = new CloseableModalController(getWindowControl(), null, imageUploadCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenMediaBrowser(UserRequest ureq) {
		if(mediaListCtrl != null) return;
		
		mediaListCtrl = new MediaCenterController(ureq, getWindowControl());
		listenTo(mediaListCtrl);
		
		String title = translate("add.media");
		cmc = new CloseableModalController(getWindowControl(), null, mediaListCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddMedia(UserRequest ureq, Media media) {
		MediaPart part = new MediaPart();
		part.setMedia(media);
		part = portfolioService.appendNewPagePart(page, part);
		EditorFragment fragment = createFragment(ureq, part);
		fragments.add(fragment);
		flc.setDirty(true);
	}
	
	private void doAddHTMLFragment(UserRequest ureq) {
		String content = "<p>Hello world</p>";
		HTMLPart htmlPart = new HTMLPart();
		htmlPart.setContent(content);
		htmlPart = portfolioService.appendNewPagePart(page, htmlPart);
		EditorFragment fragment = createFragment(ureq, htmlPart);
		fragments.add(fragment);
		flc.setDirty(true);
	}
	
	private EditorFragment createFragment(UserRequest ureq, PagePart part) {
		if(part instanceof HTMLPart) {
			HTMLPart htmlPart = (HTMLPart)part;
			HTMLEditorFragment editorFragment = new HTMLEditorFragment(htmlPart);
			
			String cmpId = "html-" + (++counter);
			String content = htmlPart.getContent();
			RichTextElement htmlItem = uifactory.addRichTextElementForStringDataCompact(cmpId, null, content, 8, 80, null, flc, ureq.getUserSession(), getWindowControl());
			//htmlItem.getEditorConfiguration().setInline(true);
			editorFragment.setFormItem(htmlItem);
			return editorFragment;
		} else if (part instanceof MediaPart) {
			MediaPart mediaPart = (MediaPart)part;
			MediaHandler handler = portfolioService.getMediaHandler(mediaPart.getMedia().getType());
			String cmpId = "media-" + (++counter);
			Controller mediaCtrl = handler.getMediaController(ureq, getWindowControl(), mediaPart.getMedia());
			MediaEditorFragment fragment = new MediaEditorFragment(mediaPart, cmpId, mediaCtrl);
			flc.getFormItemComponent().put(cmpId, mediaCtrl.getInitialComponent());
			return fragment;
		}
		return null;
		
	}
	
	public static class HTMLEditorFragment implements EditorFragment {
		
		private PagePart part;
		private RichTextElement formItem;
		
		public HTMLEditorFragment(HTMLPart part) {
			this.part = part;
		}

		@Override
		public PagePart getPart() {
			return part;
		}
		
		public void setPart(PagePart part) {
			this.part = part;
		}

		public RichTextElement getFormItem() {
			return formItem;
		}

		public void setFormItem(RichTextElement formItem) {
			this.formItem = formItem;
			formItem.setUserObject(this);
		}

		@Override
		public String getComponentName() {
			return formItem.getComponent().getComponentName();
		}
	}
	
	public static class MediaEditorFragment implements EditorFragment {
		
		private PagePart part;
		private String cmpName;
		private Controller controller;
		
		public MediaEditorFragment(MediaPart part, String cmpName, Controller controller) {
			this.part = part;
			this.cmpName = cmpName;
			this.controller = controller;
		}

		@Override
		public PagePart getPart() {
			return part;
		}
		
		public void setPart(PagePart part) {
			this.part = part;
		}

		@Override
		public String getComponentName() {
			return cmpName;
		}
		
		public Controller getController() {
			return controller;
		}
	}
	
	public interface EditorFragment {
		
		public PagePart getPart();
		
		public void setPart(PagePart part);
		
		public String getComponentName();
	}
}
