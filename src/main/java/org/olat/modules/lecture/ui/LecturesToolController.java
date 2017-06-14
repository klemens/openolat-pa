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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 13 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesToolController extends BasicController implements BreadcrumbPanelAware {
	
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link teacherLink, participantLink;
	
	private final TeacherToolOverviewController teacherOverviewCtrl;
	private final ParticipantLecturesOverviewController participantOverviewCtrl;
	
	public LecturesToolController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("user_tool");
		
		teacherOverviewCtrl = new TeacherToolOverviewController(ureq, getWindowControl());
		listenTo(teacherOverviewCtrl);
		boolean withTitle = teacherOverviewCtrl.getRowCount() == 0;
		participantOverviewCtrl = new ParticipantLecturesOverviewController(ureq, getWindowControl(), withTitle);
		listenTo(participantOverviewCtrl);
		
		if(teacherOverviewCtrl.getRowCount() > 0 && participantOverviewCtrl.getRowCount() > 0) {
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			teacherLink = LinkFactory.createLink("tool.teacher", mainVC, this);
			segmentView.addSegment(teacherLink, true);
			participantLink = LinkFactory.createLink("tool.participant", mainVC, this);
			segmentView.addSegment(participantLink, false);
			mainVC.put("segmentCmp", teacherOverviewCtrl.getInitialComponent());
		} else if(teacherOverviewCtrl.getRowCount() > 0) {
			mainVC.put("teacherView", teacherOverviewCtrl.getInitialComponent());
		} else if(participantOverviewCtrl.getRowCount() > 0) {
			mainVC.put("participantView", participantOverviewCtrl.getInitialComponent());
		} else {
			
		}
		putInitialPanel(mainVC);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		participantOverviewCtrl.setBreadcrumbPanel(stackPanel);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == teacherLink) {
				doOpenTeacherView();
			} else if (clickedLink == participantLink) {
				doOpenParticipantView();
			}
		}
	}
	
	private void doOpenTeacherView() {
		mainVC.put("segmentCmp", teacherOverviewCtrl.getInitialComponent());
	}
	
	private void doOpenParticipantView() {
		mainVC.put("segmentCmp", participantOverviewCtrl.getInitialComponent());
	}
}