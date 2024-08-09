/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A workspace contains all {@link AProject projects} and allows global operations.
 */
public interface AWorkspace extends AAbstraction {

    /**
     * Returns the resource representing this workspace.
     */
    AWorkspaceRoot getRoot();

    /**
     * Runs the given action, reporting progress to the given monitor
     *
     * @param action an action to be run in the context of this workspace
     * @param monitor a progress monitor that is notified about the action's process. Individual
     *            processing steps may be reported to the monitor to allow fine-grained progress
     *            reporting. The monitor may be {@code null} when progress does not need to be
     *            reported.
     */
    void run(ICoreRunnable action, @CheckForNull IProgressMonitor monitor);

    /**
     * Runs the given action in the background, reporting progress to the given monitor
     *
     * @param action an action to be run in the context of this workspace
     * @param monitor a progress monitor that is notified about the action's process. Individual
     *            processing steps may be reported to the monitor to allow fine-grained progress
     *            reporting. The monitor may be {@code null} when progress does not need to be
     *            reported.
     */
    void runAsync(ICoreRunnable action, @CheckForNull IProgressMonitor monitor);

    /**
     * Builds all projects in this workspace.
     *
     * @param buildKind the kind of build to perform
     * @param monitor a progress monitor that is notified about the build process. Individual file
     *            processing is reported to the monitor to allow fine-grained progress reporting.
     *            The monitor may be {@code null} when progress does not need to be reported.
     */
    void build(ABuildKind buildKind, @CheckForNull IProgressMonitor monitor);

    /**
     * @return {@code true} if the current OS is Windows
     */
    boolean isWindows();

    /**
     * @return {@code true} if the current OS is Linux
     */
    boolean isLinux();

    /**
     * @return {@code true} if the current OS is MacOSX
     */
    boolean isMac();
}
