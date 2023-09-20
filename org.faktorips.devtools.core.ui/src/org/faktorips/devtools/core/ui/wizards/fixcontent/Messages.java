/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.fixcontent;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.fixcontent.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String FixContentWizard_title;
    public static String FixContentWizard_labelNewContentType;
    public static String FixContentWizard_msgChooseContentType;
    public static String FixContentWizard_chooseContentTypePageTitle;
    public static String FixEnumContentWizard_chosenEnumTypeAbstract;
    public static String FixEnumContentWizard_chosenEnumTypeValuesArePartOfModel;
    public static String FixContentWizard_chosenContentTypeEmpty;
    public static String FixContentWizard_chosenContentTypeDoesNotExist;
    public static String FixContentWizard_msgAssignColumns;
    public static String FixContentWizard_assignColumnsPageTitle;
    public static String FixContentWizard_assignColumnsCreateNewColumn;
    public static String FixContentWizard_assignColumnsDuplicateColumnAssigned;
    public static String FixContentWizard_assignColumnsAttributeNotAssigned;
    public static String FixContentWizard_assignColumnsDeleteColumnsConfirmationTitle;
    public static String FixContentWizard_assignColumnsDeleteColumnsConfirmationMessageSingular;
    public static String FixContentWizard_assignColumnsDeleteColumnsConfirmationMessagePlural;
    public static String FixContentWizard_assignColumnsGroup;
    public static String FixContentWizard_assignColumnMismatchPageTitle;
    public static String FixContentWizard_assignColumnsDataTypesNotMatching;
    public static String FixContentWizard_checkboxFillNewColumnsWithNull;

    public static String FixContentWizard_messageMultilingual;
    public static String FixContentWizard_messageNoMultilingual;

    public static String EnumTypeString;
    public static String TableStructureString;
}
