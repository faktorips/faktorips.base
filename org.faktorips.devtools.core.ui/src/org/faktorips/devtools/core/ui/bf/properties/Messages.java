/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf.properties;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.bf.properties.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String BusinessFunctionRefControl_dialogMessage;
    public static String BusinessFunctionRefControl_title;
    public static String CallBusinessFunctionActionPropertySection_bfLabel;
    public static String CallMethodDecisionPropertySection_labelDatatype;
    public static String CallMethodPropertySection_MethodLabel;
    public static String CallMethodPropertySection_parameterLabel;
    public static String ControlFlowPropertySection_valueLabel;
    public static String DecisionPropertySection_datatypeLabel;
    public static String NamedOnlyBFElementsPropertySection_nameLabel;
    public static String ParameterMethodRefControl_ChooseMethodLabel;
    public static String ParameterMethodRefControl_ChooseMethodTitle;
    public static String ParameterMethodRefControl_dialogDescription;
    public static String ParametersEditControl_AddLabel;
    public static String ParametersEditControl_datatypeLabel;
    public static String ParametersEditControl_parameterNameLabel;
    public static String ParametersEditControl_RemoveLabel;

}
