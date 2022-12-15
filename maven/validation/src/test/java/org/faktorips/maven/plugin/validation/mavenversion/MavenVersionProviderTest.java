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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

class MavenVersionProviderTest {

    @Test
    void testGetProjectVersion() {
        MavenProject mavenProject = mock(MavenProject.class);
        doReturn("1.2.3-SNAPSHOT").when(mavenProject).getVersion();
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(mavenProject);

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("1.2.3-SNAPSHOT")));
    }

    @Test
    void testGetProjectVersion_NoMavenProject() {
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(null);

        assertThat(mavenVersionProvider.getProjectVersion(), is(new MavenVersion("0.0")));
    }

    @Test
    void testSetProjectVersion() {
        MavenProject mavenProject = mock(MavenProject.class);
        MavenVersionProvider mavenVersionProvider = new MavenVersionProvider(mavenProject);

        mavenVersionProvider.setProjectVersion(new MavenVersion("1.2.4-SNAPSHOT"));

        verify(mavenProject).setVersion("1.2.4-SNAPSHOT");
    }
}
