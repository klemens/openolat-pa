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

package org.olat.core.commons.services.mark;

import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for MarkManager
 * 
 * <P>
 * Initial Date:  9 mars 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public abstract class MarkManager extends BasicManager {
	
	protected static MarkManager INSTANCE;

	public static MarkManager getInstance() {
		return INSTANCE;
	}

	public abstract List<Mark> getMarks(OLATResourceable ores, Identity identity, Collection<String> subPaths);
	
	public abstract List<Mark> getMarks(Identity identity, String resourceTypeName, Collection<String> subPaths);
	
	public abstract void filterMarks(Identity identity, String resourceTypeName, Collection<Long> resIds);
	
	public abstract boolean isMarked(OLATResourceable ores, Identity identity, String subPath);
	
	public abstract Mark setMark(OLATResourceable ores, Identity identity, String subPath, String businessPath);
	
	public abstract void moveMarks(OLATResourceable ores, String oldSubPath, String newSubPath);
	
	public abstract void removeMark(OLATResourceable ores, Identity identity, String subPath);
	
	public abstract void removeMark(Mark mark);
	
	public abstract void deleteMark(OLATResourceable ores);
	
	public abstract void deleteMark(OLATResourceable ores, String subPath);
	
	public abstract List<MarkResourceStat> getStats(OLATResourceable ores, List<String> subPaths, Identity identity);
}
