/**
 * 
 */
package gr.agroknow.metadata.translet;

import java.io.File;

import org.slf4j.Logger;

/**
 * @author vogias
 * 
 */
public class Worker implements Runnable {

	private String generalInfo;
	private Logger slf4jLogger;
	private File file;
	private Translet translet;

	public Worker(String generalInfo, Logger slf4jLogger, File file,
			Translet translet) {
		// TODO Auto-generated constructor stub
		this.generalInfo = generalInfo;
		this.slf4jLogger = slf4jLogger;
		this.file = file;
		this.translet = translet;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		StringBuffer logString = new StringBuffer();
		logString.append(generalInfo);
		String name = file.getName();
		name = name.substring(0, name.indexOf(".xml"));
		logString.append(" " + name);
		logString.append(" " + translet.transform(file));
		logString.append(" " + "thread:" + Thread.currentThread().getId());
		slf4jLogger.info(logString.toString());

	}
}
