package ws.slink.spm.sr.parser;

import java.util.List;

import ws.slink.spm.model.SR;

public interface SRParser {
	public List<SR> parse(String fileName);
	public List<SR> parse(String fileName, int skipLines);
	public List<SR> parse(String fileName, String encoding);
	public List<SR> parse(String fileName, String encoding, String fieldSeparator);

	public List<SR> parseWithNotes(String fileName);
	public List<SR> parseWithNotes(String fileName, int skipLines);
}
