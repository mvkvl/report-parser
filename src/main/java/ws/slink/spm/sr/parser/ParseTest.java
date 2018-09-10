package ws.slink.spm.sr.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import ws.slink.spm.db.DatastoreProvider;
import ws.slink.spm.model.Risk;
import ws.slink.spm.model.SR;
import ws.slink.spm.model.SRNote;
import ws.slink.spm.sr.parser.impl.CompleteSRParser;
import ws.slink.spm.sr.parser.impl.RiskParser;
import ws.slink.spm.tools.DataTools;
import ws.slink.spm.tools.Parameters;

public class ParseTest {

	final static String dataPath = "D:\\work\\dev\\spm\\data\\";
	final static String dataFile = "complete.xlsx";
//	final static String dataPath = "D:\\work\\1\\";
//	final static String dataFile = "risks.xlsx";
	final static String dateFormat = "M/d/y h:m:s a";
	final static String STR_SEPARATOR = " !@##@!";
	final static int skipLines = 2;
	
	static Optional<Workbook> optWorkbook;
	
	public ParseTest() {
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
				results.addAll(processExcelDataNotes(iterator));
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
	
	protected List<SR> processExcelData(Iterator<Row> iterator) {
		List<SR> results = new ArrayList<>();
		if (!iterator.hasNext()) return results;
		while (iterator.hasNext()) {
			String line = DataTools.getLine(iterator.next().cellIterator(), STR_SEPARATOR);
			if (line != null && line.length() > 1) {
				processLine(line, STR_SEPARATOR).ifPresent(sr -> results.add(sr));
			}
		}
		return results;
	}
	
	protected List<SR> processExcelDataNotes(Iterator<Row> iterator) {
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
					processLineNotes(firstLine, new ArrayList<SRNote>(notes), STR_SEPARATOR).ifPresent(sr -> results.add(sr));
				} else {
					// get second row
					Row next               = iterator.next();	
					String secondLine      = DataTools.getLine(next.cellIterator(), STR_SEPARATOR);
					String [] secondFields = secondLine.split(STR_SEPARATOR);
					
					if (!secondFields[0].isEmpty()) {
						notes.add(new SRNote(firstFields));
						processLineNotes(firstLine, new ArrayList<SRNote>(notes), STR_SEPARATOR).ifPresent(sr -> results.add(sr));
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
		processLineNotes(firstLine, new ArrayList<SRNote>(notes), STR_SEPARATOR).ifPresent(sr -> results.add(sr));
		return results;
	}
	
	protected Optional<SR> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		return SR.makeCompleteSR(fields, "");
//		System.out.println("SR: " + fields[0] + "\t(" + fields[2].split(",")[1] + ")");
//		notes.sort((a, b) -> b.date.compareTo(a.date));
//		notes.stream().forEach(n -> System.out.println("   --> " + String.format("%-50s", n.text.substring(0, Math.min(50, n.text.length()))) + " : " + n.date));
	}

	protected Optional<SR> processLineNotes(String line, List<SRNote> notes, String delimiter) {
		String [] fields = line.split(delimiter);
//		System.out.println("SR: " + fields[0] + "\t(" + fields[2].split(",")[1] + ")");
		notes.sort((a, b) -> b.date.compareTo(a.date));
//		notes.stream().forEach(n -> System.out.println("   --> " + String.format("%-50s", n.text.substring(0, Math.min(50, n.text.length()))) + " : " + n.date));
		return SR.makeNotesSR(fields, notes, "");
	}
	
	/*
	protected Optional<SR> processLine(String line, String delimiter) {
		
//		42374.05290509259
		
		
//		System.out.println(line);
		String [] fields = line.split(delimiter);
		if (!fields[0].isEmpty())
			System.out.println("SR: " + fields[0] + "; WN: " + fields[3]);
		else
			System.out.println(" -->");
//		System.out.println("report: " + fields[6] + "; closure: "  + fields[7]);
		
//		Date dr = DateUtil.getJavaDate(Double.parseDouble(fields[6]));
//		Date dc = DateUtil.getJavaDate(Double.parseDouble(fields[7]));
		
//		System.out.println(dr + "\t" + dc);
		
//		return SR.makeHistoricSR(fields, dateFormat);
		return Optional.empty(); 
	}
*/

	public static void main(String[] args) {
		/*
		String riskDateFormat = "y-M-d";
		String riskTimeFormat = "y-M-d h:m:s";
		RiskParser parser = new RiskParser(riskDateFormat, riskTimeFormat);
		List<Risk> risks   = parser.parse(dataPath + "\\" + dataFile, 1);
		List<String> statuses = Stream.of("Warning implementing", "Plan Implementing").collect(Collectors.toList()); 
		Risk r = risks.get(0);
		System.out.println(r.toDetailedString()); 
		/**/
		/*
		risks.stream().forEach(r -> 
			{
				if (statuses.contains(r.riskStatus)) 
					System.out.println(r.taskId + "\t" + r.riskStatus);
			});		
		/**/
		/*		
		ParseTest pt = new ParseTest();
		List<SR> srs = pt.readInputFile(ExcelTools.getExcelWorkbook(dataPath + dataFile), skipLines);
//		srs.stream().forEach(sr -> System.out.println(sr.toDetailedString()));
		
		srs.stream().forEach(sr -> {
			System.out.println("\n----------------------------------------------------");
			System.out.println(sr.srNumber);
			sr.notes.stream().forEach(n -> {
				System.out.println("    " + DataTools.dateToStr(n.date));
				System.out.println("        " + n.type);
				System.out.println("        " + n.text);						
				System.out.println("        " + n.visibility);						
			});
		});
*/
/*
		SRParser parser = new CompleteSRParser(dateFormat);
		List<SR> srs = parser.parse(dataPath + dataFile, skipLines);
		srs.stream().forEach(sr -> {
			System.out.println(sr.srNumber);
		});/**/

		/*
		SRParser parser = new NotesSRParser(dateFormat);
		List<SR> srs = parser.parseWithNotes(dataPath + dataFile, skipLines);

		srs.stream().forEach(sr -> {
			System.out.println("\n----------------------------------------------------");
			System.out.println(sr.srNumber);
			sr.notes.stream().forEach(n -> {
				System.out.println("    " + DataTools.dateToStr(n.date));
				System.out.println("        " + n.type);
				System.out.println("        " + n.text);						
				System.out.println("        " + n.visibility);						
			});
		});
		*/
	}
}
