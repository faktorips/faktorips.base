package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public class ChangesOverTimeNamingConvention implements
		IChangesOverTimeNamingConvention {

	
	private final static String pack = StringUtil.getPackageName(ChangesOverTimeNamingConvention.class.getName());
	private final static String unqalifiedClassName = StringUtil.unqualifiedName(ChangesOverTimeNamingConvention.class.getName());

	
	private String id;
	private LocalizedStringsSet locStringSet;

	public ChangesOverTimeNamingConvention(String id) {
		this(id, new LocalizedStringsSet(pack + "." + id + unqalifiedClassName, ChangesOverTimeNamingConvention.class.getClassLoader()));
	}
	
	public ChangesOverTimeNamingConvention(String id, LocalizedStringsSet locStringSet) {
		ArgumentCheck.notNull(id);
		ArgumentCheck.notNull(locStringSet);
		this.id = id;
		this.locStringSet = locStringSet;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName(Locale locale) {
		return locStringSet.getString("name", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGenerationConceptNameSingular(Locale locale) {
		return locStringSet.getString("generationConceptNameSingular", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGenerationConceptNamePlural(Locale locale) {
		return locStringSet.getString("generationConceptNamePlural", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGenerationConceptNameAbbreviation(Locale locale) {
		return locStringSet.getString("generationConceptAbbreviation", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionConceptNameSingular(Locale locale) {
		return locStringSet.getString("versionConceptNameSingular", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionConceptNamePlural(Locale locale) {
		return locStringSet.getString("versionConceptNamePlural", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionConceptNameAbbreviation(Locale locale) {
		return locStringSet.getString("versionConceptAbbreviation", locale);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEffectiveDateConceptName(Locale locale) {
		return locStringSet.getString("effectiveDateConceptName", locale);
	}

	public String toString() {
		return id;
	}

	
}
