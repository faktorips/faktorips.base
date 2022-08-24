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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Test;

public class EclipseWorkspaceTest extends EclipseAbstractionTestSetup {

    @Test
    public void testGetEclipseWorkspace() {
        AWorkspace workspace = Abstractions.getWorkspace();

        assertThat(workspace, is(notNullValue()));
        assertThat(workspace.unwrap(), is(instanceOf(IWorkspace.class)));
    }

    @Test
    public void testGetRoot() {
        AWorkspaceRoot root = Abstractions.getWorkspace().getRoot();

        assertThat(root.unwrap(), is(instanceOf(IWorkspaceRoot.class)));
    }

    @Test
    public void testRun() {
        String name = "AEclipseWorkspaceTestProject";
        ICoreRunnable runnable = $ -> {
            newSimpleIpsProject(name);
        };
        Abstractions.getWorkspace().run(runnable, new NullProgressMonitor());

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        assertThat(project, is(notNullValue()));
        assertThat(project.exists(), is(true));
    }

    @Test
    public void testBuild() {
        newSimpleIpsProject("someProject");

        Abstractions.getWorkspace().build(ABuildKind.CLEAN, new NullProgressMonitor());
    }
}
