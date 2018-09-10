package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import ws.slink.spm.model.RFC;
import ws.slink.spm.model.Tuple;

public class RFCLoader {
	
	/**
	 * load RFCs from list into database (or update already existing RFCs)
	 * @param list - list of RFC objects got from RFCParser
	 * @return
	 */
	public Tuple<Integer> load(Datastore datastore, List<RFC> list) {
		int add = 0, upd = 0;
		for (RFC rfc : list) {
			Optional<RFC> optDbrfc = Optional.ofNullable((RFC)datastore.get(rfc));
			if (optDbrfc.isPresent()) {
				// preserve state field which is stored in DB,
				// cause we could change it due to possible incorrect value 
				// in original report
				RFC dbrfc   = optDbrfc.get();
				rfc.state   = dbrfc.state;
				rfc.checked = dbrfc.checked;
				upd++;
			} else {
				add++;
			} 
			datastore.save(rfc);
		}
		return new Tuple<Integer>(add, upd);
	}
	
	/**
	 * get all RFCs from database
	 * @return
	 */
	public List<RFC> query(Datastore datastore) {
		Query<RFC> query = datastore.createQuery(RFC.class);
		return query.asList();
	}

}
