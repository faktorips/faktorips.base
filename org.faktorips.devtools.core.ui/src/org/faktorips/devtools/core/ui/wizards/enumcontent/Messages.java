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

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.enumcontent.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumContentPage_msgEnumContentAlreadyExists;
    public static String EnumContentPage_msgEnumContentExistsForNameExistsAlready;
    public static String EnumContentPage_msgEnumContentNameOfEnumTypeMissing;
    public static String EnumContentPage_msgEnumTypeMissing;
    public static String EnumContentPage_msgEnumTypeNotExisting;
    public static String Page_Title;
    public static String Fields_EnumType;

    public static String CreateMissingEnumContentsWizard_title;
    public static String CreateMissingEnumContentsWizard_labelOperation;
    public static String SelectEnumContentsPage_title;
    public static String SelectEnumContentsPage_prompt;
    public static String SelectEnumContentsPage_labelTargetSourceFolder;
    public static String SelectEnumContentsPage_buttonSelectAll;
    public static String SelectEnumContentsPage_buttonDeselectAll;
    public static String SelectEnumContentsPage_msgTargetSourceFolderNotSpecified;
    public static String SelectEnumContentsPage_msgTargetSourceFolderDoesNotExist;

}
