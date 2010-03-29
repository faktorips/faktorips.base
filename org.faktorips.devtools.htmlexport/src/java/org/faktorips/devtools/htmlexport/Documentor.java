package org.faktorips.devtools.htmlexport;

import java.util.List;

import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;

/**
 * The Documentor is the base for the export and should be used as base for the
 * documentation.
 * 
 * @author dicker
 * 
 */
public class Documentor {
	private DocumentorConfiguration config;

	/**
	 * Instantiates a Documentor. Throws an {@link IllegalArgumentException}, if config is null.
	 * @param config
	 * @throws IllegalArgumentException
	 */
	public Documentor(DocumentorConfiguration config) {
		setDocumentorConfiguration(config);
	}

	private void setDocumentorConfiguration(DocumentorConfiguration config) {
		if (config == null)
			throw new IllegalArgumentException("The DocumentorConfiguration must not be null");
		this.config = config;
	}

	/**
	 * 
	 * @return the {@link DocumentorConfiguration}
	 */
	public DocumentorConfiguration getDocumentorConfiguration() {
		return config;
	}

	/**
	 * Takes all scripts from the config and and executes them with the configuration
	 */
	public void execute() {
		List<IDocumentorScript> scripts = getDocumentorConfiguration().getScripts();
		for (IDocumentorScript documentorScript : scripts) {
			documentorScript.execute(getDocumentorConfiguration());
		}
	}
}
