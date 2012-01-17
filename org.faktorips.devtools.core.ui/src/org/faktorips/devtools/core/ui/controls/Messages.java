/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.controls.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String FolderSelectionControl_select_folder_message;

    public static String FolderSelectionControl_select_folder_title;

    public static String RangeEditControl_titleRange;

    public static String RangeEditControl_labelMinimum;

    public static String RangeEditControl_labelMaximum;

    public static String RangeEditControl_labelStep;

    public static String IpsPckFragmentRefControl_titleBrowse;

    public static String EnumRefControl_text;

    public static String EnumRefControl_title;

    public static String EnumValueSetEditControl_titleValues;

    public static String EnumValueSetEditControl_colName_1;

    public static String EnumValueSetEditControl_colName_2;

    public static String PcTypeRefControl_title;

    public static String PcTypeRefControl_description;

    public static String ValueSetEditControl_labelAllowedValueSet;

    public static String ValueSetEditControl_labelType;

    public static String ValueSetEditControl_UnspecifiedValueSet_Comment;

    public static String ValueSetSpecificationControl_specifyBoundsValues;

    public static String TableStructureRefControl_title;

    public static String TableStructureRefControl_description;

    public static String TableContentsRefControl_title;

    public static String TableContentsRefControl_description;

    public static String TableContentsUsageRefControl_button_new;

    public static String IpsPckFragmentRootRefControl_title;

    public static String DatatypeRefControl_title;

    public static String DescriptionControl_title;

    public static String IpsObjectCompletionProcessor_msgNoProject;

    public static String IpsObjectCompletionProcessor_msgInternalError;

    public static String ProductCmptRefControl_title;

    public static String ProductCmptRefControl_description;

    public static String IpsObjectRefControl_title;

    public static String ProductCmptTypeRefControl_title;

    public static String ProductCmptTypeRefControl_description;

    public static String ListChooser_labelAvailableValues;

    public static String ListChooser_lableChoosenValues;

    public static String ListChooser_buttonUp;

    public static String ListChooser_buttonDown;

    public static String RangeEditControl_labelIncludeNull;

    public static String FileSelectionControl_titleBrowse;

    public static String IpsProjectRefControl_labelBrowse;

    public static String IpsProjectRefControl_labelDialogMessage;

    public static String IpsProjectRefControl_msgNoProjectsFound;

    public static String IpsProjectRefControl_msgNoProjectSelected;

    public static String IpsProjectRefControl_labelDialogTitle;

    public static String TestCaseTypeRefControl_title;

    public static String TestCaseTypeRefControl_description;

    public static String EnumTypeRefControl_title;

    public static String EnumTypeRefControl_description;

}
