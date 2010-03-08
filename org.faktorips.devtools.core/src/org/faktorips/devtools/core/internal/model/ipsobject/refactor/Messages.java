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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.ipsobject.refactor.messages"; //$NON-NLS-1$

    private Messages() {

    }

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String IpsRefactoringProcessor_errorIpsElementDoesNotExist;
    public static String IpsRefactoringProcessor_errorIpsSrcFileOutOfSync;

    public static String IpsRenameProcessor_msgNewNameEmpty;
    public static String IpsRenameProcessor_msgNewNameEqualsElementName;

    public static String IpsMoveProcessor_msgTargetLocationEqualsOriginalLocation;

    public static String RenameAttributeProcessor_processorName;
    public static String RenameAttributeProcessor_msgAttributeNotValid;

    public static String RenameTypeMoveTypeHelper_msgTypeNotValid;
    public static String RenameTypeMoveTypeHelper_msgSourceFileAlreadyExists;

    public static String RenameTypeProcessor_processorName;

    public static String MoveTypeProcessor_processorName;

}
