/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
