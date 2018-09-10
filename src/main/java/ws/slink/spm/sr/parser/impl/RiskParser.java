package ws.slink.spm.sr.parser.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ws.slink.spm.model.Risk;
import ws.slink.spm.tools.DataTools;
import ws.slink.spm.tools.ExcelTools;

public class RiskParser {

	static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(RiskParser.class);

//	private static final String STR_SEPARATOR 	= "!!!!--@@@@@@@--!!!!";
	private static final String STR_SEPARATOR 	= "  '''STR SEPARATOR''' ";
	private static final int    STR_COLUMNS		= 50;
	
	protected final String dateFormat1, dateFormat2;

	public RiskParser(String dateFormat1, String dateFormat2) {
		this.dateFormat1 = dateFormat1;
		this.dateFormat2 = dateFormat2;
	}

	public List<Risk> parse(String fileName) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), 2);
	}
	public List<Risk> parse(String fileName, int skipLines) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), skipLines);
	}

	protected List<Risk> readInputFile(Optional<Workbook> optWorkbook, int skipLines) {
		List<Risk> results = new ArrayList<>();
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
					// get one row as string with given item separator and given columns amount
					String line = DataTools.getLine(nextRow.cellIterator(), STR_COLUMNS, STR_SEPARATOR);
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

	protected Optional<Risk> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
//		Arrays.asList(fields).stream().forEach(x -> System.out.println("FIELD: " + x));
		return Risk.makeRisk(fields, dateFormat1, dateFormat2);
	}
	
	public static void main(String[] args) {
		String df1 = "y-M-d";
		String df2 = "y-M-d h:m:s";
		RiskParser rp = new RiskParser(df1, df2);
//		rp.parse("D:\\work\\dev\\spm\\data\\risks-10.xlsx", 1).forEach(x -> System.out.println(x.toDetailedString()));
		rp.parse("D:\\work\\dev\\spm\\data\\risks-10.xlsx", 1).forEach(x -> System.out.println(x));
	}


}
