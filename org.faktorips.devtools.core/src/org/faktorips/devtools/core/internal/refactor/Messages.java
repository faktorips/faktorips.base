/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.refactor.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsRefactoringProcessor_errorIpsElementDoesNotExist;
    public static String IpsRefactoringProcessor_errorIpsSrcFileOutOfSync;

    public static String IpsRenameProcessor_msgNewNameEmpty;
    public static String IpsRenameProcessor_msgNewNameEqualsOriginalName;

    public static String IpsPullUpProcessor_msgTargetNotSpecified;
    public static String IpsPullUpProcessor_msgTargetEqualsCurrentContainer;

    public static String IpsMoveProcessor_msgTargetLocationEqualsOriginalLocation;

    public static String IpsCompositeRefactoring_taskCheckInitialConditions;
    public static String IpsCompositeRefactoring_subTaskCheckInitialConditionsForElement;
    public static String IpsCompositeRefactoring_taskCheckFinalConditions;
    public static String IpsCompositeRefactoring_subTaskCheckFinalConditionsForElement;
    public static String IpsCompositeRefactoring_taskProcessElements;
    public static String IpsCompositeRefactoring_subTaskProcessElement;
    public static String IpsCompositeRefactoring_childRefactoringNotExecuted;

    public static String IpsCompositeMoveRefactoring_name;
    public static String IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentNotSet;
    public static String IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentEqualsOriginalIpsPackageFragment;

    public static String MoveOperation_errorMessageSourceNotExists;
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
    public static String MoveOperation_errorIpsObjectMissing;

}
