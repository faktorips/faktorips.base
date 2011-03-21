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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.refactor.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ElementNames_Attribute;
    public static String ElementNames_Method;
    public static String ElementNames_Association;
    public static String ElementNames_Type;
    public static String ElementNames_EnumLiteralNameAttributeValue;

    public static String RenameRefactoringWizard_title;
    public static String MoveRefactoringWizard_title;

    public static String RenameUserInputPage_labelNewName;
    public static String RenameUserInputPage_labelNewPluralName;
    public static String RenameUserInputPage_message;

    public static String MoveUserInputPage_message;
    public static String MoveUserInputPage_labelChooseDestination;
    public static String MoveUserInputPage_msgSelectOnlyPackages;

    public static String IpsRenameAndMoveUserInputPage_labelRefactorRuntimeId;

}
