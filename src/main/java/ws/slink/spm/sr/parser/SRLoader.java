package ws.slink.spm.sr.parser;

import java.util.List;

import org.mongodb.morphia.Datastore;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.Tuple;

public interface SRLoader {
	public Tuple<Integer> load(Datastore datastore, List<SR> list);
	public List<SR> query(Datastore datastore);
}
