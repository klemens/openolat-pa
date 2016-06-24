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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderMetadataEditController extends FormBasicController {
	
	private static final Set<String> imageMimeTypes = new HashSet<String>();
	static {
		imageMimeTypes.add("image/gif");
		imageMimeTypes.add("image/jpg");
		imageMimeTypes.add("image/jpeg");
		imageMimeTypes.add("image/png");
	}
	
	private TextElement titleEl, summaryEl;
	private TextBoxListElement categoriesEl;
	
	private FileElement fileUpload;
	private static final int picUploadlimitKB = 5120;
	
	private Binder binder;
	private Map<String,String> categories = new HashMap<>();
	private Map<String,Category> categoriesMap = new HashMap<>();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;

	public BinderMetadataEditController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, (Binder)null);
		initForm(ureq);
	}
	
	public BinderMetadataEditController(UserRequest ureq, WindowControl wControl, Binder binder) {
		super(ureq, wControl);
		this.binder = binder;
		if(binder != null) {
			List<Category> tags = portfolioService.getCategories(binder);
			for(Category tag:tags) {
				categories.put(tag.getName(), tag.getName());
				categoriesMap.put(tag.getName(), tag);
			}
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = binder == null ? null : binder.getTitle();
		titleEl = uifactory.addTextElement("title", "title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		String summary = binder == null ? null : binder.getSummary();
		summaryEl = uifactory.addTextAreaElement("summary", "summary", 4096, 4, 60, false, summary, formLayout);
		summaryEl.setPlaceholderKey("summary.placeholder", null);
		
		fileUpload = uifactory.addFileElement(getWindowControl(), "file", "fileupload",formLayout);			
		fileUpload.setPreview(ureq.getUserSession(), true);
		fileUpload.addActionListener(FormEvent.ONCHANGE);
		fileUpload.setDeleteEnabled(true);
		fileUpload.limitToMimeType(imageMimeTypes, null, null);
		fileUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		if(binder != null) {
			File posterImg = portfolioService.getPosterImageFile(binder);
			if(posterImg != null) {
				fileUpload.setInitialFile(posterImg);
			}
		}
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		//Map<String, String> allUsersTags = ePFMgr.getUsersMostUsedTags(getIdentity(), 50);
		//categoriesEl.setAutoCompleteContent(allUsersTags);
		categoriesEl.setAllowDuplicates(false);
		
		// owners 		
		StringBuilder sb = new StringBuilder();
		if(binder == null || binder.getKey() == null) {
			sb.append(userManager.getUserDisplayName(getIdentity()));
		} else {
			List<Identity> owners = portfolioService.getMembers(binder, GroupRoles.owner.name());
			for(Identity owner:owners) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(userManager.getUserDisplayName(owner));
			}
		}
		uifactory.addStaticTextElement("author", "author", sb.toString(), formLayout);
		
		// access
		uifactory.addStaticTextElement("access", "access", "[private]", formLayout);
		
		// template name
		String templateName;
		if(binder != null && binder.getTemplate() != null) {
			templateName = binder.getTemplate().getTitle();
		} else {
			templateName = translate("template.none");
		}
		uifactory.addStaticTextElement("template", "template", templateName, formLayout);
		
		// portfolio task
		String courseName;
		if(binder != null && binder.getEntry() != null) {
			courseName = binder.getEntry().getDisplayname();
		} else {
			courseName = translate("portfoliotask.none");
		}
		uifactory.addStaticTextElement("portfolio-task", "portfoliotask", courseName, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		
		if(binder != null && binder.getKey() != null) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		} else {
			uifactory.addFormSubmitButton("create.binder", buttonsCont);
		}
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public Binder getBinder() {
		return binder;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(binder == null) {
			String title = titleEl.getValue();
			String summary = summaryEl.getValue();
			
			String imagePath = null;
			if(fileUpload.getUploadFile() != null) {
				imagePath = portfolioService.addPosterImageForBinder(fileUpload.getUploadFile(), fileUpload.getUploadFileName());
			}
			binder = portfolioService.createNewBinder(title, summary, imagePath, getIdentity());
		} else {
			binder = portfolioService.getBinderByKey(binder.getKey());
			if(fileUpload.getUploadFile() != null) {
				String imagePath = portfolioService.addPosterImageForBinder(fileUpload.getUploadFile(), fileUpload.getUploadFileName());
				binder.setImagePath(imagePath);
			} else if(fileUpload.getInitialFile() == null) {
				binder.setImagePath(null);
				portfolioService.removePosterImage(binder);
			}
			binder.setTitle(titleEl.getValue());
			binder.setSummary(summaryEl.getValue());
			binder = portfolioService.updateBinder(binder);
		}
		
		List<String> updatedCategories = categoriesEl.getValueList();
		portfolioService.updateCategories(binder, updatedCategories);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (fileUpload == source) {
			if (event instanceof FileElementEvent) {
				String cmd = event.getCommand();
				if (FileElementEvent.DELETE.equals(cmd)) {
					if(fileUpload.getUploadFile() != null) {
						fileUpload.reset();
					} else if(fileUpload.getInitialFile() != null) {
						fileUpload.setInitialFile(null);
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}
