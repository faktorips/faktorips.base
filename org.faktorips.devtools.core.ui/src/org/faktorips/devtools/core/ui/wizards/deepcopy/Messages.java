/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.deepcopy.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.

    }

    public static String DeepCopyLabelProvider_textUndefined;
    public static String DeepCopyWizard_title;
    public static String SameOperationValidator_errorMsgInvalidSelectionOfProductCmpt;
    public static String SameOperationValidator_SameOperationValidator_errorMsgParentChildOperationMismatch;
    public static String SourcePage_columnNameNewName;
    public static String SourcePage_columnNameOperation;
    public static String SourcePage_columnNameSourceStructure;
    public static String SourcePage_copyFrom;
    public static String SourcePage_copyTo;
    public static String SourcePage_deactivationHintText;
    public static String SourcePage_errorPrefixWorkingDateFormat;
    public static String SourcePage_errorWorkingDateFormat;
    public static String SourcePage_labelSourceValidFrom;
    public static String SourcePage_labelGroupTableContents;
    public static String SourcePage_labelRadioBtnCopyTableContents;
    public static String SourcePage_labelRadioBtnCreateEmptyTableContents;
    public static String SourcePage_labelCheckboxCopyExistingGenerations;
    public static String SourcePage_tooltipCheckboxCopyExistingGenerations;
    public static String SourcePage_labelTargetRoot;
    public static String SourcePage_msgBadTargetPackage;
    public static String SourcePage_msgCopyNotPossible;
    public static String SourcePage_msgMissingSourceFolder;
    public static String SourcePage_msgNothingSelected;
    public static String SourcePage_msgSearchPatternNotFound;
    public static String SourcePage_msgSelectSourceFolder;
    public static String SourcePage_msgWarningTargetWillBeCreated;
    public static String SourcePage_tables;
    public static String SourcePage_title;
    public static String SourcePage_description;
    public static String SourcePage_description_copy;
    public static String ReferenceAndPreviewPage_descritionPreviewNewCopy;
    public static String ReferenceAndPreviewPage_descritionPreviewNewGeneration;
    public static String ReferenceAndPreviewPage_title;
    public static String ReferenceAndPreviewPage_labelValidFrom;
    public static String ReferenceAndPreviewPage_labelTargetPackage;
    public static String ReferenceAndPreviewPage_labelSearchPattern;
    public static String ReferenceAndPreviewPage_labelReplacePattern;
    public static String ReferenceAndPreviewPage_msgCanNotCreateFile;
    public static String ReferenceAndPreviewPage_msgFileAllreadyExists;
    public static String ReferenceAndPreviewPage_msgNameCollision;
    public static String ReferenceAndPreviewPage_errorLabelInsert;
    public static String SourcePage_msgCircleRelation;
    public static String SourcePage_msgCircleRelationShort;
    public static String SourcePage_msgPatternNotFound;
    public static String SourcePage_msgInvalidPattern;
    public static String SourcePage_msgPleaseEnterNewWorkingDateNewCopy;
    public static String SourcePage_msgPleaseEnterNewWorkingDateNewGeneration;
    public static String SourcePage_msgReplaceTextNotFound;
    public static String SourcePage_operationCopy;
    public static String SourcePage_operationLink;
    public static String SourcePage_searchAndReplace;
    public static String ReferenceAndPreviewPage_msgCircleDetected;
    public static String DeepCopyWizard_titleNewVersion;
    public static String SourcePage_titleNewVersion;
    public static String ReferenceAndPreviewPage_labelVersionId;
    public static String ReferenceAndPreviewPage_msgValidateCopy;

}
