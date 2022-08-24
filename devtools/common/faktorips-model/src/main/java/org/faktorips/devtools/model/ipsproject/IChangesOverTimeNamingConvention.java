/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.Locale;

/**
 * Naming convention for product changes over time.
 * <p>
 * We distinguish between two different types of product changes over time.
 * <ol>
 * <li><strong>Version:</strong> A new product version affects only new insurance policies. Existing
 * policies are not affected by a new product version in any way. Existing policies have to be
 * explicitly converted to the product version, e.g. by a batch program. Perhaps the customer
 * rejects changing to the product version and wants to remain with the old one.</li>
 * <li><strong>Generation:</strong> A new product generation affects existing policies (and of
 * course new policies). If a policy is changed in a way where product data is necessary, the
 * appropriate product is determined automatically based on the change's effective date.</li>
 * </ol>
 * <p>
 * While the above concepts are everywhere the same, there is no commonly accepted naming standard
 * for them. In Germany a standard has been defined by the GDV as part of the standard architecture
 * VAA. In Faktor-IPS' sourcecode the names are used according to the GDV definition (as described
 * above). However, as there is no widely accepted standard, this class encapsulates the names used
 * for the two concept. The names provided by the naming convention are used in the UI.
 * 
 * @author Jan Ortmann
 */
public interface IChangesOverTimeNamingConvention {

    /**
     * The id of the VAA naming convention.
     */
    String VAA = "VAA"; //$NON-NLS-1$

    /**
     * The id of the product-manager naming convention.
     */
    String PM = "PM"; //$NON-NLS-1$

    /**
     * The id of the default Faktor-IPS naming convention.
     */
    String FAKTOR_IPS = "FIPS"; //$NON-NLS-1$

    /**
     * Returns the conventions' identification.
     */
    String getId();

    /**
     * Returns the conventions' name, used to present it to the user.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    String getName(Locale locale);

    /**
     * Returns the conventions' name, used to present it to the user. The locale used is the one
     * returned from IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getName();

    /**
     * Returns the name for the generation concept in singular.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    String getGenerationConceptNameSingular(Locale locale);

    /**
     * Returns the name for the generation concept in singular. The locale used is the one returned
     * from IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getGenerationConceptNameSingular();

    /**
     * Returns the name for the generation concept in singular. The locale used is the one returned
     * from IpsModelPlugin.getUsedLanguagePackLocale().
     * 
     * @param usageInsideSentence <code>true</code> if the name will be used inside a sentence
     */
    String getGenerationConceptNameSingular(boolean usageInsideSentence);

    /**
     * Returns the name for the generation concept in singular.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     * @param usageInsideSentence <code>true</code> if the name will be used inside a sentence
     */
    String getGenerationConceptNameSingular(Locale locale, boolean usageInsideSentence);

    /**
     * Returns the name for the generation concept in plural.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    String getGenerationConceptNamePlural(Locale locale);

    /**
     * Returns the name for the generation concept in plural. The locale used is the one returned
     * from IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getGenerationConceptNamePlural();

    /**
     * Returns the name for the generation concept in plural.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     * @param usageInsideSentence <code>true</code> if the name will be used inside a sentence
     */
    String getGenerationConceptNamePlural(Locale locale, boolean usageInsideSentence);

    /**
     * Returns the name for the generation concept in plural. The locale used is the one returned
     * from IpsModelPlugin.getUsedLanguagePackLocale().
     * 
     * @param usageInsideSentence <code>true</code> if the name will be used inside a sentence
     */
    String getGenerationConceptNamePlural(boolean usageInsideSentence);

    /**
     * Returns the abbreviation for the generation concept (singular).
     * 
     * @param locale The locale that determines the language in which the abbreviation should be
     *            returned.
     */
    String getGenerationConceptNameAbbreviation(Locale locale);

    /**
     * Returns the abbreviation for the generation concept (singular). The locale used is the one
     * returned from IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getGenerationConceptNameAbbreviation();

    /**
     * Returns the name for the version concept in singular.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    String getVersionConceptNameSingular(Locale locale);

    /**
     * Returns the name for the version concept in singular. The locale used is the one returned
     * from IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getVersionConceptNameSingular();

    /**
     * Returns the name for the version concept in plural.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    String getVersionConceptNamePlural(Locale locale);

    /**
     * Returns the name for the version concept in plural. The locale used is the one returned from
     * IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getVersionConceptNamePlural();

    /**
     * Returns the abbreviation for the version concept (singular).
     * 
     * @param locale The locale that determines the language in which the abbreviation should be
     *            returned.
     */
    String getVersionConceptNameAbbreviation(Locale locale);

    /**
     * Returns the abbreviation for the version concept (singular). The locale used is the one
     * returned from IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getVersionConceptNameAbbreviation();

    /**
     * Returns the name for the effective date concept. E.g. in some cases this might be called
     * validFrom.
     * 
     * @param locale The locale that determines the language to use.
     */
    String getEffectiveDateConceptName(Locale locale);

    /**
     * Returns the name for the effective date concept. E.g. in some cases this might be called
     * validFrom. The locale used is the one returned from
     * IpsModelPlugin.getUsedLanguagePackLocale().
     */
    String getEffectiveDateConceptName();

}
