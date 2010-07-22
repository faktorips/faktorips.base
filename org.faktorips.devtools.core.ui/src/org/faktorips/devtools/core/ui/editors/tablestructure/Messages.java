/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.tablestructure.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String RangesSection_title;
    public static String ColumnEditDialog_title;
    public static String ColumnEditDialog_pageTitle;
    public static String ColumnEditDialog_labelName;
    public static String ColumnEditDialog_labelDatatype;
    public static String StructurePage_title;
    public static String UniqueKeysSection_title;
    public static String ForeignKeysSection_title;
    public static String ColumnsSection_title;
    public static String KeyEditDialog_title;
    public static String KeyEditDialog_generalTitle;
    public static String KeyEditDialog_labelReferenceStructure;
    public static String KeyEditDialog_labelReferenceUniqueKey;
    public static String KeyEditDialog_labelKeyItems;
    public static String KeyEditDialog_groupTitle;
    public static String RangeEditDialog_title;
    public static String RangeEditDialog_generalTitle;
    public static String RangeEditDialog_labelType;
    public static String RangeEditDialog_groupTitle;
    public static String RangeEditDialog_labelFrom;
    public static String RangeEditDialog_labelTo;
    public static String RangeEditDialog_groupAvailableColsTitle;
    public static String TableStructureEditor_title;
    public static String RangeEditDialog_RangeEditDialog_parameterName;
    public static String GeneralInfoSection_labelGeneralInfoSection;
    public static String GeneralInfoSection_labelTableType;

}
