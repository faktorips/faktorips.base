package org.faktorips.devtools.htmlexport.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

// TODO FEHLERBEHANLDUNG ändern
public class FileHandler {
	public static void writeFile(DocumentorConfiguration config, String filePath, byte[] content) {
		try {
			File file = new File((config.getPath() + File.separator + filePath));
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			OutputStream outputStream = new FileOutputStream(file);
			outputStream.write(content);
			outputStream.close();
		} catch (Exception e) {
			throw new RuntimeException("Fehler beim Schreiben der Datei", e);
		}
	}

	public static String readFile(String bundle, String fileName) throws IOException  {
		StringBuilder content = new StringBuilder();
		InputStream in = null;
		try {
			URL resource = Platform.getBundle(bundle).getResource(fileName);

			in = resource.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				content.append(line);
				content.append('\n');
			}

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
		return content.toString();
	}
}
