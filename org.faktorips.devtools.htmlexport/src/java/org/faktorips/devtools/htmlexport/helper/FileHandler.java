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

// TODO FEHLERBEHANLDUNG ändern
public class FileHandler {
	public static void writeFile(DocumentorConfiguration config, String filePath, byte[] content) {
		try {
			File file = new File((config.getPath() + File.separator + filePath));
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			System.out.println(file.getAbsolutePath());

			OutputStream outputStream = new FileOutputStream(file);
			outputStream.write(content);
			outputStream.close();
		} catch (Exception e) {
			throw new RuntimeException("Fehler beim Schreiben der Datei", e);
		}
	}

	public static byte[] readFile(String bundleName, String fileName) throws IOException {
		if (Platform.getBundle(bundleName) == null) throw new IOException("Bundle nicht gefunden");
		
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
