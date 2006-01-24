package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

import org.faktorips.devtools.core.model.IChangesInTimeNamingConvention;

/**
 * Naming conventions uses in Produkt-Manager.
 * 
 * @author Jan Ortmann
 */
public class PMChangesInTimeNamingConvention implements
		IChangesInTimeNamingConvention {

	private final static IChangesInTimeNamingConvention instance = new PMChangesInTimeNamingConvention();
	
	public final static IChangesInTimeNamingConvention getInstance() {
		return instance;
	}
	
	/**
	 * Overridden.
	 */
	public String getId() {
		return "PM";
	}

	/**
	 * Overridden.
	 */
	public String getName() {
		return "Produkt-Manager";
	}

	/**
	 * Overridden.
	 */
	public String getGenerationConceptNameSingular(Locale locale) {
		return "Anpassungsstufe";
	}

	/**
	 * Overridden.
	 */
	public String getGenerationConceptNamePlural(Locale locale) {
		return "Anpassungsstufen";
	}

	/**
	 * Overridden.
	 */
	public String getVersionConceptNameSingular(Locale locale) {
		return "Version";
	}

	/**
	 * Overridden.
	 */
	public String getVersionConceptNamePlural(Locale locale) {
		return "Versionen";
	}

}
