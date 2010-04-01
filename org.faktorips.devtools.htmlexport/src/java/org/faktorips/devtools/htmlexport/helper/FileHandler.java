package org.faktorips.devtools.htmlexport.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.osgi.framework.Bundle;

/**
 * This class writes and read files for the documentator.
 * 
 * @author dicker
 * 
 */
public class FileHandler {

	/**
	 * writes the content into a file. The filename is the path of the given
	 * {@link DocumentorConfiguration} and relativPath.
	 * 
	 * @param config
	 * @param relativPath
	 * @param content
	 */
	public static void writeFile(DocumentorConfiguration config, String relativPath, byte[] content) {
		try {
			File file = new File((config.getPath() + File.separator + relativPath));
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			OutputStream outputStream = new FileOutputStream(file);
			outputStream.write(content);
			outputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads the content of a file from the given bundle
	 * @param bundleName name of the bundle
	 * @param fileName name of the file
	 * @return content of the file
	 * @throws IOException 
	 */
	public static byte[] readFile(String bundleName, String fileName) throws IOException {
		if (Platform.getBundle(bundleName) == null)
			throw new IOException("Bundle not found");

		BufferedInputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			Bundle bundle = Platform.getBundle(bundleName);
			URL resource = bundle.getResource(fileName);

			if (resource == null) {
				throw new IOException(fileName + " not found in " + bundleName);
			}
			in = new BufferedInputStream(resource.openStream());
			byte[] buffer = new byte[8 * 1024];
			int count;

			while ((count = in.read(buffer)) >= 0) {
				out.write(buffer, 0, count);
			}
			in.close();
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					throw e;
				}
		}
		return out.toByteArray();
	}
}
