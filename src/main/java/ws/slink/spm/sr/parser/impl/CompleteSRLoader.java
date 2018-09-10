package ws.slink.spm.sr.parser.impl;

import java.util.List;
import java.util.Optional;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import ws.slink.spm.model.SR;
import ws.slink.spm.model.Tuple;
import ws.slink.spm.sr.parser.SRLoader;

public class CompleteSRLoader implements SRLoader {

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
		// update fields of existing SR with fields of parsed SR
		// no too efficient
		// TODO: perhaps some rework needed to optimize this issue
		
		dest.srNumber     	                 = source.srNumber;
		dest.ticketNumber 	                 = source.ticketNumber;
		dest.type         	                 = source.type;
		dest.severity     	                 = source.severity;
		dest.customerSeverity                = source.customerSeverity;
		dest.customerSeverityOriginal        = source.customerSeverityOriginal;
		dest.status			                 = source.status;
		dest.channel			             = source.channel;
		dest.contract			             = source.contract;
		dest.escalateFlag		             = source.escalateFlag;
		dest.country			             = source.country;
//		dest.state 			                 = source.state; //		keep SR.state cause it could be modified manually
		dest.loggedBy			             = source.loggedBy;
		dest.resolutionCode                  = source.resolutionCode;
		dest.resolutionSummary               = source.resolutionSummary;
		dest.resolutionDetails               = source.resolutionDetails;
		dest.problemCode                     = source.problemCode;
		dest.problemSummary	                 = source.problemSummary;
		dest.problemDetails                  = source.problemDetails;
		dest.contactType		             = source.contactType;
		dest.initiatorPhoneNumber            = source.initiatorPhoneNumber;
		dest.initiatorEmail                  = source.initiatorEmail;
		dest.initiatorName                   = source.initiatorName;
		dest.productLine		             = source.productLine;
		dest.productClass		             = source.productClass;
		dest.product			             = source.product;
		dest.employeeOrgTAC	                 = source.employeeOrgTAC;
		dest.employeeOrgName	             = source.employeeOrgName;
		dest.employeeName		             = source.employeeName;
		dest.customerRegion	                 = source.customerRegion;
		dest.customerOffice	                 = source.customerOffice;
		dest.customerName		             = source.customerName;

		dest.dates.reportDate 			     = source.dates.reportDate; 
		dest.dates.responseDateExpected	     = source.dates.responseDateExpected;
		dest.dates.responseDateActual	     = source.dates.responseDateActual;
		dest.dates.restoreDateExpected		 = source.dates.restoreDateExpected;
		dest.dates.restoreDateExpectedTravel = source.dates.restoreDateExpectedTravel;
		dest.dates.restoreDateActual		 = source.dates.restoreDateActual;
		dest.dates.waDateExpected			 = source.dates.waDateExpected;
		dest.dates.waDateExpectedWithSuspend = source.dates.waDateExpectedWithSuspend;
		dest.dates.waDateActual				 = source.dates.waDateActual;
		dest.dates.closeDateExpected		 = source.dates.closeDateExpected;
		dest.dates.closeDateExpectedSuspend	 = source.dates.closeDateExpectedSuspend;
		dest.dates.solutionProvidedDate      = source.dates.solutionProvidedDate;
		dest.dates.closeDateActual           = source.dates.closeDateActual;
		dest.dates.closedDate                = source.dates.closedDate;
		dest.dates.lastUpdateDate			 = source.dates.lastUpdateDate;
		dest.dates.totalSuspendDuration		 = source.dates.totalSuspendDuration;
		dest.dates.totalOpenDuration         = source.dates.totalOpenDuration;
		dest.dates.escalateDateActual	     = source.dates.escalateDateActual;
		dest.dates.escalateDateExpected		 = source.dates.escalateDateExpected;

		return dest;
	}
	
	@Override
	public List<SR> query(Datastore datastore) {
		Query<SR> query = datastore.createQuery(SR.class);
		return query.asList();
	}

}
