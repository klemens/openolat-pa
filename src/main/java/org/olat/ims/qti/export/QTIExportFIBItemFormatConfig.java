/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.export;

import org.olat.core.logging.OLATRuntimeException;

public class QTIExportFIBItemFormatConfig implements QTIExportItemFormatConfig {
	private boolean responseCols;
	private boolean pointCol;
	private boolean timeCols;
	
	public QTIExportFIBItemFormatConfig(boolean resCols, boolean pointCol, boolean timeCols){
		this.responseCols = resCols;
		this.pointCol = pointCol;
		this.timeCols = timeCols;
	}

	public boolean hasResponseCols() {
		return responseCols;
	}

	public boolean hasPointCol() {
		return pointCol;
	}

	public boolean hasTimeCols() {
		return timeCols;
	}

	public boolean hasPositionsOfResponsesCol() {
		return false;
	}

	public void setPointCol(boolean pointColConfigured) {
		this.pointCol = pointColConfigured;
	}

	public void setResponseCols(boolean responseColsConfigured) {
		this.responseCols = responseColsConfigured;
	}

	public void setTimeCols(boolean timeColsConfigured) {
		this.timeCols = timeColsConfigured;
	}

	public void setPositionsOfResponsesCol(boolean positionsOfResponsesColConfigured) {
		throw new OLATRuntimeException(null, "responseCols is not configureable for QTIType FIB", null);
	}
}
