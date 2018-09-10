package ws.slink.spm.sr.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import ws.slink.spm.db.DatastoreProvider;
import ws.slink.spm.model.SR;
import ws.slink.spm.tools.ExcelTools;

public class CriticalsChecker {
	
	private Datastore datastore;
	
	public CriticalsChecker(Datastore datastore) {
		this.datastore = datastore;
	}
	
	public List<String> getCriticalSRs(String fileName, int skipRows) {
		List<String> res = new ArrayList<>();
		ExcelTools.getExcelWorkbook(fileName).ifPresent( workbook -> {
			Sheet firstSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();

			for (int i = 0; i < skipRows; i++)
				iterator.next();
			
			// iterate over rows
			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Iterator<Cell> cellIterator = nextRow.cellIterator();
				// get cell value
				if (cellIterator.hasNext()) {
		            Cell cell = cellIterator.next();
		            String str = "";
		            switch (cell.getCellType()) {
		                case Cell.CELL_TYPE_STRING:
		                	str += cell.getStringCellValue();
		                    break;
		                case Cell.CELL_TYPE_BOOLEAN:
		                	str += cell.getBooleanCellValue();
		                    break;
		                case Cell.CELL_TYPE_NUMERIC:
		                	str += cell.getNumericCellValue();
		                	break;
		            }
					res.add(((int)Double.parseDouble(str.replaceAll("[^\\d.]", ""))) + "");
				}
			}
		});
		return res;		
	}
	
	public void processCriticalSrs(List<String> srs) {
		srs.stream().forEach( srId -> {
			Query<SR> q = datastore.find(SR.class, "_id", srId);
			Optional.ofNullable(q.get()).ifPresent( dbsr -> {
				if (!dbsr.critical) {
					dbsr.critical = true;
					datastore.save(dbsr);
				}
			});
		});
	}
	
	public static void main(String[] args) {
		// turn off mongo logging
		Logger mongoLogger = Logger.getLogger("org.mongodb");
		mongoLogger.setLevel(Level.OFF);

		String inputFile = "D:\\work\\dev\\spm\\data\\criticals_2015.xlsx";
		CriticalsChecker checker = new CriticalsChecker(new DatastoreProvider("localhost", 27017, "spm").datastore());
		List<String> criticals = checker.getCriticalSRs(inputFile, 1);
		checker.processCriticalSrs(criticals);
	}
}
