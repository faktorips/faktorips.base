/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Comparator;

import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.productcmpt.PropertyValueComparator;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;

/**
 * Comparator for <code>ProductCmptCompareItem</code>s. Compares the actual <code>IIpsElement</code>
 * s referenced by each compare item.
 * <p>
 * PropertyValues of <code>IProductCmpt</code> are placed above <code>IProductCmptGeneration</code>.
 * Then sorts <code>IProductCmptGeneration</code> s by their validFrom date.
 * <p>
 * <code>IConfigElement</code>s and <code>ITableContentUsage</code>s are sorted in the following
 * order: product attributes, table usages, formulas, policy attributes. Attributes are placed above
 * relations. <code>IProductCmptRelations</code> are <em>not</em> sorted, instead their natural
 * order (in the XML file) is maintained.
 * <p>
 * The sorting of <code>ProductCmptCompareItem</code>s is necessary to ensure that differences in
 * product components (their structures) are consistent with differences in the text representation
 * displayed in the <code>ProductCmptCompareViewer</code>. Moreover the representation must be
 * consistent with the ProductCmptEditor.
 * 
 * @see org.faktorips.devtools.core.ui.team.compare.productcmpt.ProductCmptCompareItem
 */
public class ProductCmptCompareItemComparator implements Comparator<AbstractCompareItem> {

    private static final Class<?>[] TYPE_ORDER = { IPropertyValue.class, IProductCmptLink.class,
            IProductCmptGeneration.class };

    private static final PropertyValueComparator PROPERTY_VALUE_COMPARATOR = new PropertyValueComparator();

    @Override
    public int compare(AbstractCompareItem pci1, AbstractCompareItem pci2) {
        IIpsElement element1 = pci1.getIpsElement();
        IIpsElement element2 = pci2.getIpsElement();

        // Sort generations by generation number (and thus chronologically)
        if (element1 instanceof IProductCmptGeneration && element2 instanceof IProductCmptGeneration) {
            return ((IProductCmptGeneration)element1).getGenerationNo()
                    - ((IProductCmptGeneration)element2).getGenerationNo();
        } else if ((element1 instanceof IPropertyValue) && (element2 instanceof IPropertyValue)) {
            return comparePropertyValues((IPropertyValue)element1, (IPropertyValue)element2);
        } else {
            return typeIndexOf(element1) - typeIndexOf(element2);
        }
    }

    private int comparePropertyValues(IPropertyValue element1, IPropertyValue element2) {
        return PROPERTY_VALUE_COMPARATOR.compare(element1, element2);
    }

    private int typeIndexOf(IIpsElement part) {
        int i = 0;
        for (Class<?> type : TYPE_ORDER) {
            if (type.isAssignableFrom(part.getClass())) {
                return i;
            }
            i++;
        }
        return -1;
    }

}
