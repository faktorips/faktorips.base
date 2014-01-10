/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.bf;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.bf.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String BusinessFunctionLabelProvider_bf;
    public static String BusinessFunctionLabelProvider_callBfAction;
    public static String BusinessFunctionLabelProvider_callMethodAction;
    public static String BusinessFunctionLabelProvider_callMethodDecision;
    public static String BusinessFunctionLabelProvider_controlFlow;
    public static String BusinessFunctionLabelProvider_decision;
    public static String BusinessFunctionLabelProvider_inlineAction;
    public static String BusinessFunctionLabelProvider_merge;
    public static String BusinessFunctionLabelProvider_parameters;
    public static String PaletteBuilder_bfCallActionDesc;
    public static String PaletteBuilder_controlflow;
    public static String PaletteBuilder_controlFlowDesc;
    public static String PaletteBuilder_decisionDesc;
    public static String PaletteBuilder_endDesc;
    public static String PaletteBuilder_inlineActionDesc;
    public static String PaletteBuilder_mergeDesc;
    public static String PaletteBuilder_methodCallActionDesc;
    public static String PaletteBuilder_methodCallDecisionDesc;
    public static String PaletteBuilder_startDec;

}
