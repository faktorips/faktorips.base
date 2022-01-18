/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class AEclipseProjectTest extends AEclipseAbstractionTestSetup {

    private static final String TEST_PROJECT1 = "TestProject1";
    private static final String NON_FIPS_PROJECT = "NonFipsProject";

    private AProject testProject1;
    private AProject refProject2;
    private AProject refProject1;
    private IProject eclipseProject1;

    @Before
    public void setUp() {
        refProject1 = newAbstractionProject("RefProject1");
        refProject2 = newAbstractionProject("RefProject2");
        testProject1 = newSimpleIpsProjectWithDependencies(TEST_PROJECT1, refProject1, refProject2);
        eclipseProject1 = testProject1.unwrap();
        newAbstractionProject("NON_FIPS_PROJECT");
    }

    @Test
    public void testAEclipseProject() {
        assertThat(testProject1.unwrap(), is(instanceOf(IProject.class)));
    }

    @Test
    public void testGetFile_aFile() {
        AFile aFile = testProject1.getFile(".ipsproject");

        assertThat(aFile, is(wrapperOf(eclipseProject1.getFile(".ipsproject"))));
        assertThat(((IFile)aFile.unwrap()).exists(), is(true));
    }

    @Test
    public void testGetFile_aFolder() {
        AFile aFile = testProject1.getFile("productdef");

        assertThat(aFile, is(notNullValue()));
        assertThat(((IFile)aFile.unwrap()).exists(), is(false));
    }

    @Test
    public void testGetFolder_aFolder() {
        AFolder aFolder = testProject1.getFolder("productdef");

        assertThat(aFolder, is(wrapperOf(eclipseProject1.getFolder("productdef"))));
        assertThat(((IFolder)aFolder.unwrap()).exists(), is(true));
    }

    @Test
    public void testGetFolder_aFile() {
        AFolder aFolder = testProject1.getFolder(".ipsproject");

        assertThat(aFolder, is(notNullValue()));
        assertThat(((IFolder)aFolder.unwrap()).exists(), is(false));
    }

    @Test
    public void testIsIpsProject() {
        assertThat(Abstractions.getWorkspace().getRoot().getProject(TEST_PROJECT1).isIpsProject(), is(true));
    }

    @Test
    public void testIsIpsProject_not() {
        assertThat(Abstractions.getWorkspace().getRoot().getProject(NON_FIPS_PROJECT).isIpsProject(), is(false));
    }

    @Test
    public void testDelete() {
        AWorkspaceRoot root = Abstractions.getWorkspace().getRoot();

        assertThat(root.getProjects().size(), is(4));

        testProject1.delete(new NullProgressMonitor());

        IWorkspaceRoot workspaceRoot = (IWorkspaceRoot)root.unwrap();
        assertThat(workspaceRoot.getProjects().length, is(3));
        assertThat(workspaceRoot.getProject(TEST_PROJECT1).exists(), is(false));
    }

    @Test
    public void testGetReferencedProjects() {
        assertThat(testProject1.getReferencedProjects(), is(hasItems(refProject1, refProject2)));
    }

    @Test
    public void testGetDefaultCharset() {
        assertThat(testProject1.getDefaultCharset(), is(Charset.defaultCharset()));
    }

    @Test
    public void testGetType() {
        assertThat(testProject1.getType(), is(AResourceType.PROJECT));
    }
}
