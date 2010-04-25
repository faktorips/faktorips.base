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

package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.refactor.messages"; //$NON-NLS-1$

    private Messages() {
        // Messages bundle cannot be instantiated.
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

    public static String MoveOperation_errorMessageSourceNotExists;
    public static String MoveOperation_msgErrorProductCmptIsMissing;
    public static String MoveOperation_msgErrorTheTargetIsIncludedInTheSource;
    public static String MoveOperation_titleAborted;
    public static String MoveOperation_msgAborted;
    public static String MoveOperation_msgFileExists;
    public static String MoveOperation_msgPackageExists;
    public static String MoveOperation_msgSourceMissing;
    public static String MoveOperation_msgSourceModified;
    public static String MoveOperation_msgPackageMissing;
    public static String MoveOperation_msgUnsupportedType;
    public static String MoveOperation_msgUnsupportedObject;
    public static String MoveOperation_tableContentIsMissing;
    public static String MoveOperation_testCaseIsMissing;
    public static String MoveOperation_typeIsMissing;

}
