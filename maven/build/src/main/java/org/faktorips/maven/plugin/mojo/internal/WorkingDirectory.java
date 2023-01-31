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

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.project.MavenProject;

public class WorkingDirectory {

    private WorkingDirectory() {
        // Util class
    }

    /**
     * Creates a working directory, intentionally outside the maven project target directory.
     * <p>
     * The root directory will be created in java.io.tmpdir. To minimize collisions with other
     * builds the full path of the maven project is SHA1 encoded and added at the end of the working
     * directory path.
     *
     * @param project the maven project
     * @return the working directory as {@link File}
     */
    public static File createFor(MavenProject project) {
        return createFor(project, project.getGroupId(), project.getArtifactId(),
                project.getVersion());
    }

    /**
     * Creates a working directory, intentionally outside the maven project target directory.
     * <p>
     * The root directory will be created in java.io.tmpdir then the directories from prefixes will
     * be added. To minimize collisions with other builds the full path of the maven project is SHA1
     * encoded and added at the end of the working directory path.
     *
     * @param project the maven project
     * @param prefixes add more directories for enhanced readability
     * @return the working directory as {@link File}
     */
    public static File createFor(MavenProject project, String... prefixes) {
        String prefix = "";
        if (prefixes.length != 0) {
            prefix = Arrays.stream(prefixes)
                    .collect(Collectors.joining(File.separator, File.separator, File.separator));
        }
        String sha1ProjectPath = DigestUtils.sha1Hex(project.getBasedir().getAbsolutePath());
        String tmp = System.getProperty("java.io.tmpdir");
        return new File(new File(tmp, prefix), sha1ProjectPath).getAbsoluteFile();
    }
}
