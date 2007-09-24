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

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Comparator;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.ITableContentUsage;

/**
 * Comparator for <code>ProductCmptCompareItem</code>s. Compares the actual
 * <code>IIpsElement</code>s referenced by each compare item. Sorts
 * <code>IProductCmptGeneration</code>s by their validFrom date. <code>IConfigElement</code>s
 * and <code>ITableContentUsage</code>s are sorted in the following order: product attributes,
 * table usages, formulas, policy attributes. Attributes are placed above relations in each
 * generation. <code>IProductCmptRelations</code> are <em>not</em> sorted, instead their natural
 * order (in the XML file) is maintained.
 * <p>
 * The sorting of <code>ProductCmptCompareItem</code>s is necessary to ensure that differences in
 * product components (their structures) are consistent with differences in the text representation
 * displayed in the <code>ProductCmptCompareViewer</code>. Moreover the representation must be
 * consistent with the ProductCmptEditor.
 * 
 * @see org.faktorips.devtools.core.ui.team.compare.productcmpt.ProductCmptCompareItem
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
            if ((element1 instanceof IConfigElement||element1 instanceof ITableContentUsage || element1 instanceof IFormula) 
                    && (element2 instanceof IConfigElement||element2 instanceof ITableContentUsage || element2 instanceof IFormula)) {
                int first= getOrderNumber(element1);
                int second= getOrderNumber(element2);
                return first-second;
            }
        }
        return 0;
    }

    /** 
     * Sorts configElements and tableUsages in the following oder:
     * <ul>
     *  <li>product attribute</li>
     *  <li>tableUsage</li>
     *  <li>formula</li>
     *  <li>policy attribute</li>
     * </ul>
     * @param element
     * @return
     */
    private int getOrderNumber(IIpsElement element){
        if(element instanceof IConfigElement){
            IConfigElement ce= (IConfigElement) element;
            if(ce.getType()==ConfigElementType.PRODUCT_ATTRIBUTE){
                return 1;
            }else if(ce.getType()==ConfigElementType.POLICY_ATTRIBUTE){
                return 4;
            }
        } else if(element instanceof IFormula){
            return 3;
        } else if(element instanceof ITableContentUsage){
            return 2;
        }
        return 5;
    }

}
