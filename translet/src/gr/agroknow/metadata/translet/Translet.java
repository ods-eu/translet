package gr.agroknow.metadata.translet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.corba.se.spi.orbutil.threadpool.Work;

public class Translet {
	private static File xsl;
	private static File input;
	private static File output;
	private static File bad;

	private Transformer transformer;

	private static final Logger slf4jLogger = LoggerFactory
			.getLogger(Translet.class);

	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Usage : ");
			System.err
					.println("java -jar gr.agroknow.metadata.translet.Translet <xsl file> <input folder> <output folder> <bad folder>");
			System.exit(-1);
		}

		xsl = new File(args[0]);
		input = new File(args[1]);
		output = new File(args[2]);
		bad = new File(args[3]);

		if (!xsl.exists() || !xsl.isFile()) {
			System.err.println("Invalid xsl transformation: "
					+ xsl.getAbsolutePath());
			System.exit(-1);
		}
		if (!input.exists() || !input.isDirectory()) {
			System.err.println("Invalid input directory: "
					+ input.getAbsolutePath());
			System.exit(-1);
		}
		if (!output.exists() || !output.isDirectory()) {
			System.err.println("Invalid output directory: "
					+ output.getAbsolutePath());
			System.exit(-1);
		}
		if (!bad.exists() || !bad.isDirectory()) {
			System.err.println("Invalid directory for bad files: "
					+ bad.getAbsolutePath());
			System.exit(-1);
		}

		String date = new Date().toString();

		System.out.println("Transformation date:" + date);
		Translet translet = new Translet();

		String generalInfo = translet.ini();

		// logString.append(translet.ini());

		// int availableProcessors = Runtime.getRuntime().availableProcessors();
		// System.out.println("Available cores:" + availableProcessors);
		// ExecutorService executor = Executors
		// .newFixedThreadPool(availableProcessors);

		long start = System.currentTimeMillis();
		for (File file : input.listFiles()) {

			StringBuffer logString = new StringBuffer();
			logString.append(generalInfo);
			String name = file.getName();
			name = name.substring(0, name.indexOf(".xml"));
			logString.append(" " + name);
			logString.append(" " + translet.transform(file));
			slf4jLogger.info(logString.toString());

			// Worker worker = new Worker(generalInfo, slf4jLogger, file,
			// translet);
			// executor.execute(worker);

		}

		// executor.shutdown();
		// try {
		// executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		long end = System.currentTimeMillis();
		long diff = end - start;
		System.out.println("Duration:" + diff + "ms");

		System.out.println("Number of healthy transformed files:"
				+ output.listFiles().length);
		// logString.append(" " + output.listFiles().length);

		System.out.println("Number of bad transformed files:"
				+ bad.listFiles().length);
		// logString.append(" " + bad.listFiles().length);

		System.out.println("Transformation is done.");

		// } catch (InterruptedException e) { // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	private String ini() {
		TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();// TransformerFactory.newInstance()
																				// ;
		StreamSource xslStream = new StreamSource(xsl);
		StringBuffer str = new StringBuffer();

		String repo = input.getName();
		String xslUsed = xsl.getName();
		System.out.println("Transforming repository:" + repo);
		str.append(repo);

		System.out.println("XSL used:" + xslUsed);
		str.append(" " + xslUsed);

		System.out.println("Nuber of files to transform:"
				+ input.listFiles().length);
		str.append(" " + input.listFiles().length);

		try {
			transformer = factory.newTransformer(xslStream);

		} catch (TransformerConfigurationException e) {
			System.err.println("Cannot initialize transformer: "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		return str.toString();
	}

	public String transform(File source) {
		StreamSource in = new StreamSource(source);
		// System.out.println( output.getAbsolutePath() + File.separator +
		// source.getName() ) ;
		File target = new File(output.getAbsolutePath() + File.separator
				+ source.getName());
		StreamResult out = new StreamResult(target);
		try {
			transformer.transform(in, out);
			return "Healthy";
		} catch (TransformerException te) {
			try {
				FileUtils.copyFileToDirectory(source, bad);
				System.err.println("Cannot convert file: " + source.getName());
				return "Bad";
			} catch (IOException ioe) {
				System.err.println("Cannot copy file " + source.getName()
						+ " to " + bad.getAbsolutePath());
				return "Bad";
			}
		}
	}
}