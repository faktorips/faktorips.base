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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.refactor.messages"; //$NON-NLS-1$

    public static String RenameRefactoringWizard_Attribute;
    public static String RenameRefactoringWizard_Method;
    public static String RenameRefactoringWizard_Type;

    public static String RenameRefactoringWizard_title;
    public static String RenameRefactoringWizard_message;

    public static String RenameRefactoringWizard_labelNewName;

    public static String RenameRefactoringWizard_msgNewNameEmpty;
    public static String RenameRefactoringWizard_msgNewNameEqualsElementName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {

    }

}
