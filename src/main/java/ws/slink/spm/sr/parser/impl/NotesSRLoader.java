package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.Tuple;
import ws.slink.spm.sr.parser.SRLoader;

public class NotesSRLoader implements SRLoader {

	/**
	 * load SRs from list into database (or update already existing SRs)
	 * @param list - list of SRs got from SRParser
	 * @return
	 */
	@Override
	public Tuple<Integer> load(Datastore datastore, List<SR> list) {
		int add = 0, upd = 0;
		for (SR sr : list) {
			Optional<SR> optDbsr = Optional.ofNullable((SR)datastore.get(sr));
			if (optDbsr.isPresent()) {
				SR dbsr = optDbsr.get();
				datastore.save(updateSR(sr, dbsr));
				upd++;
			} else {
//				add++;
//				datastore.save(sr);
			} 
		}
		return new Tuple<Integer>(add, upd);
	}
	
	private SR updateSR(SR source, SR dest) {
		dest.notes = source.notes; 
		return dest;
	}
	
	@Override
	public List<SR> query(Datastore datastore) {
		Query<SR> query = datastore.createQuery(SR.class);
		return query.asList();
	}

}
