/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.abstraction.mapping.BuildKindMapping.buildKind;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.AWrapper;
import org.faktorips.devtools.abstraction.Wrappers;

public class EclipseWorkspace extends AWrapper<IWorkspace> implements AWorkspace {

    EclipseWorkspace(IWorkspace workspace) {
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
    public void runAsync(ICoreRunnable action, IProgressMonitor monitor) {
        run(action, monitor);
    }

    @Override
    public void build(ABuildKind buildKind, IProgressMonitor monitor) {
        Wrappers.run(() -> workspace().build(buildKind(buildKind), monitor));
    }
}
