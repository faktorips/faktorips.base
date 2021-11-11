/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.maven.plugin.mojo.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkingDirectoryTest {

    private static final String FIX_PATH = "/fix/absolute/path/of/someDir";
    private static final String CHECK_SUM = "d02e58c95a504e83cd8a6c305e3bd4e18905be07";

    private MavenProject mp;

    @BeforeEach
    public void setup() {
        mp = mock(MavenProject.class);
        File file = new File(FIX_PATH);
        when(mp.getBasedir()).thenReturn(file);
        when(mp.getArtifactId()).thenReturn("artifact.id");
        when(mp.getGroupId()).thenReturn("group.id");
        when(mp.getVersion()).thenReturn("version");
    }

    @Test
    public void testCreateFor() {
        File workingDirectory = WorkingDirectory.createFor(mp);

        assertThat(workingDirectory.getAbsolutePath(), is(new StringBuilder(System.getProperty("java.io.tmpdir"))
                .append(File.separator)
                .append("group.id")
                .append(File.separator)
                .append("artifact.id")
                .append(File.separator)
                .append("version")
                .append(File.separator)
                .append(CHECK_SUM)
                .toString()));
    }

    @Test
    public void testCreateForWithPrefix() {
        File workingDirectory = WorkingDirectory.createFor(mp, "project.name");

        assertThat(workingDirectory.getAbsolutePath(), is(new StringBuilder(System.getProperty("java.io.tmpdir"))
                .append(File.separator)
                .append("project.name")
                .append(File.separator)
                .append(CHECK_SUM)
                .toString()));
    }
}