package ws.slink.spm.sr.parser.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ws.slink.spm.model.RFC;
import ws.slink.spm.tools.DataTools;
import ws.slink.spm.tools.ExcelTools;

public class RFCParser {

	static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(RFCParser.class);

	private static final String STR_SEPARATOR = "!!!!--@@@@@@@--!!!!";

	protected final String dateFormat;

	public RFCParser(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public List<RFC> parse(String fileName) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), 2);
	}
	public List<RFC> parse(String fileName, int skipLines) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), skipLines);
	}

	protected List<RFC> readInputFile(Optional<Workbook> optWorkbook, int skipLines) {
		List<RFC> results = new ArrayList<>();
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
	
	protected Optional<RFC> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		return RFC.makeRFC(fields, dateFormat);
	}
	
	public static void main(String[] args) {
		String df = "M/d/y h:m:s a";
		RFCParser rp = new RFCParser(df);
		rp.parse("D:\\work\\dev\\spm\\data\\rfc_2016_test.xlsx").forEach(System.out::println);
	}
	
}
