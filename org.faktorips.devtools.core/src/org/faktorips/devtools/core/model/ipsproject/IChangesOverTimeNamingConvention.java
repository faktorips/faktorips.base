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
package org.faktorips.devtools.core.model.ipsproject;

import java.util.Locale;

/**
 * Naming convention for product changes over time.
 * <p>
 * We distinguish between two different types of product changes over time.
 * <ol>
 * <li>Version</li>
 * <p>
 * A new product version affects only new insurance policies. Existing policies are not affected by
 * a new product version in any way. Existing policies have to be explicitly converted to the
 * product version, e.g. by a batch program. Perhaps the customer rejects changing to the product
 * version and wants to remain with the old one.
 * <p>
 * <li>Generation</li>
 * <p>
 * A new product generation affects existing policies (and of course new policies). If a policy is
 * changed in a way where product data is necessary, the appropriate product is determined
 * automatically based on the change's effective date.
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
    public final static String VAA = "VAA"; //$NON-NLS-1$

    /**
     * The id of the product-manager naming convention.
     */
    public final static String PM = "PM"; //$NON-NLS-1$

    /**
     * The id of the default Faktor-IPS naming convention.
     */
    public final static String FAKTOR_IPS = "FIPS"; //$NON-NLS-1$

    /**
     * Returns the conventions' identification.
     */
    public String getId();

    /**
     * Returns the conventions' name, used to present it to the user.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    public String getName(Locale locale);

    /**
     * Returns the conventions' name, used to present it to the user. The locale used is the one
     * returned from IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getName();

    /**
     * Returns the name for the generation concept in singular.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    public String getGenerationConceptNameSingular(Locale locale);

    /**
     * Returns the name for the generation concept in singular. The locale used is the one returned
     * from IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getGenerationConceptNameSingular();

    /**
     * Returns the name for the generation concept in singular. The locale used is the one returned
     * from IpsPlugin.getUsedLanguagePackLocale().
     * 
     * @param usageInsideSentence <code>true</code> if the name will be used inside a sentence
     */
    public String getGenerationConceptNameSingular(boolean usageInsideSentence);

    /**
     * Returns the name for the generation concept in plural.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    public String getGenerationConceptNamePlural(Locale locale);

    /**
     * Returns the name for the generation concept in plural. The locale used is the one returned
     * from IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getGenerationConceptNamePlural();

    /**
     * Returns the name for the generation concept in plural. The locale used is the one returned
     * from IpsPlugin.getUsedLanguagePackLocale().
     * 
     * @param usageInsideSentence <code>true</code> if the name will be used inside a sentence
     */
    public String getGenerationConceptNamePlural(boolean usageInsideSentence);

    /**
     * Returns the abbreviation for the generation concept (singular).
     * 
     * @param locale The locale that determines the language in which the abbreviation should be
     *            returned.
     */
    public String getGenerationConceptNameAbbreviation(Locale locale);

    /**
     * Returns the abbreviation for the generation concept (singular). The locale used is the one
     * returned from IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getGenerationConceptNameAbbreviation();

    /**
     * Returns the name for the version concept in singular.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    public String getVersionConceptNameSingular(Locale locale);

    /**
     * Returns the name for the version concept in singular. The locale used is the one returned
     * from IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getVersionConceptNameSingular();

    /**
     * Returns the name for the version concept in plural.
     * 
     * @param locale The locale that determines the language in which the name should be returned.
     */
    public String getVersionConceptNamePlural(Locale locale);

    /**
     * Returns the name for the version concept in plural. The locale used is the one returned from
     * IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getVersionConceptNamePlural();

    /**
     * Returns the abbreviation for the version concept (singular).
     * 
     * @param locale The locale that determines the language in which the abbreviation should be
     *            returned.
     */
    public String getVersionConceptNameAbbreviation(Locale locale);

    /**
     * Returns the abbreviation for the version concept (singular). The locale used is the one
     * returned from IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getVersionConceptNameAbbreviation();

    /**
     * Returns the name for the effective date concept. E.g. in some cases this might be called
     * validFrom.
     * 
     * @param locale The locale that determines the language to use.
     */
    public String getEffectiveDateConceptName(Locale locale);

    /**
     * Returns the name for the effective date concept. E.g. in some cases this might be called
     * validFrom. The locale used is the one returned from IpsPlugin.getUsedLanguagePackLocale().
     */
    public String getEffectiveDateConceptName();

}
