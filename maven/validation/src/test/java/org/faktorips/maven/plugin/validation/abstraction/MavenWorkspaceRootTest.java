/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation.abstraction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaImplementation;
import org.junit.jupiter.api.Test;

class MavenWorkspaceRootTest {

    @Test
    void testGetProject_MainProject() throws IOException {
        File directory = Files.createTempDirectory("foobar").toFile();
        MavenProject mavenProject = mockMavenProject(directory, "foo.bar", "baz");
        MavenWorkspaceRoot root = new MavenWorkspace(mavenProject, List.of()).getRoot();
        PlainJavaImplementation.get().setWorkspace(root.getWorkspace());

        AProject project = root.getProject("foo.bar.baz");

        assertThat(project.getName(), is("foo.bar.baz"));
        assertThat(project.unwrap(), is(directory));
    }

    @Test
    void testGetProject_Dependency() throws IOException {
        MavenProject mavenProject = mockMavenProject("foo.bar", "baz");
        File directory = Files.createTempDirectory("dep").toFile();
        MavenProject dependency = mockMavenProject(directory, "a.referenced", "project");
        MavenWorkspaceRoot root = new MavenWorkspace(mavenProject, List.of(dependency)).getRoot();
        PlainJavaImplementation.get().setWorkspace(root.getWorkspace());

        AProject project = root.getProject("a.referenced.project");

        assertThat(project.getName(), is("a.referenced.project"));
        assertThat(project.unwrap(), is(directory));
    }

    @Test
    void testGetNameAProject() throws IOException {
        MavenProject mavenProject = mockMavenProject("foo.bar", "baz");
        MavenWorkspaceRoot root = new MavenWorkspace(mavenProject, List.of()).getRoot();
        AProject project = root.getProject("foo.bar.baz");

        assertThat(root.getName(project), is("foo.bar.baz"));
    }

    @Test
    void testToProjectName() throws IOException {
        MavenProject mavenProject = mockMavenProject("foo.bar", "baz");

        String projectName = MavenWorkspaceRoot.toProjectName(mavenProject);

        assertThat(projectName, is("foo.bar.baz"));
    }

    private static MavenProject mockMavenProject(String groupId, String artifactId) throws IOException {
        return mockMavenProject(Files.createTempDirectory(artifactId).toFile(), groupId, artifactId);
    }

    private static MavenProject mockMavenProject(File directory, String groupId, String artifactId) {
        MavenProject mavenProject = mock(MavenProject.class);
        doReturn(groupId).when(mavenProject).getGroupId();
        doReturn(artifactId).when(mavenProject).getArtifactId();
        doReturn(directory).when(mavenProject).getBasedir();
        return mavenProject;
    }

}
