/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    public static String MoveWizard_errorPackageContainsInvalidObjects;

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
