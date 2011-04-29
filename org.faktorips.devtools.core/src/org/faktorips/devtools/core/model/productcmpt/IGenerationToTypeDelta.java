/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

/**
 * A product component generation /policy component type delta describes the difference between what
 * a product component generation based on specific product component type should contain and what
 * it actually contains.
 * 
 * @author Jan Ortmann
 */
public interface IGenerationToTypeDelta {

    /**
     * Returns the product component generation this delta was computed for.
     */
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns the product component type this delta was computed for.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Returns <code>true</code> if the delta is empty. In this case the product component conforms
     * to the product component type it is based on.
     */
    public boolean isEmpty();

    /**
     * Returns the delta entries that describe the delta details. Each entry reports a difference
     * between the generation and the product component type.
     */
    public IDeltaEntry[] getEntries();

    /**
     * Returns all entries for the given type.
     */
    public IDeltaEntry[] getEntries(DeltaType type);

    /**
     * Fixes the generation so that it conforms to the type afterwards.
     * <p>
     * For example if the type contains a new attribute but the product component generation. has
     * not matching attribute value, this method creates the attribute value.
     */
    public void fix();

}
