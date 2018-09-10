package misc;

import java.util.List;
import java.util.Optional;

import ws.slink.spm.model.SR;

public class HistoricCsvSRParser extends AbstractCsvSRParser {
	@Override
	protected Optional<SR> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		return SR.makeHistoricSR(fields, "yyyy-MM-dd HH:mm:ss");
	}

	@Override
	public List<SR> parseWithNotes(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SR> parseWithNotes(String fileName, int skipLines) {
		// TODO Auto-generated method stub
		return null;
	}
}
