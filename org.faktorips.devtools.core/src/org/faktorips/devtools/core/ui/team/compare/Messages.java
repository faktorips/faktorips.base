/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.team.compare.messages"; //$NON-NLS-1$

    private Messages() {
    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String ProductCmptCompareItemCreator_StructureViewer_title;
    public static String ProductCmptCompareViewer_CompareViewer_title;
    public static String ProductCmptCompareItem_Generation;
    public static String ProductCmptCompareItem_ValidFrom;
    public static String ProductCmptCompareItem_Relations;
    public static String ProductCmptCompareItem_ProductComponent;
    public static String ProductCmptCompareItem_Attributes;
    public static String ProductCmptCompareItem_PolicyComponentType;
    public static String ProductCmptCompareItem_RuntimeID;
    public static String ProductCmptCompareItem_SourceFile;
    public static String ProductCmptCompareItem_ValueSet_Value;
    public static String ProductCmptCompareItem_ValueSet;
    public static String ProductCmptCompareItem_AllValues;
    public static String ProductCmptCompareItem_Attribute;
    public static String ProductCmptCompareItem_Relation;
}
