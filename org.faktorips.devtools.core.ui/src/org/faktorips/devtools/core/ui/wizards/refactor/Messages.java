/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.refactor.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ElementNames_Attribute;
    public static String ElementNames_Method;
    public static String ElementNames_Association;
    public static String ElementNames_Type;
    public static String ElementNames_EnumLiteralNameAttributeValue;
    public static String ElementNames_ValidationRule;

    public static String IpsRenameRefactoringWizard_title;
    public static String IpsMoveRefactoringWizard_title;
    public static String IpsPullUpRefactoringWizard_title;

    public static String RenameUserInputPage_message;
    public static String RenameUserInputPage_labelNewName;
    public static String RenameUserInputPage_labelNewPluralName;
    public static String RenameUserInputPage_labelRefactorRuntimeId;

    public static String MoveUserInputPage_message;
    public static String MoveUserInputPage_labelChooseDestination;
    public static String MoveUserInputPage_msgSelectOnlyPackages;

    public static String PullUpUserInputPage_message;
    public static String PullUpUserInputPage_labelChooseDestination;

}
