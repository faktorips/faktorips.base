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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.team.compare.productcmpt.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ProductCmptCompareItemCreator_StructureViewer_title;
    public static String ProductCmptCompareViewer_CompareViewer_title;

    public static String ProductCmptCompareItem_Attribute;

    public static String ProductCmptCompareItem_Formula;

    public static String ProductCmptCompareItem_FormulaHeader;

    public static String ProductCmptCompareItem_Relation;

    public static String ProductCmptCompareItem_SourceFile;

    public static String ProductCmptCompareItem_ValueSet;

    public static String ProductCmptCompareItem_Default;

    public static String ProductCmptCompareItem_VRule_active;

    public static String ProductCmptCompareItem_VRule_inactive;

    public static String ProductCmptCompareItem_RuleLabel;

    public static String ProductCmptCompareItem_TableContentsLabel;

    public static String ProductCmptCompareItem_TableUsagesHeader;

    public static String ProductCmptCompareItem_DefaultsAndValueSets;

}
