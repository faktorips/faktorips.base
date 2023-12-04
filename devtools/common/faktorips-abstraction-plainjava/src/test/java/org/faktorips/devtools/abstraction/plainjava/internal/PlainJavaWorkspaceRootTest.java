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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWorkspaceRoot;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.junit.Before;
import org.junit.Test;

public class PlainJavaWorkspaceRootTest extends PlainJavaAbstractionTestSetup {

    private static final String TEST_PROJECT1 = "TestProject1"; //$NON-NLS-1$
    private static final String TEST_PROJECT2 = "TestProject2"; //$NON-NLS-1$
    private static final String TEST_PROJECT3 = "TestProject3"; //$NON-NLS-1$
    private static final String TEST_PROJECT_OFF_ROOT = "TestProjectOffRoot"; //$NON-NLS-1$

    private AProject testProject1;
    private AProject testProject2;
    private AWorkspace workspace;

    @Before
    public void setUp() {
        testProject1 = newSimpleIpsProject(TEST_PROJECT1);
        testProject2 = newSimpleIpsProject(TEST_PROJECT2);
        workspace = Abstractions.getWorkspace();
    }

    @Test
    public void testPlainJavaWorkspaceRoot() {
        AWorkspaceRoot root = workspace.getRoot();

        assertThat(root.unwrap(), is(instanceOf(File.class)));
    }

    @Test
    public void testGetProject() {
        AWorkspaceRoot root = workspace.getRoot();

        assertThat(root.getProject(TEST_PROJECT1), is(testProject1));
        assertThat(root.getProject(TEST_PROJECT2), is(testProject2));
        // will create a new project
        assertThat(root.getProject(TEST_PROJECT3), is(notNullValue()));
    }

    @Test
    public void testGetProject_OffRoot() throws IOException {
        AWorkspaceRoot root = workspace.getRoot();
        Path offRootParent = Files.createTempDirectory("OffRoot");
        Path offRootProjectPath = offRootParent.resolve(TEST_PROJECT_OFF_ROOT).toAbsolutePath();
        AProject offRootProject = setUpOffRootProject(offRootProjectPath);

        assertThat(root.getProject(offRootProjectPath.toString()), is(offRootProject));
        assertThat(root.getProject(TEST_PROJECT_OFF_ROOT), is(offRootProject));
    }

    private AProject setUpOffRootProject(Path offRootProjectPath) {
        File offRootProjectDir = offRootProjectPath.toFile();
        offRootProjectDir.mkdirs();
        AProject offRootProject = Wrappers.wrap(offRootProjectDir).as(AProject.class);
        setupAbstractionProject(offRootProject);
        toIpsProject(offRootProject);
        return offRootProject;
    }

    @Test
    public void testGetFileForLocation() {
        AFile aFile = workspace.getRoot()
                .getFileForLocation(testProject1.getLocation().resolve(".ipsproject")); //$NON-NLS-1$

        assertThat(aFile, is(wrapperOf(testProject1.getLocation().resolve(".ipsproject").toFile()))); //$NON-NLS-1$
    }

    @Test
    public void testGetProjects() {
        Set<AProject> projects = workspace.getRoot().getProjects();

        assertThat(projects, hasItems(testProject1, testProject2));
    }

    @Test
    public void testGetProjects_IncludingOffRoot() throws IOException {
        AWorkspaceRoot root = workspace.getRoot();
        Path offRootParent = Files.createTempDirectory("OffRoot");
        Path offRootProjectPath = offRootParent.resolve(TEST_PROJECT_OFF_ROOT).toAbsolutePath();
        AProject offRootProject = setUpOffRootProject(offRootProjectPath);
        root.getProject(offRootProjectPath.toString());

        Set<AProject> projects = root.getProjects();

        assertThat(projects, hasItems(testProject1, testProject2, offRootProject));
    }

    @Test
    public void testGetType() {
        assertThat(workspace.getRoot().getType(), is(AResourceType.WORKSPACE));
    }
}
