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

package org.faktorips.devtools.core.ui.editors.enumcontent;

import org.eclipse.osgi.util.NLS;

/*
 * 
 * @author Alexander Weickmann
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.editors.enumcontent.messages"; //$NON-NLS-1$

    public static String EnumContentEditor_title;
    public static String EnumContentValuesPage_title;
    public static String EnumContentGeneralInfoSection_title;
    public static String EnumContentGeneralInfoSection_linkEnumType;

    public static String FixEnumTypeDialog_title;
    public static String FixEnumTypeDialog_labelNewEnumType;
    public static String FixEnumTypeDialog_msgEnumTypeMissing;
    public static String FixEnumTypeDialog_msgEnumTypeDoesNotExist;

    public static String EnumContentPage_labelOpenFixEnumTypeDialog;
    public static String EnumContentPage_tooltipOpenFixEnumTypeDialog;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

}
