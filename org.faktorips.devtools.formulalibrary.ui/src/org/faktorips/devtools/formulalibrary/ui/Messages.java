/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.formulalibrary.ui.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

    public static String FormulaFunctionListSection_title;
    public static String FormulaLibraryEditor_title;
    public static String FormulaLibraryEditorPage_DescriptionsTitle;
    public static String FormulaLibraryEditorPage_FormulaTitle;
    public static String FormulaLibraryEditorPage_SignatureTitle;
    public static String FormulaLibraryContentPage_title;
    public static String SelectedFormulaDescriptionSection_DescriptionsLabel;
    public static String SelectedFormulaEquationSection_FormulaLabel;
    public static String SelectedFormulaSignatureSection_ReturnTypSerchButton;
    public static String SelectedFormulaSignatureSection_AddButton;
    public static String SelectedFormulaSignatureSection_DatatypeColumn;
    public static String SelectedFormulaSignatureSection_FormulaNameLabel;
    public static String SelectedFormulaSignatureSection_MethodNameLabel;
    public static String SelectedFormulaSignatureSection_MoveDownButton;
    public static String SelectedFormulaSignatureSection_MoveUpButton;
    public static String SelectedFormulaSignatureSection_NameColumn;
    public static String SelectedFormulaSignatureSection_ParamtersLabel;
    public static String SelectedFormulaSignatureSection_RemoveButton;
    public static String SelectedFormulaSignatureSection_ReturnTypeLabel;

    public static String NewFormulaLibrary_title;
    public static String NewFormulaLibrary_msgDuplicateLibrary;

}
