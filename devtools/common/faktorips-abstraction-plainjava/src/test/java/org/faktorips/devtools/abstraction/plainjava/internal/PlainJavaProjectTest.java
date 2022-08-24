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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaProjectTest extends PlainJavaAbstractionTestSetup {

    private AProject testProject1;
    private AProject testProject2;

    @Before
    public void setUp() {
        testProject1 = newSimpleIpsProject("TestProject1"); //$NON-NLS-1$
        testProject2 = newAbstractionProject("TestProject2"); //$NON-NLS-1$
    }

    @Test
    public void testPlainJavaProject() {
        assertThat(testProject1.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testGetFile_aFile() {
        AFile aFile = testProject1.getFile(".ipsproject"); //$NON-NLS-1$

        assertThat(aFile, is(wrapperOf(testProject1.getLocation().resolve(".ipsproject").toFile()))); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFile_aFolder() {
        testProject1.getFile("productdef"); //$NON-NLS-1$
    }

    @Test
    public void testGetFolder_aFolder() {
        AFolder aFolder = testProject1.getFolder("productdef"); //$NON-NLS-1$

        assertThat(aFolder, is(wrapperOf(testProject1.getLocation().resolve("productdef").toFile()))); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFolder_aFile() {
        testProject1.getFolder(".ipsproject"); //$NON-NLS-1$
    }

    @Test
    public void testIsIpsProject() {
        assertThat(testProject1.isIpsProject(), is(true));
    }

    @Test
    public void testIsIpsProject_not() {
        assertThat(testProject2.isIpsProject(), is(false));
    }

    @Test
    public void testDelete() {
        AWorkspaceRoot root = Abstractions.getWorkspace().getRoot();

        assertThat(root.getProjects().size(), is(2));

        testProject1.delete(null);

        assertThat(root.getProjects().size(), is(1));
        assertThat(root.getLocation().resolve("TestProject1").toFile().exists(), is(false)); //$NON-NLS-1$
    }

    @Test
    public void testCreate() {
        PlainJavaProject project = (PlainJavaProject)Abstractions.getWorkspace().getRoot().getProject("TestProject3"); //$NON-NLS-1$

        assertThat(project.exists(), is(false));

        project.create();

        assertThat(project.exists(), is(true));
    }

    @Test
    public void testGetType() {
        assertThat(testProject1.getType(), is(AResourceType.PROJECT));
    }
}
