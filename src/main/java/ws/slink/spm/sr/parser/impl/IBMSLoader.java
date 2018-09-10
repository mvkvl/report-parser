package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import ws.slink.spm.model.IBMS;
import ws.slink.spm.model.Tuple;

public class IBMSLoader {
	
	/**
	 * load IBMSs from list into database (or update already existing RFCs)
	 * @param list - list of RFC objects got from RFCParser
	 * @return
	 */
	public Tuple<Integer> load(Datastore datastore, List<IBMS> list) {
		int add = 0, upd = 0;
		for (IBMS ibms : list) {
			Optional<IBMS> optDbrfc = Optional.ofNullable((IBMS)datastore.get(ibms));
			if (optDbrfc.isPresent()) {
				// preserve state field which is stored in DB,
				// cause we could change it due to possible incorrect value 
				// in original report
//				RFC dbrfc   = optDbrfc.get();
//				ibms.state   = dbrfc.state;
//				ibms.checked = dbrfc.checked;
				upd++;
			} else {
				add++;
			} 
			datastore.save(ibms);
		}
		return new Tuple<Integer>(add, upd);
	}
	
	/**
	 * get all IBMSs from database
	 * @return
	 */
	public List<IBMS> query(Datastore datastore) {
		Query<IBMS> query = datastore.createQuery(IBMS.class);
		return query.asList();
	}

}
