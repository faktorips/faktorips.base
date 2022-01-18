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
import static org.hamcrest.core.IsSame.sameInstance;

import java.io.File;
import java.nio.file.Path;

import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PlainJavaJavaProjectTest extends PlainJavaAbstractionTestSetup {

    private AJavaProject javaProject;

    @Before
    public void setUp() {
        javaProject = toJavaProject(newSimpleIpsProject("TestProject"));
    }

    @Test
    public void testPlainJavaJavaProject() {
        assertThat(javaProject.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testExists() {
        assertThat(javaProject.exists(), is(true));
    }

    @Test
    public void testExists_not() {
        AProject project = Abstractions.getWorkspace().getRoot().getProject("NotSavedProject");

        assertThat(AJavaProject.from(project).exists(), is(false));
    }

    @Test
    public void testGetOutputLocation() {
        assertThat(javaProject.getOutputLocation(), is(Path.of("TestProject", "target")));
    }

    @Test
    public void testGetProject() {
        assertThat(javaProject.getProject(), is(notNullValue()));
        assertThat(javaProject.getProject(), is(sameInstance(newSimpleIpsProject("TestProject"))));
    }

    @Test
    public void testIsJavaFolder() {
        AFolder srcFolder = javaProject.getProject().getFolder("src");

        assertThat(javaProject.isJavaFolder(srcFolder), is(true));
    }

    @Test
    @Ignore
    // without maven all internal folders are java folders
    public void testIsJavaFolder_isNot() {
        AFolder nonJavaFolder = javaProject.getProject().getFolder("nonJavaResource");
        nonJavaFolder.create(null);

        assertThat(javaProject.isJavaFolder(nonJavaFolder), is(false));
    }

    @Test
    public void testGetPackageFragmentRoot() {
        AFolder sourceFolder = Abstractions.getWorkspace().getRoot().getFolder(javaProject.getOutputLocation());
        APackageFragmentRoot pfr = javaProject.toPackageFragmentRoot(sourceFolder);

        assertThat(pfr, is(wrapperOf(sourceFolder.getLocation().toFile())));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetPackageFragmentRoot_notDir() {
        AFolder sourceFolder = Abstractions.getWorkspace().getRoot().getFolder(javaProject.getOutputLocation());

        javaProject.toPackageFragmentRoot(sourceFolder.getFile("File.java"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPackageFragmentRoot_notInternalDir() {
        javaProject.toPackageFragmentRoot(new PlainJavaFolder(new File(System.getProperty("java.io.tmpdir"))));
    }
}
