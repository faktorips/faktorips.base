/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWrapper;
import org.faktorips.devtools.abstraction.Wrappers;

public class PlainJavaWorkspace extends AWrapper<File> implements AWorkspace {

    private final AtomicLong markerId = new AtomicLong();
    private final PlainJavaWorkspaceRoot root;

    public PlainJavaWorkspace(File workspaceDirectory) {
        super(PlainJavaFileUtil.directory(workspaceDirectory));
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
        PlainJavaImplementation.getResourceChanges().hold();
        Wrappers.run(() -> action.run(monitor));
        PlainJavaImplementation.getResourceChanges().resume();
    }

    @Override
    public void runAsync(ICoreRunnable action, IProgressMonitor monitor) {
        new Thread(() -> Wrappers.run(() -> action.run(monitor))).start();
    }

    public long getNextMarkerId() {
        return markerId.getAndIncrement();
    }

    @Override
    public void build(ABuildKind buildKind, IProgressMonitor monitor) {
        getRoot().getProjects().forEach(p -> p.build(buildKind, monitor));
    }

    public String getName(PlainJavaProject project) {
        return project.file().getName();
    }

}
