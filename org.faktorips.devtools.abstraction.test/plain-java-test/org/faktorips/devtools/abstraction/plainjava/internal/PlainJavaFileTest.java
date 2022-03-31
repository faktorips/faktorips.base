/*******************************************************************************
 * 
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org> This source code is available under
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
        testProject = newSimpleIpsProject("TestProject");
    }

    @Test
    public void testPlainJavaFile() {
        AFile testFile = testProject.getFile(".ipsproject");

        assertThat(testFile.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testCreate() throws Exception {
        AFile newFile = testProject.getFile("newFile");

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(false));

        newFile.create(writeTo("TestString"), null);

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(true));
        String fromFile = FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset());
        assertThat(fromFile, is("TestString"));
    }

    @Test
    public void testCreate_withMonitor() throws Exception {
        AFile newFile = testProject.getFile("newFile");

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(false));

        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();
        assertThat(monitor.isDone(), is(false));

        newFile.create(writeTo("TestString"), monitor);

        assertThat(testProject.getLocation().resolve("newFile").toFile().exists(), is(true));
        assertThat(FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset()),
                is("TestString"));
        assertThat(monitor.isDone(), is(true));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getName(), is("Creating " + newFile.getLocation()));
    }

    @Test
    public void testSetContents() throws Exception {
        AFile newFile = testProject.getFile("newFile");
        newFile.create(writeTo("TestString"), null);

        newFile.setContents(writeTo("SomeOtherTestString"), false, new NullProgressMonitor());

        String fromFile = FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset());
        assertThat(fromFile, is("SomeOtherTestString"));
    }

    @Test
    public void testSetContents_doesNotExist() throws Exception {
        AFile newFile = testProject.getFile("newFile");

        // Eclipse would throw an CoreException
        newFile.setContents(writeTo("SomeOtherTestString"), false, new NullProgressMonitor());

        String fromFile = FileUtils.readFileToString(newFile.getLocation().toFile(), Charset.defaultCharset());
        assertThat(fromFile, is("SomeOtherTestString"));
    }

    @Test
    public void testGetContents() {
        AFile newFile = testProject.getFile("newFile");
        newFile.create(writeTo("TestString"), new NullProgressMonitor());

        String result = readFrom(newFile.getContents());

        assertThat(result, is("TestString"));
    }

    @Test
    public void testIsReadOnly() {
        AFile testFile = testProject.getFile("SomeSubFile");

        assertThat(testFile.isReadOnly(), is(false));
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
    public void testGetResourceType() {
        assertThat(testProject.getFile("newFile").getType(), is(AResourceType.FILE));
    }

    @Test
    public void testInternalCreate() {
        PlainJavaFile aFile = (PlainJavaFile)testProject.getFile("newFile");

        aFile.create();

        File file = aFile.getLocation().toFile();
        assertThat(file.exists(), is(true));
        assertThat(file.length(), is(0L));
    }
}
