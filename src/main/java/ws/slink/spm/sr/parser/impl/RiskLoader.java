package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import ws.slink.spm.model.Risk;
import ws.slink.spm.model.Tuple;

public class RiskLoader {
	/**
	 * load RFCs from list into database (or update already existing RFCs)
	 * @param list - list of RFC objects got from RFCParser
	 * @return
	 */
	public Tuple<Integer> load(Datastore datastore, List<Risk> list) {
		int add = 0, upd = 0;
		for (Risk risk : list) {
			Optional<Risk> optDbRisk = Optional.ofNullable((Risk)datastore.get(risk));
			if (optDbRisk.isPresent()) {
				// preserve state field which is stored in DB,
				// cause we could change it due to possible incorrect value 
				// in original report
//				Risk dbRisk = optDbRisk.get();
//				risk.state = dbRisk.state;
				upd++;
			} else {
				add++;
			} 
			datastore.save(risk);
		}
		return new Tuple<Integer>(add, upd);
	}
	
	/**
	 * get all RFCs from database
	 * @return
	 */
	public List<Risk> query(Datastore datastore) {
		Query<Risk> query = datastore.createQuery(Risk.class);
		return query.asList();
	}

}
