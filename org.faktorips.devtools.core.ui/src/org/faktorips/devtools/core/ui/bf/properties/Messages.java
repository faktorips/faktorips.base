/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
