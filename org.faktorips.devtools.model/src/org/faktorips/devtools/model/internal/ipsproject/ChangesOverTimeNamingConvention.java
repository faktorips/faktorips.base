/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.util.Locale;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public class ChangesOverTimeNamingConvention implements IChangesOverTimeNamingConvention {

    private static final String PACK = StringUtil.getPackageName(ChangesOverTimeNamingConvention.class.getName());
    private static final String UNQUALIFIED_CLASS_NAME = StringUtil
            .unqualifiedName(ChangesOverTimeNamingConvention.class.getName());

    private String id;
    private LocalizedStringsSet locStringSet;

    public ChangesOverTimeNamingConvention(String id) {
        this(id, new LocalizedStringsSet(
                PACK + "." + id + UNQUALIFIED_CLASS_NAME, ChangesOverTimeNamingConvention.class.getClassLoader())); //$NON-NLS-1$
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
        return getEffectiveDateConceptName(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale());
    }

    @Override
    public String getGenerationConceptNameAbbreviation() {
        return getGenerationConceptNameAbbreviation(
                IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale());
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
        return getGenerationConceptNamePlural(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale(),
                false);
    }

    @Override
    public String getGenerationConceptNameSingular() {
        return getGenerationConceptNameSingular(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale(),
                false);
    }

    @Override
    public String getGenerationConceptNamePlural(boolean usageInsideSentence) {
        return getGenerationConceptNamePlural(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale(),
                usageInsideSentence);
    }

    @Override
    public String getGenerationConceptNameSingular(boolean usageInsideSentence) {
        return getGenerationConceptNameSingular(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale(),
                usageInsideSentence);
    }

    @Override
    public String getGenerationConceptNamePlural(Locale usedLanguagePackLocale, boolean usageInsideSentence) {
        return locStringSet.getString(!usageInsideSentence ? "generationConceptNamePlural" //$NON-NLS-1$
                : "generationConceptNamePluralInsideSentence", usedLanguagePackLocale); //$NON-NLS-1$
    }

    @Override
    public String getGenerationConceptNameSingular(Locale usedLanguagePackLocale, boolean usageInsideSentence) {
        return locStringSet.getString(!usageInsideSentence ? "generationConceptNameSingular" //$NON-NLS-1$
                : "generationConceptNameSingularInsideSentence", usedLanguagePackLocale); //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return getName(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale());
    }

    @Override
    public String getVersionConceptNameAbbreviation() {
        return getVersionConceptNameAbbreviation(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale());
    }

    @Override
    public String getVersionConceptNamePlural() {
        return getVersionConceptNamePlural(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale());
    }

    @Override
    public String getVersionConceptNameSingular() {
        return getVersionConceptNameSingular(IIpsModel.get().getMultiLanguageSupport().getUsedLanguagePackLocale());
    }

}
