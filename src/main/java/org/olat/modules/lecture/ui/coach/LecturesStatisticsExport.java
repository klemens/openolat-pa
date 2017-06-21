package org.olat.modules.lecture.ui.coach;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 21 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesStatisticsExport extends OpenXMLWorkbookResource {
	
	private static final OLog log = Tracing.createLoggerFor(LecturesStatisticsExport.class);
	
	private final Translator translator;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<LectureBlockIdentityStatistics> statistics;
	
	public LecturesStatisticsExport(List<LectureBlockIdentityStatistics> statistics,
			List<UserPropertyHandler> userPropertyHandlers, boolean isAdministrativeUser, Translator translator) {
		super(label());
		this.translator = translator;
		this.statistics = statistics;
		this.isAdministrativeUser = isAdministrativeUser;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	private static final String label() {
		return "ExportLectureStatistics_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 2)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(1);
			addHeadersAggregated(exportSheet);
			addContentAggregated(exportSheet);
			
			exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(1);
			addHeadersDetailled(exportSheet);
			addContentDetailled(exportSheet);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void addHeadersAggregated(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		pos = addHeadersUser(headerRow, pos);
		pos = addHeadersStatistics(headerRow, pos);
	}
	
	private void addHeadersDetailled(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		pos = addHeadersUser(headerRow, pos);
		headerRow.addCell(pos++, translator.translate("table.header.external.ref"));
		headerRow.addCell(pos++, translator.translate("table.header.entry"));
		pos = addHeadersStatistics(headerRow, pos);
	}
	
	private int addHeadersUser(Row headerRow, int pos) {
		if(isAdministrativeUser) {
			headerRow.addCell(pos++, translator.translate("table.header.username"));
		}
		
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			headerRow.addCell(pos++, translator.translate("form.name." + userPropertyHandler.getName()));
		}
		return pos;
	}

	private int addHeadersStatistics(Row headerRow, int pos) {
		headerRow.addCell(pos++, translator.translate("table.header.planned.lectures"));
		headerRow.addCell(pos++, translator.translate("table.header.attended.lectures"));
		headerRow.addCell(pos++, translator.translate("table.header.absent.lectures"));
		headerRow.addCell(pos++, translator.translate("table.header.authorized.absence"));
		return pos;
	}
	
	private void addContentAggregated(OpenXMLWorksheet exportSheet) {
		List<LectureBlockIdentityStatistics> aggregatedStatistics = LecturesSearchController.groupByIdentity(statistics);
		for(LectureBlockIdentityStatistics statistic:aggregatedStatistics) {
			Row row = exportSheet.newRow();
			
			int pos = 0;
			pos = addContentUser(statistic, row, pos);
			pos = addContentStatistics(statistic, row, pos);
		}
	}

	private void addContentDetailled(OpenXMLWorksheet exportSheet) {

		for(LectureBlockIdentityStatistics statistic:statistics) {
			Row row = exportSheet.newRow();
			
			int pos = 0;
			pos = addContentUser(statistic, row, pos);
			row.addCell(pos++, statistic.getExternalRef());
			row.addCell(pos++, statistic.getDisplayName());
			pos = addContentStatistics(statistic, row, pos);
		}
	}

	private int addContentUser(LectureBlockIdentityStatistics statistic, Row row, int pos) {
		if(isAdministrativeUser) {
			row.addCell(pos++, statistic.getIdentityName());
		}
		
		int count = 0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			row.addCell(pos++, statistic.getIdentityProp(count++));
		}
		return pos;
	}

	private int addContentStatistics(LectureBlockIdentityStatistics statistic, Row row, int pos) {
		row.addCell(pos++, positive(statistic.getTotalPersonalPlannedLectures()), null);
		row.addCell(pos++, positive(statistic.getTotalAttendedLectures()), null);
		row.addCell(pos++, positive(statistic.getTotalAbsentLectures()), null);
		row.addCell(pos++, positive(statistic.getTotalAuthorizedAbsentLectures()), null);
		return pos;
	}
	
	private long positive(long val) {
		return val < 0 ? 0 : val;
	}
}
