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

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;

/**
 * A product component generation /policy component type delta describes the difference between what
 * a product component generation based on specific policy component type should contain and what it
 * actually contains.
 */
public interface IProductCmptGenerationPolicyCmptTypeDelta {

    /**
     * Returns the product component generation this delta was computed for.
     */
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns the policy component type this delta was computed for.
     */
    public IPolicyCmptType getPolicyCmptType();

    /**
     * Returns true if the delta is empty. The product component conforms to the policy component
     * type it is based on.
     */
    public boolean isEmpty();

    /**
     * Returns the attributes defined in the policy component type that aren't defined in the
     * product component.
     */
    public IPolicyCmptTypeAttribute[] getAttributesWithMissingConfigElements();

    /**
     * Returns the elements where the type defined in the product component does not fit the one
     * defined in the policy component type.
     */
    public IConfigElement[] getTypeMismatchElements();

    /**
     * Returns the config elements with value set type that differ form the value set's type found
     * in the corresponding policy component attribute.
     */
    public IConfigElement[] getElementsWithValueSetMismatch();

    /**
     * Returns the product component elements that don't have a counterpart in the policy component
     * type.
     */
    public IConfigElement[] getConfigElementsWithMissingAttributes();

    /**
     * Returns the product component generation's relations where the corresponding relation in the
     * policy component type can't be found.
     */
    public IProductCmptLink[] getLinksWithMissingAssociations();

    /**
     * @return All table structures of the policy component type where no corresponding table
     *         content usages in the product component was found.
     */
    public ITableStructureUsage[] getTableStructureUsagesWithMissingContentUsages();

    /**
     * @return All table content usages of the product component where no corresponding table
     *         structure usages in the policy component type was found.
     */
    public ITableContentUsage[] getTableContentUsagesWithMissingStructureUsages();
}
