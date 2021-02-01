/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.type.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String AssociationType_label_aggregation;
    public static String AssociationType_label_association;
    public static String AssociationType_label_composition_detail_to_master;
    public static String AssociationType_label_composition_master_to_detail;
    public static String ProductCmptPropertyType_defaultValueAndValueSet;
    public static String ProductCmptPropertyType_fomula;
    public static String ProductCmptPropertyType_productAttribute;
    public static String ProductCmptPropertyType_tableUsage;
    public static String ProductCmptPropertyType_ValidationRule;

}
