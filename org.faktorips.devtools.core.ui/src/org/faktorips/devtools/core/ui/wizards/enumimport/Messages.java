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

package org.faktorips.devtools.core.ui.wizards.enumimport;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.enumimport.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumImportWizard_operationName;
    public static String EnumImportWizard_title;
    public static String SelectEnumPage_locationLabel;
    public static String SelectEnumPage_msgMissingContent;
    public static String SelectEnumPage_msgEnumEmpty;
    public static String SelectEnumPage_msgEnumNotValid;
    public static String SelectEnumPage_msgAbstractEnumType;
    public static String SelectEnumPage_msgEnumTypeNotContainingValues;
    public static String SelectEnumPage_msgEnumTypeNotValid;
    public static String SelectEnumPage_targetTypeLabel;
    public static String SelectEnumPage_title;
    public static String SelectFileAndImportMethodPage_labelImportExisting;
    public static String SelectFileAndImportMethodPage_labelImportNew;

}
