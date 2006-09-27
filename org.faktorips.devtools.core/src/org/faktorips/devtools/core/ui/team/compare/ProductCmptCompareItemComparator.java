/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import java.util.Comparator;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

/**
 * Comparator for <code>ProductCmptCompareItem</code>s. Compares the actual
 * <code>IIpsElement</code>s referenced by each compare item. Sorts
 * <code>IProductCmptGeneration</code>s by their validFrom date,
 * <code>IProductCmptRelation</code>s by their name, which by convention contains the qualified
 * name of the referenced product component. <code>IConfigElement</code>s are sorted by their
 * PcTypeAttribute, which is at the same time their name. ConfigElements are put above relations.
 * <p>
 * The sorting of <code>ProductCmptCompareItem</code>s is necessary to ensure that differences in
 * product components (their structures) are consistent with differences in the text representation
 * displayed in the <code>ProductCmptCompareViewer</code>.
 * 
 * @see org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItem
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareItemComparator implements Comparator {
    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2) {
        if (o1 instanceof ProductCmptCompareItem && o2 instanceof ProductCmptCompareItem) {
            IIpsElement element1 = ((ProductCmptCompareItem)o1).getIpsElement();
            IIpsElement element2 = ((ProductCmptCompareItem)o2).getIpsElement();
            // Sort generations by generation number (and thus chronographically)
            if (element1 instanceof IProductCmptGeneration && element2 instanceof IProductCmptGeneration) {
                return ((IProductCmptGeneration)element1).getGenerationNo()
                        - ((IProductCmptGeneration)element2).getGenerationNo();
            }
            // Sort relations by name (qualified name of the target)
            if (element1 instanceof IProductCmptRelation && element2 instanceof IProductCmptRelation) {
                return element1.getName().compareTo(element2.getName());
            }
            // Sort configElements by type (which is at the same time their name)
            if (element1 instanceof IConfigElement && element2 instanceof IConfigElement) {
                String ce1 = ((IConfigElement)element1).getPcTypeAttribute();
                String ce2 = ((IConfigElement)element2).getPcTypeAttribute();
                return ce1.compareTo(ce2);
            }
        }
        return 0;
    }

}
