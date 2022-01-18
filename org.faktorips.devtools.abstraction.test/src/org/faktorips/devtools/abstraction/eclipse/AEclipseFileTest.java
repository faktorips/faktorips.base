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
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.junit.Before;
import org.junit.Test;

public class AEclipseFileTest extends AEclipseAbstractionTestSetup {

    private AProject testProject;
    private IProject eclipseProject;

    @Before
    public void setUp() {
        testProject = newSimpleIpsProject("TestProject");
        eclipseProject = testProject.unwrap();
    }

    @Test
    public void testAEclipseFile() {
        AFile aFile = testProject.getFile(".ipsproject");

        assertThat(aFile.unwrap(), is(instanceOf(IFile.class)));
    }

    @Test
    public void testCreate() throws Exception {
        AFile newFile = testProject.getFile("newFile");

        assertThat(newFile.exists(), is(false));

        newFile.create(writeTo("TestString"), new NullProgressMonitor());

        assertThat(newFile.exists(), is(true));
        assertThat(readFrom(eclipseProject.getFile("newFile").getContents()), is("TestString"));
    }

    @Test
    public void testSetContents() throws Exception {
        AFile newFile = testProject.getFile("newFile");
        newFile.create(writeTo("TestString"), new NullProgressMonitor());

        newFile.setContents(writeTo("SomeOtherTestString"), false, new NullProgressMonitor());

        assertThat(readFrom(eclipseProject.getFile("newFile").getContents()), is("SomeOtherTestString"));
    }

    @Test
    public void testGetContents() {
        AFile newFile = testProject.getFile("newFile");
        newFile.create(writeTo("TestString"), new NullProgressMonitor());

        String result = readFrom(newFile.getContents());

        assertThat(result, is("TestString"));
    }

    @Test(expected = IpsException.class)
    public void testSetContents_doesNotExist() throws CoreException {
        AFile newFile = testProject.getFile("newFile");

        newFile.setContents(writeTo("SomeOtherTestString"), false, new NullProgressMonitor());
    }

    @Test
    public void testIsReadOnly() throws CoreException {
        AFile newFile = testProject.getFile("newFile");
        newFile.create(writeTo("TestString"), new NullProgressMonitor());
        setReadOnlyResourceAttribute(newFile);

        assertThat(newFile.isReadOnly(), is(true));
    }

    private void setReadOnlyResourceAttribute(AFile newFile) throws CoreException {
        IFile iFile = (IFile)newFile.unwrap();
        ResourceAttributes attrib = iFile.getResourceAttributes();
        attrib.setReadOnly(true);
        iFile.setResourceAttributes(attrib);
    }

    @Test
    public void testIsReadOnly_not() {
        AFile newFile = testProject.getFile("newFile");
        newFile.create(writeTo("TestString"), new NullProgressMonitor());

        assertThat(newFile.isReadOnly(), is(false));
    }

    @Test
    public void testGetExtension() {
        AFile newFile = testProject.getFile("newFile.txt");

        assertThat(newFile.getExtension(), is("txt"));
    }

    @Test
    public void testGetExtension_noExtension() {
        AFile newFile = testProject.getFile("newFile");

        assertThat(newFile.getExtension(), is(""));
    }

    @Test
    public void testGetType() {
        assertThat(testProject.getFile("newFile").getType(), is(AResourceType.FILE));
    }
}
