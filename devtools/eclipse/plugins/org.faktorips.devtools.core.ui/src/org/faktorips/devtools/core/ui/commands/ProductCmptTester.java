/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * This tester is used to test {@link IProductCmpt} properties. The properties to test are defined
 * in the org.faktorips.devtools.core.ui.plugin.xml file.
 */
public class ProductCmptTester extends PropertyTester {

    public static final String ALLOW_GENERATION = "allowGenerations"; //$NON-NLS-1$
    public static final String IS_PRODUCT_TEMPLATE = "isProductTemplate"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof IProductCmpt productCmpt) {
            if (ALLOW_GENERATION.equals(property)) {
                return allowGenerations(productCmpt);
            }
            if (IS_PRODUCT_TEMPLATE.equals(property)) {
                return isProductTemplate(productCmpt);
            }
        }
        return false;
    }

    private boolean allowGenerations(IProductCmpt productCmpt) {
        return productCmpt.allowGenerations();
    }

    private boolean isProductTemplate(IProductCmpt productCmpt) {
        return productCmpt.isProductTemplate();
    }

}
