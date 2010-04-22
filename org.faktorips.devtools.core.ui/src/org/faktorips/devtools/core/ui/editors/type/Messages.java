/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.type.messages"; //$NON-NLS-1$

    private Messages() {
        // not allowed to instantiate
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String DefaultValueAndValueSetTabPage_labelDefaultValue;
    public static String MethodEditDialog_signatureGroup;
    public static String MethodsSection_title;
    public static String MethodsSection_button;
    public static String MethodEditDialog_title;
    public static String MethodEditDialog_signatureTitle;
    public static String MethodEditDialog_labelAccesModifier;
    public static String MethodEditDialog_labelAbstract;
    public static String MethodEditDialog_labelType;
    public static String MethodEditDialog_labelName;
    public static String MethodEditDialog_labelParameters;
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

}
