/*******************************************************************************
 * 
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org This source code is available under
 * the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaFileTest extends PlainJavaAbstractionTestSetup {

    private AProject testProject;

    @Before
    public void setUp() {
        testProject = newSimpleIpsProject("TestProject"); //$NON-NLS-1$
    }

    @Test
    public void testPlainJavaFile() {
        AFile testFile = testProject.getFile(".ipsproject"); //$NON-NLS-1$

        assertThat(testFile.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testCreate() throws Exception {
        AFile newFile = testProject.getFile("newFile"); //$NON-NLS-1$

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(false)); //$NON-NLS-1$

        newFile.create(writeTo("TestString"), null); //$NON-NLS-1$

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(true)); //$NON-NLS-1$
        String fromFile = FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset());
        assertThat(fromFile, is("TestString")); //$NON-NLS-1$
    }

    @Test
    public void testCreate_withMonitor() throws Exception {
        AFile newFile = testProject.getFile("newFile"); //$NON-NLS-1$

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(false)); //$NON-NLS-1$

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        newFile.create(writeTo("TestString"), monitor); //$NON-NLS-1$

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(true)); //$NON-NLS-1$
        assertThat(FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset()),
                is("TestString")); //$NON-NLS-1$
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getName(), is("Creating " + newFile.getLocation())); //$NON-NLS-1$
    }

    @Test
    public void testSetContents() throws Exception {
        AFile newFile = testProject.getFile("newFile"); //$NON-NLS-1$
        newFile.create(writeTo("TestString"), null); //$NON-NLS-1$

        newFile.setContents(writeTo("SomeOtherTestString"), false, new NullProgressMonitor()); //$NON-NLS-1$

        String fromFile = FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset());
        assertThat(fromFile, is("SomeOtherTestString")); //$NON-NLS-1$
    }

    @Test
    public void testSetContents_doesNotExist() throws Exception {
        AFile newFile = testProject.getFile("newFile"); //$NON-NLS-1$

        // Eclipse would throw an CoreException
        newFile.setContents(writeTo("SomeOtherTestString"), false, new NullProgressMonitor()); //$NON-NLS-1$

        String fromFile = FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset());
        assertThat(fromFile, is("SomeOtherTestString")); //$NON-NLS-1$
    }

    @Test
    public void testGetContents() {
        AFile newFile = testProject.getFile("newFile"); //$NON-NLS-1$
        newFile.create(writeTo("TestString"), new NullProgressMonitor()); //$NON-NLS-1$

        String result = readFrom(newFile.getContents());

        assertThat(result, is("TestString")); //$NON-NLS-1$
    }

    @Test
    public void testIsReadOnly() {
        AFile testFile = testProject.getFile("SomeSubFile"); //$NON-NLS-1$

        assertThat(testFile.isReadOnly(), is(false));
    }

    @Test
    public void testGetExtension() {
        AFile newFile = testProject.getFile("newFile.txt"); //$NON-NLS-1$

        assertThat(newFile.getExtension(), is("txt")); //$NON-NLS-1$
    }

    @Test
    public void testGetExtension_noExtension() {
        AFile newFile = testProject.getFile("newFile"); //$NON-NLS-1$

        assertThat(newFile.getExtension(), is("")); //$NON-NLS-1$
    }

    @Test
    public void testGetResourceType() {
        assertThat(testProject.getFile("newFile").getType(), is(AResourceType.FILE)); //$NON-NLS-1$
    }

    @Test
    public void testInternalCreate() {
        PlainJavaFile aFile = (PlainJavaFile)testProject.getFile("newFile"); //$NON-NLS-1$

        aFile.create();

        File file = aFile.getLocation().toFile();
        assertThat(file.exists(), is(true));
        assertThat(file.length(), is(0L));
    }
}
