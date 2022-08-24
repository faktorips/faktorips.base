/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.eclipse.IpsClasspathContainerInitializer;
import org.junit.Before;
import org.junit.Test;

public class StandardJavaProjectConfiguratorTest extends AbstractIpsPluginTest {

    private IJavaProject javaProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        AProject platformProject = newPlatformProject("TestProject");
        if (Abstractions.isEclipseRunning()) {
            javaProject = addJavaCapabilities(platformProject).unwrap();
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testConfigureDefaultIpsProject() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());
            StandardJavaProjectConfigurator.configureDefaultIpsProject(javaProject);

            List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
            assertThat(classpathEntriesBefore.size(), is(classpathEntriesAfter.size() - 1));

            for (IClasspathEntry entry : classpathEntriesBefore) {
                assertThat(classpathEntriesAfter.contains(entry), is(true));
            }

            boolean containsIpsClasspathContainer = classpathEntriesAfter.stream()
                    .anyMatch(
                            entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)));
            assertThat(containsIpsClasspathContainer, is(true));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testConfigureDefaultIpsProject_Module() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            convertToModuleProject(javaProject);
            List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());

            StandardJavaProjectConfigurator.configureDefaultIpsProject(javaProject);

            List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
            assertThat(classpathEntriesAfter.size(), is(classpathEntriesBefore.size() + 1));

            Optional<IClasspathEntry> ipsClasspathContainerEntry = classpathEntriesAfter.stream()
                    .filter(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)))
                    .findFirst();
            assertThat(ipsClasspathContainerEntry.isPresent(), is(true));
            Optional<IClasspathAttribute> moduleAttribute = Arrays
                    .stream(ipsClasspathContainerEntry.get().getExtraAttributes())
                    .filter(a -> IClasspathAttribute.MODULE.equals(a.getName())).findFirst();
            assertThat(moduleAttribute.isPresent(), is(true));
            assertThat(moduleAttribute.get().getValue(), is("true"));

            for (IClasspathEntry entry : classpathEntriesBefore) {
                assertThat(classpathEntriesAfter.contains(entry), is(true));
            }
        }
    }

    @Test
    public void testConfigureJavaProject_Module() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            convertToModuleProject(javaProject);
            List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());

            StandardJavaProjectConfigurator.configureJavaProject(javaProject, false, false);

            List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
            assertThat(classpathEntriesAfter.size(), is(classpathEntriesBefore.size() + 1));
            for (IClasspathEntry entry : classpathEntriesBefore) {
                assertThat(classpathEntriesAfter.contains(entry), is(true));
            }

            Optional<IClasspathEntry> ipsClasspathContainerEntry = classpathEntriesAfter.stream()
                    .filter(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)))
                    .findFirst();
            assertThat(ipsClasspathContainerEntry.isPresent(), is(true));
            Optional<IClasspathAttribute> moduleAttribute = Arrays
                    .stream(ipsClasspathContainerEntry.get().getExtraAttributes())
                    .filter(a -> IClasspathAttribute.MODULE.equals(a.getName())).findFirst();
            assertThat(moduleAttribute.isPresent(), is(true));
            assertThat(moduleAttribute.get().getValue(), is("true"));
        }
    }

    @Test
    public void testConfigureJavaProject_GroovyEnabled() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());
            StandardJavaProjectConfigurator.configureJavaProject(javaProject, false, true);

            List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
            assertThat(classpathEntriesBefore.size(), is(classpathEntriesAfter.size() - 1));

            for (IClasspathEntry entry : classpathEntriesBefore) {
                assertThat(classpathEntriesAfter.contains(entry), is(true));
            }

            Optional<IClasspathEntry> containsIpsClasspathContainer = classpathEntriesAfter.stream()
                    .filter(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)))
                    .findFirst();
            assertThat(containsIpsClasspathContainer.isPresent(), is(true));
            String bundles = containsIpsClasspathContainer.get().getPath().lastSegment().toString();
            assertThat(bundles.contains("org.faktorips.runtime.groovy"), is(true));
        }
    }

    @Test
    public void testConfigureJavaProject_GroovyDisabled() throws CoreException {
        if (Abstractions.isEclipseRunning()) {
            List<IClasspathEntry> classpathEntriesBefore = Arrays.asList(javaProject.getRawClasspath());
            StandardJavaProjectConfigurator.configureJavaProject(javaProject, false, false);

            List<IClasspathEntry> classpathEntriesAfter = Arrays.asList(javaProject.getRawClasspath());
            assertThat(classpathEntriesBefore.size(), is(classpathEntriesAfter.size() - 1));

            for (IClasspathEntry entry : classpathEntriesBefore) {
                assertThat(classpathEntriesAfter.contains(entry), is(true));
            }

            Optional<IClasspathEntry> containsIpsClasspathContainer = classpathEntriesAfter.stream()
                    .filter(entry -> IpsClasspathContainerInitializer.CONTAINER_ID.equals(entry.getPath().segment(0)))
                    .findFirst();
            assertThat(containsIpsClasspathContainer.isPresent(), is(true));
            String bundles = containsIpsClasspathContainer.get().getPath().lastSegment().toString();
            assertThat(bundles.contains("org.faktorips.runtime.groovy"), is(false));
        }
    }
}
