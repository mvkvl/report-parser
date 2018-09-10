package ws.slink.spm.sr.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ws.slink.spm.db.DatastoreProvider;
import ws.slink.spm.model.IBMS;
import ws.slink.spm.model.RFC;
import ws.slink.spm.model.Risk;
import ws.slink.spm.model.SR;
import ws.slink.spm.model.Tuple;
import ws.slink.spm.sr.parser.impl.ActiveSRLoader;
import ws.slink.spm.sr.parser.impl.ActiveSRParser;
import ws.slink.spm.sr.parser.impl.CommonSRLoader;
import ws.slink.spm.sr.parser.impl.CommonSRParser;
import ws.slink.spm.sr.parser.impl.CompleteSRLoader;
import ws.slink.spm.sr.parser.impl.CompleteSRParser;
import ws.slink.spm.sr.parser.impl.HistoricSRLoader;
import ws.slink.spm.sr.parser.impl.HistoricSRParser;
import ws.slink.spm.sr.parser.impl.IBMSLoader;
import ws.slink.spm.sr.parser.impl.IBMSParser;
import ws.slink.spm.sr.parser.impl.NotesSRLoader;
import ws.slink.spm.sr.parser.impl.NotesSRParser;
import ws.slink.spm.sr.parser.impl.RFCLoader;
import ws.slink.spm.sr.parser.impl.RFCParser;
import ws.slink.spm.sr.parser.impl.RiskLoader;
import ws.slink.spm.sr.parser.impl.RiskParser;
import ws.slink.spm.tools.Configuration;
import ws.slink.spm.tools.Parameters;

public class Application {

	static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("SRLoaderApp");

	static final String DBName = "spm";

	private static final String DATA_PATH           = "data.path";
	private static final String DATA_COMMON_FILES   = "data.common.files";
	private static final String DATA_ACTIVE_FILES   = "data.unresolved.files";
	private static final String DATA_HISTORIC_FILES = "data.opened.files";
	private static final String DATA_COMPLETE_FILES = "data.complete.files";
	private static final String DATA_NOTES_FILES    = "data.notes.files";
	private static final String DATA_CRITICAL_FILES = "data.critical.files";
	private static final String DATA_RFC_FILES      = "data.rfc.files";
	private static final String DATA_SKIP_ROWS_ICARE= "data.skip.rows.icare";
	private static final String DATA_SKIP_ROWS_RDS  = "data.skip.rows.rds";
	private static final String DATE_FORMAT         = "date.format";

	private static final String RISK_DATA_FILES     = "risk.data.files";
	private static final String RISK_DATE_FORMAT    = "risk.date.format";
	private static final String RISK_TIME_FORMAT    = "risk.time.format";
	private static final String RISK_SKIP_LINES     = "risk.skip.lines";

	private static final String IBMS_DATA_FILES     = "ibms.data.files";
	private static final String IBMS_DATE_FORMAT    = "ibms.date.format";
	private static final String IBMS_SKIP_LINES     = "ibms.skip.lines";

	private final String       dateFormat;
	private final String       dataPath;
	private final List<String> commonFiles;
	private final List<String> unresolvedFiles;
	private final List<String> openedFiles;
	private final List<String> completeFiles;
	private final List<String> notesFiles;
	private final List<String> criticalFiles;
	private final List<String> rfcFiles;

	private final int          skipRowsNormal;
	private final int          skipRowsCritical;

	private final List<String> riskFiles;
	private final String       riskDateFormat;
	private final String       riskTimeFormat;
	private final int          riskSkipLines;

	private final List<String> ibmsFiles;
	private final String       ibmsDateFormat;
	private final int          ibmsSkipLines;

	public Application() {
		// load configuration
		this.dataPath         = Configuration.instance().getValue(DATA_PATH, null);
		this.commonFiles      = Configuration.instance().getStringValues(DATA_COMMON_FILES, null, ",");
		this.unresolvedFiles  = Configuration.instance().getStringValues(DATA_ACTIVE_FILES, null, ",");
		this.openedFiles      = Configuration.instance().getStringValues(DATA_HISTORIC_FILES, null, ",");
		this.completeFiles    = Configuration.instance().getStringValues(DATA_COMPLETE_FILES, null, ",");
		this.notesFiles       = Configuration.instance().getStringValues(DATA_NOTES_FILES, null, ",");
		this.criticalFiles    = Configuration.instance().getStringValues(DATA_CRITICAL_FILES, null, ",");
		this.rfcFiles         = Configuration.instance().getStringValues(DATA_RFC_FILES, null, ",");
		this.dateFormat       = Configuration.instance().getValue(DATE_FORMAT, null);
		this.skipRowsNormal   = Integer.parseInt(Configuration.instance().getValue(DATA_SKIP_ROWS_ICARE, null));
		this.skipRowsCritical = Integer.parseInt(Configuration.instance().getValue(DATA_SKIP_ROWS_RDS, null));

		this.riskFiles        = Configuration.instance().getStringValues(RISK_DATA_FILES, null, ",");
		this.riskDateFormat   = Configuration.instance().getValue(RISK_DATE_FORMAT, null);
		this.riskTimeFormat   = Configuration.instance().getValue(RISK_TIME_FORMAT, null);
		this.riskSkipLines    = Integer.parseInt(Configuration.instance().getValue(RISK_SKIP_LINES, null));

		this.ibmsFiles        = Configuration.instance().getStringValues(IBMS_DATA_FILES, null, ",");
		this.ibmsDateFormat   = Configuration.instance().getValue(IBMS_DATE_FORMAT, null);
		this.ibmsSkipLines    = Integer.parseInt(Configuration.instance().getValue(IBMS_SKIP_LINES, null));
	}

	public void loadAll() {
		process_srs();
		process_rfcs();
		process_risks();
		process_ibms();
	}

	private void process_srs() {
		DatastoreProvider dsProvider = new DatastoreProvider(Parameters.getDBHost(), Parameters.getDBPort(), DBName);
		unresolvedFiles.stream().forEachOrdered( file -> loadUnresolved(file, dsProvider) );
		openedFiles.stream().forEachOrdered( file -> loadOpened(file, dsProvider) );
		commonFiles.stream().forEachOrdered( file -> loadCommon(file, dsProvider) );
		completeFiles.stream().forEachOrdered( file -> loadComplete(file, dsProvider) );
		notesFiles.stream().forEachOrdered( file -> loadNotes(file, dsProvider) );
		criticalFiles.stream().forEachOrdered( file -> checkCriticals(file, dsProvider) );
		dsProvider.disconnect();
	}

	private boolean checkFile(String fileName) {
		// if file does not exist, return false
		File f = new File(dataPath + "\\" + fileName);
		if(!f.exists() || f.isDirectory()) {
			logger.warn("file '" + fileName + "' not found");
			return false;
		}
		return true;
	}

	private void loadNotes(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null") || fileName.trim().isEmpty())
			return;
		logger.info("loading " + fileName);
		SRParser parser = new NotesSRParser(dateFormat);
		List<SR> srs = parser.parseWithNotes(dataPath + "\\" + fileName, skipRowsNormal);
		SRLoader loader = new NotesSRLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), srs);
		logger.info(fileName + "--> parsed: "+ srs.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());
	}

	private void loadComplete(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null") || fileName.trim().isEmpty())
			return;
		logger.info("loading " + fileName);
		SRParser parser = new CompleteSRParser(dateFormat);
		List<SR> srs = parser.parse(dataPath + "\\" + fileName, skipRowsNormal);

		List<SR> our_srs = new ArrayList<>();
		srs .stream()
			.filter(x -> x.customerRegion.equals("Russia Rep Office"))
			.forEach( x -> our_srs.add(x));

//		our_srs.stream().forEach(sr -> System.out.println(sr.toDetailedString()));

		SRLoader loader = new CompleteSRLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), our_srs);

		logger.info(fileName + "--> parsed: "+ our_srs.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());
	}
	private void loadCommon(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null") || fileName.trim().isEmpty())
			return;

		logger.info("loading " + fileName);

		SRParser parser = new CommonSRParser(dateFormat);
		List<SR> srs = parser.parse(dataPath + "\\" + fileName, skipRowsNormal);

		List<SR> our_srs = new ArrayList<>();
		srs .stream()
			.filter(x -> x.customerRegion.equals("Russia Rep Office"))
			.forEach( x -> our_srs.add(x));

		SRLoader loader = new CommonSRLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), our_srs);

		logger.info(fileName + "--> parsed: "+ our_srs.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());
	}
	private void loadUnresolved(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null"))
			return;

		logger.info("loading " + fileName);

		SRParser parser = new ActiveSRParser(dateFormat);
		List<SR> srs = parser.parse(dataPath + "\\" + fileName, skipRowsNormal);

		List<SR> our_srs = new ArrayList<>();
		srs .stream()
			// TODO: consider Central Asia & Caucases
			.filter(x -> x.customerRegion.equals("Russia Rep Office"))
			.forEach( x -> our_srs.add(x));


		SRLoader loader = new ActiveSRLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), our_srs);
		logger.info(fileName + "--> parsed: " + our_srs.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());
	}
	private void loadOpened(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null"))
			return;

		logger.info("loading " + fileName);

		SRParser parser = new HistoricSRParser(dateFormat);
		List<SR> srs = parser.parse(dataPath + "\\" + fileName, skipRowsNormal);

		List<SR> our_srs = new ArrayList<>();
		srs .stream()
			.filter(x -> x.customerRegion.equals("Russia Rep Office"))
			.forEach( x -> our_srs.add(x));

		SRLoader loader = new HistoricSRLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), our_srs);

		logger.info(fileName + "--> parsed: "+ our_srs.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());
	}
	private void checkCriticals(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null"))
			return;
		logger.info("checking " + fileName);
		CriticalsChecker checker = new CriticalsChecker(dsProvider.datastore());
		List<String> criticals = checker.getCriticalSRs(dataPath + "\\" + fileName, skipRowsCritical);
		checker.processCriticalSrs(criticals);
	}

	private void process_rfcs() {
		DatastoreProvider dsProvider = new DatastoreProvider(Parameters.getDBHost(), Parameters.getDBPort(), DBName);
		rfcFiles.stream().forEachOrdered( file -> loadRFCs(file, dsProvider) );
		dsProvider.disconnect();
	}
	private void loadRFCs(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null"))
			return;

		logger.info("loading " + fileName);

		RFCParser parser = new RFCParser(dateFormat);
		List<RFC> rfcs   = parser.parse(dataPath + "\\" + fileName, skipRowsNormal);

//		List<RFC> our_srs = new ArrayList<>();
//		srs .stream()
//			.forEach( x -> our_srs.add(x));

		RFCLoader loader = new RFCLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), rfcs);

		logger.info(fileName + "--> parsed: "+ rfcs.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());

	}

	private void process_risks() {
		DatastoreProvider dsProvider = new DatastoreProvider(Parameters.getDBHost(), Parameters.getDBPort(), DBName);
		riskFiles.stream().forEachOrdered( file -> loadRisks(file, dsProvider) );
		dsProvider.disconnect();
	}
	private void loadRisks(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null"))
			return;

		logger.info("loading " + fileName);

		RiskParser parser = new RiskParser(riskDateFormat, riskTimeFormat);
		List<Risk> risks   = parser.parse(dataPath + "\\" + fileName, riskSkipLines);

		RiskLoader loader = new RiskLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), risks);

		logger.info(fileName + "--> parsed: "+ risks.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());

	}

	private void process_ibms() {
		DatastoreProvider dsProvider = new DatastoreProvider(Parameters.getDBHost(), Parameters.getDBPort(), DBName);
		ibmsFiles.stream().forEachOrdered( file -> loadIBMS(file, dsProvider) );
		dsProvider.disconnect();
	}
	private void loadIBMS(String fileName, DatastoreProvider dsProvider) {
		if (!checkFile(fileName)) return;
		if (fileName.equalsIgnoreCase("none") || fileName.equalsIgnoreCase("na") || fileName.equalsIgnoreCase("null"))
			return;
		logger.info("loading " + fileName);
		IBMSParser parser = new IBMSParser(ibmsDateFormat);
		List<IBMS> ibms   = parser.parse(dataPath + "\\" + fileName, ibmsSkipLines);
		IBMSLoader loader = new IBMSLoader();
		Tuple<Integer> res = loader.load(dsProvider.datastore(), ibms);
		logger.info(fileName + "--> parsed: "+ ibms.size() + ", created: " + res.getValue1() + ", updated: " + res.getValue2());
	}

	public static void main(String[] args) {
		// turn off mongo logging
		Logger mongoLogger = Logger.getLogger("org.mongodb");
		mongoLogger.setLevel(Level.OFF);

		// create & run app
		Application app = new Application();
		app.loadAll();
	}
}
