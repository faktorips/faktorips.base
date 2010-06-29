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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.tablecontents.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ContentPage_title;

    public static String NavigateToTableStructureAction_Label;

    public static String NavigateToTableStructureAction_ToolTip;

    public static String NewRowAction_Label;

    public static String NewRowAction_Tooltip;

    public static String TableContentsEditor_TableContentsEditor_title2;

    public static String ContentPage_msgMissingStructure;

    public static String ContentPage_msgNoStructureFound;

    public static String ContentPage_titleMissingColumns;

    public static String ContentPage_msgAddMany;

    public static String ContentPage_titleMissingColumn;

    public static String ContentPage_msgAddOne;

    public static String ContentPage_msgRemoveOne;

    public static String ContentPage_titleTooMany;

    public static String ContentPage_msgRemoveMany;

    public static String ContentPage_msgCantShowContent;

    public static String ContentPage_errorNoDuplicateIndices;

    public static String ContentPage_errorIndexOutOfRange;

    public static String ContentPage_errorInvalidValueOne;

    public static String ContentPage_errorInvalidValueMany;

    public static String ContentPage_errorTooManyOne;

    public static String ContentPage_errorTooManyMany;

    public static String ContentPage_errorOneMore;

    public static String ContentPage_errorManyMore;

    public static String SetStructureDialog_titleChooseTableStructure;

    public static String SetStructureDialog_labelNewStructure;

    public static String SetStructureDialog_msgStructureDontExist;

    public static String ContentPage_Column;

    public static String TableModelDescriptionPage_generalInformation;

}
