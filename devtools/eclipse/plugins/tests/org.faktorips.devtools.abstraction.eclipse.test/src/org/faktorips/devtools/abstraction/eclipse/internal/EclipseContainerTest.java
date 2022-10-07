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
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;
import java.util.SortedSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class EclipseContainerTest extends EclipseAbstractionTestSetup {

    private AProject testProject;
    private IProject eclipseProject;

    @Before
    public void setUp() {
        testProject = newAbstractionProject("TestProject");
        eclipseProject = testProject.unwrap();
    }

    @Test
    public void testAEclipseContainer() {
        assertThat(Abstractions.getWorkspace().getRoot().unwrap(), is(instanceOf(IContainer.class)));
        assertThat(testProject.unwrap(), is(instanceOf(IContainer.class)));
        assertThat(testProject.getFolder("src").unwrap(), is(instanceOf(IContainer.class)));
    }

    @Test
    public void testGetMembers() {
        AFolder srcFolder = testProject.getFolder("src");
        AFile aFile = srcFolder.getFile("testFile");
        aFile.create(writeTo("Content"), null);
        AFolder aFolder = srcFolder.getFolder("testFolder");
        aFolder.create(null);

        SortedSet<? extends AResource> members = srcFolder.getMembers();

        assertThat(members.isEmpty(), is(false));
        assertThat(members.first(), is(aFile));
        assertThat(members.last(), is(aFolder));
    }

    @Test
    public void testGetFolder_Path() {
        AFolder srcFolder = testProject.getFolder(Path.of("src", "test"));

        assertThat(srcFolder, is(wrapperOf(eclipseProject.getFolder("src").getFolder("test"))));
    }

    @Test
    public void testGetFolder_String() {
        AFolder srcFolder = testProject.getFolder("src");

        assertThat(srcFolder, is(wrapperOf(eclipseProject.getFolder("src"))));
    }

    @Test
    public void testGetFile() {
        AFile aFile = testProject.getFile(Path.of(".ipsproject"));

        assertThat(aFile, is(wrapperOf(eclipseProject.getFile(".ipsproject"))));
    }

    @Test
    public void testFindMember() {
        AFolder srcFolder = testProject.getFolder("src");
        AFile aFile = srcFolder.getFile("testFile");
        aFile.create(writeTo("Content"), null);

        AResource found = testProject.findMember("/src/testFile");

        assertThat(found, is(aFile));
    }

    @Test
    public void testFindMember_empty() {
        AResource found = testProject.findMember("");

        assertThat(found, is(testProject));
    }
}
