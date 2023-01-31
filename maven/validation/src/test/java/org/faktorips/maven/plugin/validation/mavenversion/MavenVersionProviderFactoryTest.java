/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation.mavenversion;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.maven.plugin.validation.IpsDependency;
import org.junit.jupiter.api.Test;

class MavenVersionProviderFactoryTest {

    @Test
    void testCreateVersionProvider() {
        IIpsProject ipsProject = new IpsProject(IIpsModel.get(), "foo");
        MavenProject mavenProject = mock(MavenProject.class);
        doReturn("1.2.3-SNAPSHOT").when(mavenProject).getVersion();
        IpsDependency ipsDependency = new IpsDependency(null, null, null, null, ipsProject, mavenProject);
        MavenVersionProviderFactory factory = new MavenVersionProviderFactory(Set.of(ipsDependency));

        MavenVersionProvider versionProvider = factory.createVersionProvider(ipsProject);

        assertThat(versionProvider.getProjectVersion(), is(new MavenVersion("1.2.3-SNAPSHOT")));
    }

    @Test
    void testCreateVersionProvider_UnknownProject() {
        MavenVersionProviderFactory factory = new MavenVersionProviderFactory(Set.of());
        IIpsProject ipsProject = new IpsProject(IIpsModel.get(), "foobar");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> factory.createVersionProvider(ipsProject));
        assertThat(exception.getMessage(), containsString("foobar"));
    }

    @Test
    void testCreateVersionProvider_MultipleProjects() {
        IIpsProject ipsProject1 = new IpsProject(IIpsModel.get(), "foo");
        MavenProject mavenProject1 = mock(MavenProject.class);
        doReturn("1.2.3-SNAPSHOT").when(mavenProject1).getVersion();
        IpsDependency ipsDependency1 = new IpsDependency("foo", null, null, null, ipsProject1, mavenProject1);
        IIpsProject ipsProject2 = new IpsProject(IIpsModel.get(), "bar");
        MavenProject mavenProject2 = mock(MavenProject.class);
        doReturn("2.3.4.release").when(mavenProject2).getVersion();
        IpsDependency ipsDependency2 = new IpsDependency("bar", null, null, null, ipsProject2, mavenProject2);
        MavenVersionProviderFactory factory = new MavenVersionProviderFactory(Set.of(ipsDependency1, ipsDependency2));

        MavenVersionProvider versionProvider = factory.createVersionProvider(ipsProject2);

        assertThat(versionProvider.getProjectVersion(), is(new MavenVersion("2.3.4.release")));
    }

}
