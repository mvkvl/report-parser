package misc;

import java.util.List;
import java.util.Optional;

import ws.slink.spm.model.SR;

public class ActiveCsvSRParser extends AbstractCsvSRParser {
	@Override
	protected Optional<SR> processLine(String line, String delimiter) {
		String [] fields = line.split(delimiter);
		return SR.makeActiveSR(fields, "yyyy-MM-dd HH:mm:ss");
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


//SR sr = null;
//try {
//	sr = new SR(fields);
//} catch (IllegalArgumentException ex) {
//	ex.getMessage();
//}


//public static Stream<? extends SR> filterSRFE(List<? extends SR> values) {
//	return values.stream()
//			     .filter( x -> x.type.contains("CS - Technical Request") || x.type.contains("Technical Request - Cross"))
//			     .filter( x -> x.country.contains("Russian"))
//			     .filter( x -> x.state == null || x.state.isEmpty() || x.state.equals("FE"));
//}
//
//public static Stream<? extends SR> filterSRRussia(List<? extends SR> values) {
//	return values.stream()
//			     .filter( x -> x.type.contains("CS - Technical Request") || x.type.contains("Technical Request - Cross"))
//			     .filter( x -> x.country.contains("Russian"));
//}
