/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.application.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsActionBarAdvisor_file;
    public static String IpsActionBarAdvisor_new;
    public static String IpsActionBarAdvisor_edit;
    public static String IpsActionBarAdvisor_navigate;
    public static String IpsActionBarAdvisor_goto;
    public static String IpsActionBarAdvisor_showIn;
    public static String IpsActionBarAdvisor_project;
    public static String IpsActionBarAdvisor_Window;
    public static String IpsActionBarAdvisor_shortcuts;
    public static String IpsActionBarAdvisor_help;

    public static String IpsApplication_cannotCreateWorkspace_msg;
    public static String IpsApplication_cannotCreateWorkspace_title;
    public static String IpsApplication_cannotLockWorkspace_msg;
    public static String IpsApplication_cannotLockWorkspace_title;
    public static String IpsApplication_workspaceNotSet_msg;
    public static String IpsApplication_workspaceNotSet_title;
    public static String IpsWorkbenchAdvisor_title;

    public static String ProblemsSavingWorkspace;

}
