/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.Locale;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
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
	private final static String GENERATION_IMAGE_BASE = "Generation";
	private final static String VERSION_IMAGE_BASE = "Version";

	
	private String id;
	private LocalizedStringsSet locStringSet;

	public ChangesOverTimeNamingConvention(String id) {
		this(id, new LocalizedStringsSet(pack + "." + id + unqalifiedClassName, ChangesOverTimeNamingConvention.class.getClassLoader())); //$NON-NLS-1$
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
		return locStringSet.getString("name", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGenerationConceptNameSingular(Locale locale) {
		return locStringSet.getString("generationConceptNameSingular", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGenerationConceptNamePlural(Locale locale) {
		return locStringSet.getString("generationConceptNamePlural", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getGenerationConceptNameAbbreviation(Locale locale) {
		return locStringSet.getString("generationConceptAbbreviation", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionConceptNameSingular(Locale locale) {
		return locStringSet.getString("versionConceptNameSingular", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionConceptNamePlural(Locale locale) {
		return locStringSet.getString("versionConceptNamePlural", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersionConceptNameAbbreviation(Locale locale) {
		return locStringSet.getString("versionConceptAbbreviation", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEffectiveDateConceptName(Locale locale) {
		return locStringSet.getString("effectiveDateConceptName", locale); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getGenerationConceptImage(Locale locale) {
		return getImage(locale, GENERATION_IMAGE_BASE);
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getVersionConceptImage(Locale locale) {
		return getImage(locale, VERSION_IMAGE_BASE);
	}
	
	/**
	 * Returns the image with the name build by the id given to this
	 * class on construction, the given baseName
	 * and locale as <code><id>_<basename>_<locale>.gif</code>. First, the 
	 * image is tried to be loaded with full locale (language and country code,
	 * e.g. "de_DE"), but if no image is found, the locale is reduced to the 
	 * language only (e.g. "de"). If no image is found 
	 * for the locale-specific names, 
	 * it is tried to load the image without the locale. If no image 
	 * can be found, the missing image is returned. 
	 * <p>
	 * The images have to have the extension ".gif".
	 *  
	 * @param locale The locale to find the image for.
	 * @param baseName The base image name without extension.
	 */
	private Image getImage(Locale locale, String baseName) {
		IpsPlugin plugin = IpsPlugin.getDefault();
		String localeString = locale.toString();
		
		Image image = null;
		// first we try to load the image with the full locale, i.e. de_DE
		if (localeString.length() > 0) {
			image = plugin.getImage(id + "_" + baseName + "_" + locale.toString() + ".gif", true);
		}
		
		// if the locale has a country code (e.g. DE), we now ignore this and try
		// to load the image only with the language code (e.g. de).
		if (image == null && locale.getCountry().length() != 0) {
			image = plugin.getImage(id + "_" + baseName + "_" + locale.getLanguage(), true);
		}
		
		// neither for full locale nor for only language code an image was found,
		// so try to load the base image and let the missing image descriptor be
		// returned if not found.
		if (image == null) {
			image = plugin.getImage(id + "_" + baseName + ".gif");
		}
		
		return image;
	}
}
