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

    public static String UpdateValidFromSourcePage_pageTitle;
    public static String UpdateValidFromSourcePage_description;
    public static String UpdateValidFromSourcePage_generationID;
    public static String UpdateValidFromSourcePage_validFrom;
    public static String UpdateValidFromSourcePage_productComponent;
    public static String UpdateValidFromSourcePage_targetGroup;
    public static String UpdateValidFromSourcePage_sourceGroup;
    public static String UpdateValidFromSourcePage_emptyValidFomDateError;
    public static String UpdateValidFromSourcePage_emptyVersionIdError;
    public static String UpdateValidFromSourcePage_missingStructureError;
    public static String UpdateValidFromSourcePage_changeGenerationIDCheckbox;
    public static String UpdateValidFromSourcePage_ChangeAttributesCheckbox;
    public static String UpdateValidFromSourcePage_ValidFromDateFormatError;
    public static String UpdateValidFromSourcePage_ValidFromAfterFirstGenerationError;

    public static String UpdateValidFromPreviewPage_description;
    public static String UpdateValidFromPreviewPage_validFrom;

}
