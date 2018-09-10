package ws.slink.spm.sr.parser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.SRNote;

public class NotesSRParser extends AbstractSRParser {

	public NotesSRParser(String dateFormat) {
		super(dateFormat);
	}

	@Override
	protected Optional<SR> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		if (!fields[0].isEmpty())
			return SR.makeNotesSR(fields, new ArrayList<>(), delimiter);
		else
			return Optional.empty();
	}
	@Override
	protected Optional<SR> processLine(String line, String delimiter, List<SRNote> notes) {
		String [] fields = line.split(delimiter);
		return SR.makeNotesSR(fields, notes, dateFormat);
	}
	
}
