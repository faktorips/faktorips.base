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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkingDirectoryTest {

    private static final String FIX_PATH = "/fix/absolute/path/of/someDir";
    private static final String CHECK_SUM = DigestUtils.sha1Hex(new File(FIX_PATH).getAbsolutePath());

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

        assertThat(workingDirectory.getAbsolutePath(),
                is(Path.of(getTmpDir(), "group.id", "artifact.id", "version", CHECK_SUM).toFile().getAbsolutePath()));
    }

    private String getTmpDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir.endsWith("/")) {
            tmpDir = tmpDir.substring(0, tmpDir.length() - 1);
        }
        return tmpDir;
    }

    @Test
    public void testCreateForWithPrefix() {
        File workingDirectory = WorkingDirectory.createFor(mp, "project.name");

        assertThat(workingDirectory.getAbsolutePath(),
                is(Path.of(getTmpDir(), "project.name", CHECK_SUM).toFile().getAbsolutePath()));
    }
}
