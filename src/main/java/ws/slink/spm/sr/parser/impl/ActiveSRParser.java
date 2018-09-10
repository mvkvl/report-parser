package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.SRNote;
import ws.slink.spm.tools.MethodNotImplementedException;

public class ActiveSRParser extends AbstractSRParser {
	
	public ActiveSRParser(String dateFormat) {
		super(dateFormat);
	}
	
	@Override
	protected Optional<SR> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		return SR.makeActiveSR(fields, dateFormat);
	}

	@Override
	protected Optional<SR> processLine(String line, String delimiter, List<SRNote> notes) {
		throw new MethodNotImplementedException("not implemented in ActiveSRParser");
	}
}
