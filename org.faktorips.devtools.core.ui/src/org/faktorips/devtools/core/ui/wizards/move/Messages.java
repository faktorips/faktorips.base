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

package org.faktorips.devtools.core.ui.wizards.move;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.move.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String MovePage_msgErrorPackageAlreadyExists;
    public static String MovePage_msgErrorSelectedTargetIsIncludedInSource;
    public static String MovePage_title;
    public static String MovePage_description;
    public static String MovePage_targetLabel;

    public static String MoveWizard_titleMove;
    public static String MoveWizard_titleRename;
    public static String MoveWizard_warnInvalidOperation;
    public static String MoveWizard_errorUnsupported;
    public static String MoveWizard_errorToManySelected;
    public static String MoveWizard_error;

    public static String ErrorPage_error;

    public static String RenamePage_labelRuntimeId;
    public static String RenamePage_msgRuntimeCollision;
    public static String RenamePage_rename;
    public static String RenamePage_msgChooseNewName;
    public static String RenamePage_newName;
    public static String RenamePage_errorFileExists;
    public static String RenamePage_errorFolderExists;
    public static String RenamePage_labelVersionId;
    public static String RenamePage_labelConstNamePart;

}
