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

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsObjectPage_msgIpsObjectAlreadyExists1;
    public static String IpsObjectPage_msgIpsObjectAlreadyExists2;
    public static String IpsObjectPage_msgIpsObjectAlreadyExists3;
    public static String IpsObjectPage_msgIpsObjectAlreadyExists4;
    public static String IpsObjectPage_msgIpsObjectAlreadyExists5;
    public static String NewIpsObjectWizard_creatingObject;
    public static String NewIpsObjectWizard_creatingObjects;
    public static String NewIpsObjectWizard_error_unableToCreateIpsSrcFile;
    public static String NewIpsObjectWizard_title;
    public static String IpsObjectPage_msgNew;
    public static String IpsObjectPage_labelSrcFolder;
    public static String IpsObjectPage_labelPackage;
    public static String IpsObjectPage_labelName;
    public static String IpsObjectPage_msgRootMissing;
    public static String IpsObjectPage_msgRootNoIPSSrcFolder;
    public static String IpsObjectPage_msgPackageMissing;
    public static String IpsObjectPage_msgRootRequired;
    public static String ResultDisplayer_Errors;
    public static String ResultDisplayer_Informations;
    public static String ResultDisplayer_msgErrors;
    public static String ResultDisplayer_msgInformations;
    public static String ResultDisplayer_msgWarnings;
    public static String ResultDisplayer_reasonText;
    public static String ResultDisplayer_titleResults;
    public static String ResultDisplayer_Warnings;

}
