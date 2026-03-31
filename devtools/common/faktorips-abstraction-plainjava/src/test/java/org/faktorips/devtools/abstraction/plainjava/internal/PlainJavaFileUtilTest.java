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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaFileUtilTest extends PlainJavaAbstractionTestSetup {

    private Path tmpDir;

    @Before
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory(PlainJavaFileUtilTest.class.getSimpleName());
    }

    @Override
    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tmpDir.toFile());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        super.tearDown();
    }

    @Test
    public void testCopy_IOException() throws IOException {
        File file = tmpDir.resolve("foo.bar").toFile(); //$NON-NLS-1$
        file.createNewFile();
        Path destination = Path.of("dest"); //$NON-NLS-1$
        // create a directory at the destination so the file can't be copied there
        tmpDir.resolve(destination).toFile().mkdir();
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.copy(file, destination, monitor);
        });

        assertThat(monitor.getName(), containsString("Copying")); //$NON-NLS-1$
        assertThat(monitor.getName(), containsString("foo.bar")); //$NON-NLS-1$
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(0));
    }

    @Test
    public void testMove_IOException() throws IOException {
        File file = tmpDir.resolve("foo.bar").toFile(); //$NON-NLS-1$
        file.createNewFile();
        Path destination = Path.of("dest"); //$NON-NLS-1$
        // create a directory at the destination so the file can't be moved there
        tmpDir.resolve(destination).toFile().mkdir();
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.move(file, destination, monitor);
        });

        assertThat(monitor.getName(), containsString("Moving")); //$NON-NLS-1$
        assertThat(monitor.getName(), containsString("foo.bar")); //$NON-NLS-1$
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(0));
        assertThat(file.exists(), is(true));
    }

    @Test
    public void testDirectory_Real() {
        File realDirectory = tmpDir.toFile();

        assertThat(PlainJavaFileUtil.directory(realDirectory), is(realDirectory));
    }

    @Test
    public void testDirectory_Potential() {
        File potentialDirectory = tmpDir.resolve("pot").toFile(); //$NON-NLS-1$

        assertThat(PlainJavaFileUtil.directory(potentialDirectory), is(potentialDirectory));
    }

    @Test
    public void testDirectory_File() throws IOException {
        File notADirectory = tmpDir.resolve("foo.bar").toFile(); //$NON-NLS-1$
        notADirectory.createNewFile();

        assertThrows(IllegalArgumentException.class, () -> PlainJavaFileUtil.directory(notADirectory));
    }

    @Test
    public void testWalk_IOException() throws IOException {
        File file = tmpDir.toFile();
        tmpDir.resolve("foo").toFile().createNewFile(); //$NON-NLS-1$
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.walk(file, monitor, "Foo", path -> { //$NON-NLS-1$
                throw new IOException();
            });
        });

        assertThat(monitor.getName(), containsString("Foo")); //$NON-NLS-1$
        assertThat(monitor.getTotalWork(), is(2));
        assertThat(monitor.getWork(), is(0));
    }

    @Test
    public void testWithMonitor_IOException() throws IOException {
        File file = tmpDir.toFile();
        tmpDir.resolve("foo").toFile().createNewFile(); //$NON-NLS-1$
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.withMonitor(file, monitor, "Foo", path -> { //$NON-NLS-1$
                throw new IOException();
            });
        });

        assertThat(monitor.getName(), containsString("Foo")); //$NON-NLS-1$
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(0));
    }

    @Test
    public void testInternalResource() {
        File workspacePath = Abstractions.getWorkspace().getRoot().unwrap();
        AProject project1 = Wrappers.wrap(workspacePath).as(AProject.class);
        AProject testProject1 = setupAbstractionProject(project1);
        toIpsProject(testProject1);
        newSimpleIpsProject("TestProject1");
        File project2Dir = new File(workspacePath.getParentFile(), workspacePath.getName() + "TestProject2");
        project2Dir.mkdir();
        AProject project = Wrappers.wrap(project2Dir).as(AProject.class);
        AProject testProject2 = setupAbstractionProject(project);
        toIpsProject(testProject2);

        AFolder folder = testProject2.getFolder("foo");
        folder.create(null);
        AFile file = folder.getFile("bar");
        file.create(new ByteArrayInputStream("bar".getBytes()), null);

        File internalResource = PlainJavaFileUtil.internalResource(file.unwrap(), (PlainJavaProject)testProject2);

        assertThat(internalResource, is(notNullValue()));

    }

}
