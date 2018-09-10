package ws.slink.spm.sr.parser.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.SRNote;
import ws.slink.spm.sr.parser.SRParser;
import ws.slink.spm.tools.DataTools;
import ws.slink.spm.tools.ExcelTools;

public abstract class AbstractSRParser implements SRParser {

	static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AbstractSRParser.class);
	
	private static final String STR_SEPARATOR = "!!!!--@@@@@@@--!!!!";
	
	protected final String dateFormat;
	
	public AbstractSRParser(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	@Override
	public List<SR> parse(String fileName) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), 2);
	}
	public List<SR> parse(String fileName, int skipLines) {
		return readInputFile(ExcelTools.getExcelWorkbook(fileName), skipLines);
	}
	@Override
	public List<SR> parse(String fileName, String encoding) {
		throw new RuntimeException("Method not implemented");
	}
	@Override
	public List<SR> parse(String fileName, String encoding, String fieldSeparator){
		throw new RuntimeException("Method not implemented");
	}

	@Override
	public List<SR> parseWithNotes(String fileName) {
		return readInputFileWithNotes(ExcelTools.getExcelWorkbook(fileName), 2);
	}
	public List<SR> parseWithNotes(String fileName, int skipLines) {
		return readInputFileWithNotes(ExcelTools.getExcelWorkbook(fileName), skipLines);
	}

	protected List<SR> readInputFile(Optional<Workbook> optWorkbook, int skipLines) {
		List<SR> results = new ArrayList<>();
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
						processLine(line, STR_SEPARATOR).ifPresent( sr -> results.add(sr) );					
				}
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return results;
	}
	protected List<SR> readInputFileWithNotes(Optional<Workbook> optWorkbook, int skipLines) {
		List<SR> results = new ArrayList<>();
		optWorkbook.ifPresent( workbook -> {
			try {
				Sheet firstSheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = firstSheet.iterator();
				// skip starting rows
				for (int i = 0; i < skipLines; i++) 
					if (iterator.hasNext())
						iterator.next();
				results.addAll(processExcelDataWithNotes(iterator));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					workbook.close();
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		return results;
	}
	private List<SR> processExcelDataWithNotes(Iterator<Row> iterator) {
		List<SR> results = new ArrayList<>();
		
		if (!iterator.hasNext()) return results;

		Set<SRNote> notes = new HashSet<>();
		String firstLine   = null;

		Row row            = iterator.next();
		firstLine          = DataTools.getLine(row.cellIterator(), STR_SEPARATOR);
		
		while (iterator.hasNext()) {
			String [] firstFields;
			if (firstLine != null && firstLine.length() > 1) {
				firstFields = firstLine.split(STR_SEPARATOR);

				// make final SR
				if (!iterator.hasNext()) {
					notes.add(new SRNote(firstFields));
					processLine(firstLine, STR_SEPARATOR, new ArrayList<SRNote>(notes)).ifPresent(sr -> results.add(sr));
				} else {
					// get second row
					Row next               = iterator.next();	
					String secondLine      = DataTools.getLine(next.cellIterator(), STR_SEPARATOR);
					String [] secondFields = secondLine.split(STR_SEPARATOR);
					
					if (!secondFields[0].isEmpty()) {
						notes.add(new SRNote(firstFields));
						processLine(firstLine, STR_SEPARATOR, new ArrayList<SRNote>(notes)).ifPresent(sr -> results.add(sr));
						firstLine   = secondLine;
						notes       = new HashSet<>();
						firstFields = firstLine.split(STR_SEPARATOR);
						notes.add(new SRNote(firstFields));
					} else {
						notes.add(new SRNote(secondFields));
					}
				}
			}
		}
		// process last row
		processLine(firstLine, STR_SEPARATOR, new ArrayList<SRNote>(notes)).ifPresent(sr -> results.add(sr));
		return results;
	}	
	protected abstract Optional<SR> processLine(String line, String delimiter);
	protected abstract Optional<SR> processLine(String line, String delimiter, List<SRNote> notes);
}
