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

package org.faktorips.devtools.core.model;

import java.util.Locale;

import org.eclipse.swt.graphics.Image;

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
	 * Returns the conventions' identification.
	 */
	public String getId();
	
	/**
	 * Returns the conventions' name, used to present it to the user.
	 */
	public String getName(Locale locale);

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
	 * Returns an image that can be used to represent an item of the
	 * genration concept.
	 * 
	 * @param locale The locale that determines the langugage to use. This
	 * is neccessary because chars can be contained in the image.
	 */
	public Image getGenerationConceptImage(Locale locale);
	
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
	
	/**
	 * Returns an image that can be used to represent an item of the
	 * version concept.
	 * 
	 * @param locale The locale that determines the langugage to use. This
	 * is neccessary because chars can be contained in the image.
	 */
	public Image getVersionConceptImage(Locale locale);

	/**
	 * Returns the name for the effective date concept. E.g. in some
	 * cases this might be called validFrom.
	 */
	public String getEffectiveDateConceptName(Locale locale);

}
