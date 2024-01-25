/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String DocumentationPage_inheritSupertypeMessage;

    public static String IpsObjectEditor_fileHasChangesOnDiskMessage;
    public static String IpsObjectEditor_fileHasChangesOnDiskNoButton;
    public static String IpsObjectEditor_fileHasChangesOnDiskTitle;
    public static String IpsObjectEditor_fileHasChangesOnDiskYesButton;

    public static String IpsObjectEditor_multipleErrorMessages;
    public static String IpsObjectEditor_multipleWarningMessages;
    public static String IpsObjectEditor_multipleInformationMessages;
    public static String IpsObjectEditor_singleErrorMessage;
    public static String IpsObjectEditor_singleWarningMessage;
    public static String IpsObjectEditor_singleInformationMessage;

    public static String IpsPartEditDialog_tabItemDocumentation;
    public static String IpsPartEditDialog_groupLabel;
    public static String IpsPartEditDialog_groupDescription;
    public static String IpsPartEditDialog_groupVersion;
    public static String IpsPartEditDialog_versionAvailableSince;
    public static String IpsPartEditDialog_versionTooltip;

    public static String IpsPartEditDialog_moreMessagesInTooltip;

    public static String IpsPartsComposite_submenuRefactor;
    public static String IpsPartsComposite_labelRenameRefactoring;
    public static String IpsPartsComposite_labelPullUpRefactoring;
    public static String IpsPartsComposite_buttonNew;
    public static String IpsPartsComposite_buttonEdit;
    public static String IpsPartsComposite_buttonShow;
    public static String IpsPartsComposite_buttonDelete;
    public static String IpsPartsComposite_buttonUp;
    public static String IpsPartsComposite_buttonDown;
    public static String IpsPartsComposite_override;
    public static String IpsPartsComposite_deleteElement;
    public static String IpsPartsComposite_deleteElementConfirm;

    public static String IpsPartEditDialog2_inheritOverriddenElementMessage;

    public static String UnparsableFilePage_fileContentIsNotParsable;

    public static String UnreachableFilePage_msgUnreachableFile;

    public static String DescriptionSection_description;

    public static String DeprecationSection_label;
    public static String DeprecationSection_isDeprecated;
    public static String DeprecationSection_version;
    public static String DeprecationSection_versionTooltip;
    public static String DeprecationSection_forRemoval;

    public static String LabelSection_label;

    public static String LabelEditComposite_tableColumnHeaderLanguage;
    public static String LabelEditComposite_tableColumnHeaderLabel;
    public static String LabelEditComposite_tableColumnHeaderPluralLabel;
    public static String SearchBar_searchFieldHint;
    public static String SearchSelectionBar_searchBarTitle;

}
