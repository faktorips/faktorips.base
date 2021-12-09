/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.abstraction.AWorkspaceRoot.PlainJavaWorkspaceRoot;

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
     * Builds all projects in this workspace.
     *
     * @param buildKind the kind of build to perform
     * @param monitor a progress monitor that is notified about the build process. Individual file
     *            processing is reported to the monitor to allow fine-grained progress reporting.
     *            The monitor may be {@code null} when progress does not need to be reported.
     */
    void build(ABuildKind buildKind, @CheckForNull IProgressMonitor monitor);

    public static class AEclipseWorkspace extends AWrapper<IWorkspace> implements AWorkspace {

        AEclipseWorkspace(IWorkspace workspace) {
            super(workspace);
        }

        IWorkspace workspace() {
            return unwrap();
        }

        @Override
        public AWorkspaceRoot getRoot() {
            return wrap(workspace().getRoot()).as(AWorkspaceRoot.class);
        }

        @Override
        public void run(ICoreRunnable action, IProgressMonitor monitor) {
            Wrappers.run(() -> workspace().run(action, monitor));
        }

        @Override
        public void build(ABuildKind buildKind, IProgressMonitor monitor) {
            Wrappers.run(() -> workspace().build(ABuildKind.forEclipse(buildKind), monitor));
        }

    }

    public static class PlainJavaWorkspace extends AWrapper<File> implements AWorkspace {

        private final AtomicLong markerId = new AtomicLong();
        private final PlainJavaWorkspaceRoot root;

        public PlainJavaWorkspace(File workspaceDirectory) {
            super(PlainJavaUtil.directory(workspaceDirectory));
            root = new PlainJavaWorkspaceRoot(this);
        }

        File workspace() {
            return unwrap();
        }

        @Override
        public PlainJavaWorkspaceRoot getRoot() {
            return root;
        }

        @Override
        public void run(ICoreRunnable action, IProgressMonitor monitor) {
            Wrappers.run(() -> action.run(monitor));
        }

        public long getNextMarkerId() {
            return markerId.getAndIncrement();
        }

        @Override
        public void build(ABuildKind buildKind, IProgressMonitor monitor) {
            getRoot().getProjects().forEach(p -> p.build(buildKind, monitor));
        }

    }
}
