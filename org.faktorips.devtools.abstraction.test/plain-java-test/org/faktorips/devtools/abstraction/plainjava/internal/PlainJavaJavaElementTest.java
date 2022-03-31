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
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.File;

import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaJavaElementTest extends PlainJavaAbstractionTestSetup {

    private AJavaProject javaProject;
    private AProject aProject;

    @Before
    public void setUp() {
        aProject = newSimpleIpsProject("TestProject");
        javaProject = toJavaProject(aProject);
    }

    @Test
    public void testPlainJavaJavaElement() {
        assertThat(javaProject.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testGetJavaProject() {
        assertThat(javaProject.getJavaProject(), is(equalTo(javaProject)));
    }

    @Test
    public void testGetResource() {
        assertThat(javaProject.getResource(), is(notNullValue()));
    }

    @Test
    public void testGetResource_Packagefragment() {
        AFolder srcFolder = aProject.getFolder("src");
        APackageFragmentRoot pkgFragRoot = javaProject.toPackageFragmentRoot(srcFolder);

        assertThat(pkgFragRoot, is(wrapperOf(aProject.getLocation().resolve("src").toFile())));
    }

    @Test
    public void testExists() {
        assertThat(javaProject.exists(), is(true));
    }

    @Test
    public void testGetPath() {
        assertThat(javaProject.getPath(),
                is(Abstractions.getWorkspace().getRoot().getProject("TestProject").getWorkspaceRelativePath()));
    }
}
