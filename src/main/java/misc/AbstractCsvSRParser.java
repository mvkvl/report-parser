package misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ws.slink.spm.model.SR;
import ws.slink.spm.sr.parser.SRParser;

public abstract class AbstractCsvSRParser implements SRParser {

	@Override
	public List<SR> parse(String fileName) {
		return readInputFile(getUnicodeReader(fileName, "UTF-16"), "\t");
	}
	@Override
	public List<SR> parse(String fileName, String encoding){
		return readInputFile(getUnicodeReader(fileName, encoding), "\t");
	}
	@Override
	public List<SR> parse(String fileName, String encoding, String fieldSeparator){
		return readInputFile(getUnicodeReader(fileName, encoding), fieldSeparator);
	}
	
	public List<SR> parse(String fileName, int skipLines) {
		throw new RuntimeException("Method not implemented");
	}
	
	protected Optional<BufferedReader> getACSIIReader(String inFile) {
		File file = new File(inFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return Optional.ofNullable(reader);
	}
	
	protected Optional<BufferedReader> getUnicodeReader(String inFile, String encoding) {
		BufferedReader reader = null;
		try {
			InputStream        is = new FileInputStream(inFile);
			Reader            isr = new InputStreamReader(is, encoding);
			reader = new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return Optional.ofNullable(reader);
	}
	
	protected List<SR> readInputFile(Optional<BufferedReader> optReader, String delimiter) {
		List<SR> results = new ArrayList<>();
		optReader.ifPresent( reader -> {
			String line = null;	// current line (for processing)
			try {
				line = reader.readLine(); // skip first line (headers)
				line = reader.readLine(); // first line (data)
				while(line != null) {
					if (line.length() > 1)
						processLine(line, delimiter).ifPresent( sr -> results.add(sr) );
					line = reader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {						// close reader
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return results;
	}
	
	protected abstract Optional<SR> processLine(String line, String delimiter);
}
