/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Locale;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public class ChangesOverTimeNamingConvention implements IChangesOverTimeNamingConvention {

    private final static String pack = StringUtil.getPackageName(ChangesOverTimeNamingConvention.class.getName());
    private final static String unqualifiedClassName = StringUtil.unqualifiedName(ChangesOverTimeNamingConvention.class
            .getName());
    private final static String GENERATION_IMAGE_BASE = "Generation"; //$NON-NLS-1$
    private final static String VERSION_IMAGE_BASE = "Version"; //$NON-NLS-1$

    private String id;
    private LocalizedStringsSet locStringSet;

    public ChangesOverTimeNamingConvention(String id) {
        this(id, new LocalizedStringsSet(
                pack + "." + id + unqualifiedClassName, ChangesOverTimeNamingConvention.class.getClassLoader())); //$NON-NLS-1$
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
    @Override
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
     * {@inheritDoc}
     */
    public String getEffectiveDateConceptName() {
        return getEffectiveDateConceptName(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * {@inheritDoc}
     */
    public Image getGenerationConceptImage() {
        return getGenerationConceptImage(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * {@inheritDoc}
     */
    public String getGenerationConceptNameAbbreviation() {
        return getGenerationConceptNameAbbreviation(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * {@inheritDoc}
     */
    public String getGenerationConceptNameSingular(Locale locale) {
        return getGenerationConceptNameSingular(locale, false);
    }

    /**
     * {@inheritDoc}
     */
    public String getGenerationConceptNamePlural(Locale locale) {
        return getGenerationConceptNamePlural(locale, false);
    }

    /**
     * {@inheritDoc}
     */
    public String getGenerationConceptNamePlural() {
        return getGenerationConceptNamePlural(IpsPlugin.getDefault().getUsedLanguagePackLocale(), false);
    }

    /**
     * {@inheritDoc}
     */
    public String getGenerationConceptNameSingular() {
        return getGenerationConceptNameSingular(IpsPlugin.getDefault().getUsedLanguagePackLocale(), false);
    }

    /**
     * {@inheritDoc}
     */
    public String getGenerationConceptNamePlural(boolean usageInsideSentence) {
        return getGenerationConceptNamePlural(IpsPlugin.getDefault().getUsedLanguagePackLocale(), usageInsideSentence);
    }

    /**
     * {@inheritDoc}
     */
    public String getGenerationConceptNameSingular(boolean usageInsideSentence) {
        return getGenerationConceptNameSingular(IpsPlugin.getDefault().getUsedLanguagePackLocale(), usageInsideSentence);
    }

    private String getGenerationConceptNamePlural(Locale usedLanguagePackLocale, boolean usageInsideSentence) {
        return locStringSet.getString(!usageInsideSentence ? "generationConceptNamePlural"
                : "generationConceptNamePluralInsideSentence", usedLanguagePackLocale);
    }

    private String getGenerationConceptNameSingular(Locale usedLanguagePackLocale, boolean usageInsideSentence) {
        return locStringSet.getString(!usageInsideSentence ? "generationConceptNameSingular"
                : "generationConceptNameSingularInsideSentence", usedLanguagePackLocale);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return getName(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * {@inheritDoc}
     */
    public Image getVersionConceptImage() {
        return getVersionConceptImage(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * {@inheritDoc}
     */
    public String getVersionConceptNameAbbreviation() {
        return getVersionConceptNameAbbreviation(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * {@inheritDoc}
     */
    public String getVersionConceptNamePlural() {
        return getVersionConceptNamePlural(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * {@inheritDoc}
     */
    public String getVersionConceptNameSingular() {
        return getVersionConceptNameSingular(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    /**
     * Returns the image with the name build by the id given to this class on construction, the
     * given baseName and locale as <code><id>_<basename>_<locale>.gif</code>. First, the image is
     * tried to be loaded with full locale (language and country code, e.g. "de_DE"), but if no
     * image is found, the locale is reduced to the language only (e.g. "de"). If no image is found
     * for the locale-specific names, it is tried to load the image without the locale. If no image
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
            image = plugin.getImage(id + "_" + baseName + "_" + locale.toString() + ".gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        // if the locale has a country code (e.g. DE), we now ignore this and try
        // to load the image only with the language code (e.g. de).
        if (image == null && locale.getCountry().length() != 0) {
            image = plugin.getImage(id + "_" + baseName + "_" + locale.getLanguage(), true); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // neither for full locale nor for only language code an image was found,
        // so try to load the base image and let the missing image descriptor be
        // returned if not found.
        if (image == null) {
            image = plugin.getImage(id + "_" + baseName + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return image;
    }
}
