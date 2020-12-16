/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.productcmpt.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ConfigElementType_policyAttribute;

    public static String DeltaType_valueHolderMismatch;

    public static String DeltaType_LinksWithWrongParent;

    public static String DeltaType_LinksNotFoundInTheModel;
    public static String DeltaType_missingTemplateLink;

    public static String DeltaType_missingValue;

    public static String DeltaType_ConfigWithoutValidationRule;

    public static String DeltaType_datatypeMissmatch;

    public static String DeltaType_hiddenAttributeMismatch;

    public static String DeltaType_MissingVRuleConfig;
    public static String DeltaType_propertiesNotFoundInTheModel;
    public static String DeltaType_propertiesWithTypeMismatch;
    public static String DeltaType_ValueSetMismatches;

    public static String ProductCmptValidations_error_inconsistentTemplateType;

    public static String ProductCmptValidations_error_invalidTemplate;

    public static String ProductCmptValidations_error_validFromTemplate;

    public static String ProductCmptValidations_typeDoesNotExist;
    public static String ProductCmptValidations_typeIsAbstract;

    public static String DeltaType_multilingualMismatch;

    public static String DeltaType_invalidGenerations;

    public static String DeltaType_removedTemplateLink;

    public static String TemplateValidations_error_templateCycle;
}
