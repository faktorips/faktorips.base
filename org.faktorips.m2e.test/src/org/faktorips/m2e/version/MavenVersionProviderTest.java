/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.m2e.version;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class MavenVersionProviderTest extends AbstractIpsPluginTest {

    private static final String POM_XML = "pom.xml";

    // @formatter:off
	private static final String POM_CONTENT =
	        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
            + "	<modelVersion>4.0.0</modelVersion>\n"
            + "	<groupId>org.faktorips.test</groupId>\n"
            + "	<artifactId>maven-ips-project</artifactId>\n"
            + "	<version>1.2.3-SNAPSHOT</version>\n"
            + "</project>";
    // @formatter:on

    private IIpsProject ipsProject;
    private IProject project;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        project = ipsProject.getProject();
        newPom(POM_CONTENT);
        addMavenNature();
    }

    private void newPom(String pomContent) throws CoreException {
        project.getFile(POM_XML).create(new ByteArrayInputStream(pomContent.getBytes()), true, null);
    }

    private void addMavenNature() throws CoreException {
        MavenPlugin.getProjectConfigurationManager().enableMavenNature(project, new ResolverConfiguration(),
                new NullProgressMonitor());
    }

    @Test
    public void testGetProjectVersion() {
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("1.2.3-SNAPSHOT")));
    }

    @Test
    public void testGetProjectVersion_NoMavenNature() throws CoreException {
        ipsProject = newIpsProject();
        project = ipsProject.getProject();
        newPom(POM_CONTENT);

        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("1.2.3-SNAPSHOT")));
    }

    @Test
    public void testGetProjectVersion_afterMavenUpdate() throws CoreException {
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(project)
                .getMavenProject(new NullProgressMonitor());
        mavenProject.setVersion("1.2.4-SNAPSHOT");

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("1.2.4-SNAPSHOT")));
    }

    @Test
    public void testSetProjectVersion() {
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        mavenVersionProvider.setProjectVersion(new MavenVersion("1.2.4-SNAPSHOT"));

        assertThat(MavenPlugin.getMavenProjectRegistry().getProject(project).getMavenProject().getVersion(),
                is("1.2.4-SNAPSHOT"));
    }
}
