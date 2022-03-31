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
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.SortedSet;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaContainerTest extends PlainJavaAbstractionTestSetup {

    private AProject testProject;

    @Before
    public void setUp() {
        testProject = newAbstractionProject("TestProject");
    }

    @Test
    public void testPlainJavaContainer() {
        assertThat(Abstractions.getWorkspace().getRoot().unwrap(), is(instanceOf(File.class)));
        assertThat(testProject.unwrap(), is(instanceOf(File.class)));
        assertThat(testProject.getFolder("src").unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testGetMembers() {
        AFolder srcFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
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
    public void testGetFolder() {
        AFolder srcFolder = testProject.getFolder("src").getFolder("main").getFolder("java");

        assertThat(srcFolder,
                is(wrapperOf(testProject.getLocation().resolve(Path.of("src", "main", "java")).toFile())));
    }

    @Test
    public void testGetFile() {
        AFile aFile = testProject.getFile(Path.of(".ipsproject"));

        assertThat(aFile, is(wrapperOf(testProject.getLocation().resolve(".ipsproject").toFile())));
    }

    @Test
    public void testFindMember() {
        AFolder srcFolder = testProject.getFolder("src").getFolder("main").getFolder("java");
        AFile aFile = srcFolder.getFile("testFile");
        aFile.create(writeTo("Content"), null);

        AResource found = testProject.findMember("src/main/java/testFile");

        assertThat(found, is(aFile));
    }

    @Test
    public void testFindMember_empty() {
        AResource found = testProject.findMember("");

        assertThat(found, is(testProject));
    }

    @Test
    public void testIsSynchronizedInternal_true() {
        PlainJavaFolder srcFolder = (PlainJavaFolder)testProject.getFolder("src").getFolder("main").getFolder("java");

        assertThat(srcFolder.isSynchronizedInternal(), is(true));
    }

    @Test
    public void testIsSynchronizedInternal_false() {
        PlainJavaFolder srcFolder = (PlainJavaFolder)testProject.getFolder("src").getFolder("main").getFolder("java");
        srcFolder.getLocation().resolve("aNewMember").toFile().mkdir();

        assertThat(srcFolder.isSynchronizedInternal(), is(false));
    }

}
