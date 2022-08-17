/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.tablestructure.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String RangesSection_title;
    public static String ColumnEditDialog_title;
    public static String ColumnEditDialog_pageTitle;
    public static String ColumnEditDialog_labelName;
    public static String ColumnEditDialog_labelDatatype;
    public static String StructurePage_title;
    public static String IndicesSection_title;
    public static String ForeignKeysSection_title;
    public static String ColumnsSection_title;
    public static String KeyEditDialog_generalTitle;
    public static String KeyEditDialog_labelReferenceStructure;
    public static String KeyEditDialog_labelReferenceUniqueKey;
    public static String KeyEditDialog_labelKeyItems;
    public static String KeyEditDialog_groupTitle;
    public static String KeyEditDialog_checkboxUniqueKey;
    public static String KeyEditDialogForeignKey_titleText;
    public static String KeyEditDialogIndices_titleText;
    public static String RangeEditDialog_title;
    public static String RangeEditDialog_generalTitle;
    public static String RangeEditDialog_labelType;
    public static String RangeEditDialog_groupTitle;
    public static String RangeEditDialog_labelFrom;
    public static String RangeEditDialog_labelTo;
    public static String RangeEditDialog_groupAvailableColsTitle;
    public static String TableStructureEditor_title;
    public static String TableStructureEditorStructurePage_warningUniqueKeysWithSameDatatypes;
    public static String RangeEditDialog_RangeEditDialog_parameterName;
    public static String GeneralInfoSection_labelGeneralInfoSection;
    public static String GeneralInfoSection_labelTableType;

}
