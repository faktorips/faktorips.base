package org.faktorips.devtools.core.model;

import java.util.Locale;

/**
 * Naming convention for product changes over time. 
 * <p>
 * We distinguish between two different types of product changes over time.
 * <ol>
 *   <li>Version</li>
 *   <p>
 *   A new product version affects only new insurance policies. Exististing policies are not affected
 *   by a new product version in any way. Existing policies have to be explicitly converted
 *   to the product version, e.g. by a batch program. Perhaps the customer rejects changing
 *   to the product version and wants to remain with the old one. 
 *   <li>Generation</li>
 *   <p>
 *   A new product generation affects exististing policies (and of cource new policies).
 *   If a policy is changed in a way where product data is neccessary, the appropriate product 
 *   is determined automatically based on the change's effective date. 
 * </ol>
 * <p>
 * While the above concepts are everywhere the same, there is no commonly accepted naming standard for them.
 * In Germany a standard has been defined by the GDV as part of the standard architecture VAA. In FaktorIPS'
 * sourcecode the names are used according to the GDV definition (as described above). However, as their is no widely
 * accepted standard, this class encapsulates the names used for the two concept. The names provided by the
 * naming convention are used in the UI.
 * 
 * @author Jan Ortmann
 */
public interface IChangesInTimeNamingConvention {
	
	/**
	 * Returns the conventions' identification.
	 */
	public String getId();
	
	/**
	 * Returns the conventions' name, used to present it itself to the user.
	 */
	public String getName();

	/**
	 * Returns the name for the generation concept in singular.
	 * 
	 * @param locale The locale that determines the language in which
	 * the name should be returned.
	 */
	public String getGenerationConceptNameSingular(Locale locale);
	
	/**
	 * Returns the name for the generation concept in plural.
	 * 
	 * @param locale The locale that determines the language in which
	 * the name should be returned.
	 */
	public String getGenerationConceptNamePlural(Locale locale);

	/**
	 * Returns the abbreviation for the generation concept (singular).
	 * 
	 * @param locale The locale that determines the language in which
	 * the abbreivation should be returned.
	 */
	public String getGenerationConceptNameAbbreviation(Locale locale);

	/**
	 * Returns the name for the version concept in singular.
	 * 
	 * @param locale The locale that determines the language in which
	 * the name should be returned.
	 */
	public String getVersionConceptNameSingular(Locale locale);
	
	/**
	 * Returns the name for the version concept in plural.
	 * 
	 * @param locale The locale that determines the language in which
	 * the name should be returned.
	 */
	public String getVersionConceptNamePlural(Locale locale);

	/**
	 * Returns the abbreviation for the version concept (singular).
	 * 
	 * @param locale The locale that determines the language in which
	 * the abbreivation should be returned.
	 */
	public String getVersionConceptNameAbbreviation(Locale locale);

}
