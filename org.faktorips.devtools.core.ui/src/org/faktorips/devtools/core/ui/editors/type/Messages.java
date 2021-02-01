/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.type.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String DefaultValueAndValueSetTabPage_labelDefaultValue;

    public static String AttributesSection_title;
    public static String AttributesSection_openEnumContentInNewEditor;

    public static String AssociationsSection_title;

    public static String MethodsSection_title;

    public static String MethodEditDialog_signatureGroup;
    public static String MethodEditDialog_title;
    public static String MethodEditDialog_signatureTitle;
    public static String MethodEditDialog_labelAccesModifier;
    public static String MethodEditDialog_labelAbstract;
    public static String MethodEditDialog_labelType;
    public static String MethodEditDialog_labelName;
    public static String MethodEditDialog_labelParameters;
    public static String MethodEditDialog_labelChangeOverTimeCheckbox;

    public static String OverrideMethodDialog_labelSelectMethods;
    public static String OverrideMethodDialog_title;
    public static String OverrideMethodDialog_msgEmpty;

    public static String ParametersEditControl_buttonLabelAdd;
    public static String ParametersEditControl_buttonLabelMoveDown;
    public static String ParametersEditControl_buttonLabelMoveUp;
    public static String ParametersEditControl_buttonLabelRemove;
    public static String ParametersEditControl_columnLabelDatatype;
    public static String ParametersEditControl_columnLabelDefaultValue;
    public static String ParametersEditControl_columnLabelName;

    public static String OverrideAttributeDialog_labelNoAttributes;
    public static String OverrideAttributeDialog_labelSelectAttribute;
    public static String OverrideAttributeDialog_title;
}
