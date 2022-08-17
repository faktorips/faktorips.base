/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.productrelease;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.productrelease.messages"; //$NON-NLS-1$
    public static String CvsTeamOperations_exception_remoteChanges;
    public static String CvsTeamOperations_status_notSynchron;
    public static String CvsTeamOperations_status_notVersionized;
    public static String ProductReleaseProcessor_status_build_success;
    public static String ProductReleaseProcessor_status_commit_success;
    public static String ProductReleaseProcessor_error_custom_validation_failed;
    public static String ProductReleaseProcessor_status_new_version_set;
    public static String ProductReleaseProcessor_status_start;
    public static String ProductReleaseProcessor_status_synchon;
    public static String ProductReleaseProcessor_status_tag_success;
    public static String ReleaseAndDeploymentOperation_commit_comment;
    public static String ReleaseAndDeploymentOperation_exception_errors;
    public static String ReleaseAndDeploymentOperation_exception_fipsErrors;
    public static String ReleaseAndDeploymentOperation_exception_noDeploymentExtension;
    public static String ReleaseAndDeploymentOperation_exception_noLongerSynchron;
    public static String ReleaseAndDeploymentOperation_exception_notSynchron;
    public static String ReleaseAndDeploymentOperation_exception_refreshFilesystem;
    public static String ReleaseAndDeploymentOperation_taskName_release;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do not instantiate
    }
}
