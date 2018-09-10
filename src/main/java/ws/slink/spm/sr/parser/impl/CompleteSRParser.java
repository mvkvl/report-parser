package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.SRNote;

public class CompleteSRParser extends AbstractSRParser {

	public CompleteSRParser(String dateFormat) {
		super(dateFormat);
	}
	@Override
	protected Optional<SR> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		if (!fields[0].isEmpty())
			return SR.makeCompleteSR(fields, dateFormat);
		else
			return Optional.empty();
	}
	@Override
	protected Optional<SR> processLine(String line, String delimiter, List<SRNote> notes) {
		String [] fields = line.split(delimiter);
		return SR.makeCompleteSR(fields, dateFormat);
	}

}
