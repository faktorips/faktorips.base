/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.search.product.messages"; //$NON-NLS-1$
    public static String ProductSearchConditionsTableViewerProvider_argument;
    public static String ProductSearchConditionsTableViewerProvider_conditionType;
    public static String ProductSearchConditionsTableViewerProvider_element;
    public static String ProductSearchConditionsTableViewerProvider_operator;
    public static String ProductSearchPage_labelAddConditionButton;
    public static String ProductSearchPage_labelChooseProductComponentType;
    public static String ProductSearchPage_labelProductComponent;
    public static String ProductSearchPage_labelProductComponentType;
    public static String ProductSearchPage_labelRemoveConditionButton;
    public static String ProductSearchPage_labelSearchConditions;
    public static String ProductSearchQuery_1;
    public static String ProductSearchQuery_faktorIpsProductSearchLabel;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        //
    }
}
