/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.time.Duration;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.testsupport.Wait;
import org.junit.Ignore;
import org.junit.Test;

public class PlainJavaWorkspaceTest extends PlainJavaAbstractionTestSetup {

    @Test
    public void testGetPlainJavaWorkspace() {
        AWorkspace workspace = Abstractions.getWorkspace();

        assertThat(workspace, is(notNullValue()));
        assertThat(workspace.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testGetRoot() {
        AWorkspaceRoot root = Abstractions.getWorkspace().getRoot();

        assertThat(root.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testRun() {
        String name = "AEclipseWorkspaceTestProject"; //$NON-NLS-1$
        ICoreRunnable runnable = $ -> {
            newSimpleIpsProject(name);
        };

        Abstractions.getWorkspace().run(runnable, new NullProgressMonitor());

        Wait.atMost(Duration.ofSeconds(1)).until(() -> {
            File project = Abstractions.getWorkspace().getRoot().getLocation().resolve(name).toFile();
            return project != null && project.exists();
        }, "Project " + name + " was not created"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    @Ignore
    public void testBuild() {
        Abstractions.getWorkspace().getRoot().getProject("TestProject"); //$NON-NLS-1$

        Abstractions.getWorkspace().build(ABuildKind.CLEAN, new NullProgressMonitor());

        // TODO: assert später wenn PlainJavaProject#build implementiert ist
    }
}
