/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Locale;

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName(Locale locale) {
        return locStringSet.getString("name", locale); //$NON-NLS-1$
    }

    @Override
    public String getGenerationConceptNameAbbreviation(Locale locale) {
        return locStringSet.getString("generationConceptAbbreviation", locale); //$NON-NLS-1$
    }

    @Override
    public String getVersionConceptNameSingular(Locale locale) {
        return locStringSet.getString("versionConceptNameSingular", locale); //$NON-NLS-1$
    }

    @Override
    public String getVersionConceptNamePlural(Locale locale) {
        return locStringSet.getString("versionConceptNamePlural", locale); //$NON-NLS-1$
    }

    @Override
    public String getVersionConceptNameAbbreviation(Locale locale) {
        return locStringSet.getString("versionConceptAbbreviation", locale); //$NON-NLS-1$
    }

    @Override
    public String getEffectiveDateConceptName(Locale locale) {
        return locStringSet.getString("effectiveDateConceptName", locale); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public String getEffectiveDateConceptName() {
        return getEffectiveDateConceptName(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    @Override
    public String getGenerationConceptNameAbbreviation() {
        return getGenerationConceptNameAbbreviation(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    @Override
    public String getGenerationConceptNameSingular(Locale locale) {
        return getGenerationConceptNameSingular(locale, false);
    }

    @Override
    public String getGenerationConceptNamePlural(Locale locale) {
        return getGenerationConceptNamePlural(locale, false);
    }

    @Override
    public String getGenerationConceptNamePlural() {
        return getGenerationConceptNamePlural(IpsPlugin.getDefault().getUsedLanguagePackLocale(), false);
    }

    @Override
    public String getGenerationConceptNameSingular() {
        return getGenerationConceptNameSingular(IpsPlugin.getDefault().getUsedLanguagePackLocale(), false);
    }

    @Override
    public String getGenerationConceptNamePlural(boolean usageInsideSentence) {
        return getGenerationConceptNamePlural(IpsPlugin.getDefault().getUsedLanguagePackLocale(), usageInsideSentence);
    }

    @Override
    public String getGenerationConceptNameSingular(boolean usageInsideSentence) {
        return getGenerationConceptNameSingular(IpsPlugin.getDefault().getUsedLanguagePackLocale(), usageInsideSentence);
    }

    private String getGenerationConceptNamePlural(Locale usedLanguagePackLocale, boolean usageInsideSentence) {
        return locStringSet.getString(!usageInsideSentence ? "generationConceptNamePlural" //$NON-NLS-1$
                : "generationConceptNamePluralInsideSentence", usedLanguagePackLocale); //$NON-NLS-1$
    }

    private String getGenerationConceptNameSingular(Locale usedLanguagePackLocale, boolean usageInsideSentence) {
        return locStringSet.getString(!usageInsideSentence ? "generationConceptNameSingular" //$NON-NLS-1$
                : "generationConceptNameSingularInsideSentence", usedLanguagePackLocale); //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return getName(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    @Override
    public String getVersionConceptNameAbbreviation() {
        return getVersionConceptNameAbbreviation(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    @Override
    public String getVersionConceptNamePlural() {
        return getVersionConceptNamePlural(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

    @Override
    public String getVersionConceptNameSingular() {
        return getVersionConceptNameSingular(IpsPlugin.getDefault().getUsedLanguagePackLocale());
    }

}
