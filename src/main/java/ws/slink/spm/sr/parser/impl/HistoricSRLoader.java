package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.Tuple;
import ws.slink.spm.sr.parser.SRLoader;

public class HistoricSRLoader implements SRLoader {

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
				add++;
				datastore.save(sr);
			} 
		}
		return new Tuple<Integer>(add, upd);
	}
	
	private SR updateSR(SR source, SR dest) {
		// update fields of existing (possibly 'active') SR with fields of 
		// 'historic' SR
		// no too efficient, as existing 'historic' SRs will also be updated
		// TODO: perhaps some rework needed to optimize this issue
		
		dest.dates.closeDateActual 	= source.dates.closeDateActual;
		dest.dates.reportDate 		= source.dates.reportDate;
		dest.dates.totalSuspendDuration = source.dates.totalSuspendDuration;

		dest.customerRegion 		= source.customerRegion;
		dest.country 				= source.country;
		dest.customerOffice 		= source.customerOffice;
		dest.customerName 			= source.customerName;
		dest.customerSeverity 		= source.customerSeverity;
		dest.srNumber 				= source.srNumber;
		dest.type 					= source.type;		
//		dest.state 					= source.state; // keep SR.state cause it could be modified manually
		dest.status 				= source.status;
		dest.problemSummary 		= source.problemSummary;
		dest.resolutionCode 		= source.resolutionCode;
		dest.resolutionSummary 		= source.resolutionSummary;
		dest.loggedBy 				= source.loggedBy;
		dest.contract 				= source.contract;
		dest.product 				= source.product;
		dest.productLine 			= source.productLine;
		dest.employeeOrgTAC 		= source.employeeOrgTAC;
		dest.employeeName 			= source.employeeName;

		return dest;
	}
	
	@Override
	public List<SR> query(Datastore datastore) {
		Query<SR> query = datastore.createQuery(SR.class);
		return query.asList();
	}

}
