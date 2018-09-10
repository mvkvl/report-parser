package ws.slink.spm.sr.parser.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ws.slink.spm.model.IBMS;
import ws.slink.spm.tools.DataTools;
import ws.slink.spm.tools.ExcelTools;

public class IBMSParser {
	
	static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(RFCParser.class);
	
	private static final String STR_SEPARATOR = "!!!!--@@@@@@@--!!!!";

	protected final String dateFormat;

	public IBMSParser(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public List<IBMS> parse(String fileName) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), 1);
	}
	public List<IBMS> parse(String fileName, int skipLines) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), skipLines);
	}

	protected List<IBMS> readInputFile(Optional<Workbook> optWorkbook, int skipLines) {
		List<IBMS> results = new ArrayList<>();
		optWorkbook.ifPresent( workbook -> {
			try {
				Sheet firstSheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = firstSheet.iterator();
				// skip starting rows
				for (int i = 0; i < skipLines; i++) 
					if (iterator.hasNext())
						iterator.next();
				while (iterator.hasNext()) {
					Row nextRow = iterator.next();
					// get one row as string with given item separator
					String line = DataTools.getLine(nextRow.cellIterator(), STR_SEPARATOR);
					if (line.length() > 1)
						processLine(line, STR_SEPARATOR).ifPresent( rfc -> results.add(rfc) );					
				}
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return results;
	}
	
	protected Optional<IBMS> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		logger.debug("#FIELDS: " + fields.length);
		return IBMS.makeIBMS(fields, dateFormat);
	}

	public static void main(String[] args) {
		String dfs = "yyyy-MM-dd";
		DateFormat df = new SimpleDateFormat(dfs);
		IBMSParser rp = new IBMSParser(dfs);
		rp.parse("D:\\ibms.xlsx").stream()
		                         .filter(i -> !i.archiveStatus.equals("Invalid"))
								 .forEach(i -> System.out.println(i.archiveId + ", " + 
																  df.format(i.neUpdateDate) + ", " + 
																  df.format(i.archiveUpdateDate) + ", " +
																  i.archiveStatus + ", " +
		                                                          i.archiveName));
	}

	

}
