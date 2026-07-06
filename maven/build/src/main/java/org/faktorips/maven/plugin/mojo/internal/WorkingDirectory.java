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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.project.MavenProject;

public class WorkingDirectory {

    private static final ConcurrentMap<String, Object> JVM_LOCKS = new ConcurrentHashMap<>();

    private WorkingDirectory() {
        // Util class
    }

    /**
     * Returns a JVM-level monitor object for the given working directory. Used as the argument to
     * {@code synchronized} to prevent {@link java.nio.channels.OverlappingFileLockException} when
     * multiple Maven mojos run concurrently in the same JVM.
     *
     * @param workDir the working directory as returned by {@link #createFor}
     * @return a stable monitor object for this path
     */
    public static Object jvmLockFor(File workDir) {
        return JVM_LOCKS.computeIfAbsent(workDir.getAbsoluteFile().getPath(), k -> new Object());
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

    /**
     * Returns the lock file for the given working directory. The lock file is placed next to the
     * working directory (not inside it) so that the clean mojo can delete the entire working
     * directory without invalidating a lock held by a concurrent build mojo.
     *
     * @param workDir the working directory as returned by {@link #createFor}
     * @return the lock file (e.g. {@code /tmp/.../SHA1.lock})
     */
    public static File lockFileFor(File workDir) {
        return new File(workDir.getParentFile(), workDir.getName() + ".lock");
    }
}
