package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

import org.faktorips.devtools.core.model.IChangesInTimeNamingConvention;

/**
 * Naming conventions defined in VAA by the GDV.
 * 
 * @author Jan Ortmann
 */
public class VAAChangesInTimeNamingConvention implements
		IChangesInTimeNamingConvention {

	/**
	 * Overridden.
	 */
	public String getId() {
		return "VAA";
	}

	/**
	 * Overridden.
	 */
	public String getName() {
		return "VAA";
	}

	/**
	 * Overridden.
	 */
	public String getGenerationConceptNameSingular(Locale locale) {
		return "Generation";
	}

	/**
	 * Overridden.
	 */
	public String getGenerationConceptNamePlural(Locale locale) {
		return "Generationen";
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

	/**
	 * {@inheritDoc}
	 */
	public String getGenerationConceptNameAbbreviation(Locale locale) {
		return "Gen";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionConceptNameAbbreviation(Locale locale) {
		return "Version";
	}

}
