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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaFileUtilTest {

    private Path tmpDir;

    @Before
    public void setUp() throws IOException {
        tmpDir = Files.createTempDirectory(PlainJavaFileUtilTest.class.getSimpleName());
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

    @Test
    public void testCopy_IOException() throws IOException {
        File file = tmpDir.resolve("foo.bar").toFile();
        file.createNewFile();
        Path destination = Path.of("dest");
        // create a directory at the destination so the file can't be copied there
        tmpDir.resolve(destination).toFile().mkdir();
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.copy(file, destination, monitor);
        });

        assertThat(monitor.getName(), containsString("Copying"));
        assertThat(monitor.getName(), containsString("foo.bar"));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(0));
    }

    @Test
    public void testMove_IOException() throws IOException {
        File file = tmpDir.resolve("foo.bar").toFile();
        file.createNewFile();
        Path destination = Path.of("dest");
        // create a directory at the destination so the file can't be moved there
        tmpDir.resolve(destination).toFile().mkdir();
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.move(file, destination, monitor);
        });

        assertThat(monitor.getName(), containsString("Moving"));
        assertThat(monitor.getName(), containsString("foo.bar"));
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
        File potentialDirectory = tmpDir.resolve("pot").toFile();

        assertThat(PlainJavaFileUtil.directory(potentialDirectory), is(potentialDirectory));
    }

    @Test
    public void testDirectory_File() throws IOException {
        File notADirectory = tmpDir.resolve("foo.bar").toFile();
        notADirectory.createNewFile();

        assertThrows(IllegalArgumentException.class, () -> PlainJavaFileUtil.directory(notADirectory));
    }

    @Test
    public void testWalk_IOException() throws IOException {
        File file = tmpDir.toFile();
        tmpDir.resolve("foo").toFile().createNewFile();
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.walk(file, monitor, "Foo", path -> {
                throw new IOException();
            });
        });

        assertThat(monitor.getName(), containsString("Foo"));
        assertThat(monitor.getTotalWork(), is(2));
        assertThat(monitor.getWork(), is(0));
    }

    @Test
    public void testWithMonitor_IOException() throws IOException {
        File file = tmpDir.toFile();
        tmpDir.resolve("foo").toFile().createNewFile();
        TestPlainJavaMonitor monitor = new TestPlainJavaMonitor();

        assertThrows(IpsException.class, () -> {
            PlainJavaFileUtil.withMonitor(file, monitor, "Foo", path -> {
                throw new IOException();
            });
        });

        assertThat(monitor.getName(), containsString("Foo"));
        assertThat(monitor.getTotalWork(), is(1));
        assertThat(monitor.getWork(), is(0));
    }

}
