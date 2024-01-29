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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.m2e.AbstractMavenIpsProjectTest;
import org.junit.Before;
import org.junit.Test;

public class MavenVersionProviderTest extends AbstractMavenIpsProjectTest {

    private static final String POM_XML = "pom.xml";

    // @formatter:off
	private static final String POM_CONTENT =
	        """
    	<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    		xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    		<modelVersion>4.0.0</modelVersion>
    		<groupId>org.faktorips.test</groupId>
    		<artifactId>maven-ips-project</artifactId>
    		<version>1.2.3-SNAPSHOT</version>
    	</project>""";
    // @formatter:on

    private IIpsProject ipsProject;
    private AProject project;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        project = ipsProject.getProject();
        newPom(POM_CONTENT);
        addMavenNature();
    }

    private void newPom(String pomContent) {
        project.getFile(POM_XML).create(new ByteArrayInputStream(pomContent.getBytes()), null);
    }

    private void addMavenNature() throws CoreException {
        MavenPlugin.getProjectConfigurationManager().enableMavenNature(project.unwrap(), new ResolverConfiguration(),
                new NullProgressMonitor());
    }

    @Test
    public void testGetProjectVersion() {
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("1.2.3-SNAPSHOT")));
    }

    @Test
    public void testGetProjectVersion_NoMavenNature() {
        ipsProject = newIpsProject();
        project = ipsProject.getProject();
        newPom(POM_CONTENT);

        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("1.2.3-SNAPSHOT")));
    }

    @Test
    public void testGetProjectVersion_afterMavenUpdate() throws CoreException {
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        MavenProject mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(project.unwrap())
                .getMavenProject(new NullProgressMonitor());
        mavenProject.setVersion("1.2.4-SNAPSHOT");

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("1.2.4-SNAPSHOT")));
    }

    @Test
    public void testSetProjectVersion() {
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(ipsProject);

        mavenVersionProvider.setProjectVersion(new MavenVersion("1.2.4-SNAPSHOT"));

        assertThat(MavenPlugin.getMavenProjectRegistry().getProject(project.unwrap()).getMavenProject().getVersion(),
                is("1.2.4-SNAPSHOT"));
    }
}
