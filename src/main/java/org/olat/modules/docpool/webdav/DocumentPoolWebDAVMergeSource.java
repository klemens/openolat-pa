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
package org.olat.modules.docpool.webdav;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.webdav.manager.WebDAVMergeSource;
import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.core.util.vfs.callbacks.DefaultVFSSecurityCallback;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.docpool.manager.DocumentPoolNotificationsHandler;
import org.olat.modules.docpool.ui.DocumentPoolMainController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyTreeBuilder;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.ui.component.TaxonomyVFSSecurityCallback;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class DocumentPoolWebDAVMergeSource extends WebDAVMergeSource {
	
	private final IdentityEnvironment identityEnv;
	
	private final DocumentPoolModule docPoolModule;
	private final TaxonomyService taxonomyService;
	private final DocumentPoolNotificationsHandler notificationsHandler;
	
	public DocumentPoolWebDAVMergeSource(IdentityEnvironment identityEnv) {
		super(identityEnv.getIdentity());
		this.identityEnv = identityEnv;
		docPoolModule = CoreSpringFactory.getImpl(DocumentPoolModule.class);
		taxonomyService = CoreSpringFactory.getImpl(TaxonomyService.class);
		notificationsHandler = CoreSpringFactory.getImpl(DocumentPoolNotificationsHandler.class);
	}
	
	@Override
	protected List<VFSContainer> loadMergedContainers() {
		List<VFSContainer> containers = new ArrayList<>();
		
		String taxonomyTreeKey = docPoolModule.getTaxonomyTreeKey();
		if(StringHelper.containsNonWhitespace(taxonomyTreeKey)) {
			Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(new Long(taxonomyTreeKey)));
			if(taxonomy != null) {
				String  templatesDir = Util.createPackageTranslator(DocumentPoolMainController.class, identityEnv.getLocale())
					.translate("document.pool.templates");
				TaxonomyTreeBuilder builder = new TaxonomyTreeBuilder(taxonomy, identityEnv.getIdentity(), null,
						identityEnv.getRoles().isOLATAdmin(), templatesDir);
				TreeModel model = builder.buildTreeModel();
				TreeNode rootNode = model.getRootNode();
				for(int i=0; i<rootNode.getChildCount(); i++) {
					VFSContainer container = loadRecursiveMergedContainers(taxonomy, rootNode.getChildAt(i));
					if(container != null) {
						containers.add(container);
					}
				}
			}
		}
		return containers;
	}
	
	private VFSContainer loadRecursiveMergedContainers(Taxonomy taxonomy, INode node) {
		VFSContainer container = null;
		if(node instanceof TaxonomyTreeNode) {
			TaxonomyTreeNode taxonomyNode = (TaxonomyTreeNode)node;
			String name = RequestUtil.normalizeFilename(taxonomyNode.getTitle());
			VirtualContainer levelContainer = new VirtualContainer(name);
			levelContainer.setLocalSecurityCallback(new DefaultVFSSecurityCallback());
			if(taxonomyNode.getTaxonomyLevel() != null && taxonomyNode.isDocumentsLibraryEnabled()
					&& taxonomyNode.isCanRead()) {
				// the real thing
				VFSContainer documents = taxonomyService.getDocumentsLibrary(taxonomyNode.getTaxonomyLevel());
				SubscriptionContext subscriptionCtx = notificationsHandler.getTaxonomyDocumentsLibrarySubscriptionContext();
				TaxonomyVFSSecurityCallback secCallback = new TaxonomyVFSSecurityCallback(taxonomyNode, subscriptionCtx);
				documents.setLocalSecurityCallback(secCallback);
				VFSContainer namedContainer = new NamedContainerImpl("_documents", documents);
				levelContainer.addItem(namedContainer);
			}
			
			for(int i=0; i<node.getChildCount(); i++) {
				VFSContainer childContainer = loadRecursiveMergedContainers(taxonomy, node.getChildAt(i));
				if(childContainer != null) {
					levelContainer.addItem(childContainer);
				}
			}
			container = levelContainer;
		}
		return container;
	}
}