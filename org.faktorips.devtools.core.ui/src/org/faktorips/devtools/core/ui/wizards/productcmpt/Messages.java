/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.productcmpt.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String InferTemplateOperation_progress_inferringTemplate;
    public static String InferTemplateOperation_progress_save;
    public static String InferTemplateValidator_error_invalidValidFrom;
    public static String NewProductCmptWizard_title;
    public static String NewProductCmptWizard_copyTitle;
    public static String NewProductTemplateWizard_NoCommonType_message;
    public static String NewProductTemplateWizard_NoCommonType_title;
    public static String NewProductTemplateWizard_Precondition_TemplateDefined_message;
    public static String NewProductTemplateWizard_Precondition_TemplateDefined_title;
    public static String NewProdutCmptValidator_msg_emptyKindId;
    public static String NewProdutCmptValidator_msg_emptyVersionId;
    public static String NewProdutCmptValidator_msg_invalidAddToGeneration;
    public static String NewProdutCmptValidator_msg_invalidBaseType;
    public static String NewProdutCmptValidator_msg_invalidFullName;
    public static String NewProdutCmptValidator_msg_invalidKindId;
    public static String NewProdutCmptValidator_msg_invalidPackage;
    public static String NewProdutCmptValidator_msg_invalidProject;
    public static String NewProdutCmptValidator_msg_invalidSelectedType;
    public static String NewProdutCmptValidator_msg_invalidTypeAddTo;
    public static String NewProdutCmptValidator_msg_invalidVersionId;
    public static String NewProdutCmptValidator_msg_invalidEffectiveDate;
    public static String ProductCmptPage_label_effectiveFrom;
    public static String ProductCmptPage_label_name;
    public static String ProductCmptPage_label_versionSuffix;
    public static String ProductCmptPage_label_runtimeId;
    public static String ProductCmptPage_label_selectType;
    public static String ProductCmptPage_label_selectTypeAndTemplate;
    public static String ProductCmptPage_msg_fullName;
    public static String ProductCmptPage_name;
    public static String ProductCmptPage_title;
    public static String ProductCmptPage_copyTitle;
    public static String TypeSelectionPage_checkboxTemplate;
    public static String TypeSelectionPage_label_project;
    public static String TypeSelectionPage_label_type;
    public static String TypeSelectionPage_name;
    public static String TypeSelectionPage_title;

}
