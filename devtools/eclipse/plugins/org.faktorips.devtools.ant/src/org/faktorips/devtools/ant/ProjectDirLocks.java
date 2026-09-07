/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Acquires a {@link ProjectDirLock} for each of several directories at once, e.g. for a
 * {@link MavenProjectRefreshTask} that refreshes many projects in one call.
 * <p>
 * The directories are locked in a deterministic order (sorted by canonical path) so that two
 * processes locking overlapping sets of directories never deadlock by acquiring them in opposite
 * order. All locks share one deadline computed from {@code lockTimeoutMs}, so the total wait is
 * bounded by that timeout regardless of how many directories are locked.
 */
final class ProjectDirLocks implements AutoCloseable {

    private final List<ProjectDirLock> locks;

    private ProjectDirLocks(List<ProjectDirLock> locks) {
        this.locks = locks;
    }

    // CSOFF: ThrowsCount
    static ProjectDirLocks acquire(Collection<File> dirs, long lockTimeoutMs)
            throws IOException, InterruptedException, TimeoutException {
        // CSON: ThrowsCount
        Map<Path, File> distinctDirs = new LinkedHashMap<>();
        for (File dir : dirs) {
            distinctDirs.putIfAbsent(ProjectDirLock.canonicalize(dir), dir);
        }
        List<File> sortedDirs = distinctDirs.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
        System.out.println("locking " + sortedDirs.size() + " project director"
                + (sortedDirs.size() == 1 ? "y" : "ies"));
        long deadline = System.currentTimeMillis() + lockTimeoutMs;
        List<ProjectDirLock> acquired = new ArrayList<>();
        try {
            for (File dir : sortedDirs) {
                acquired.add(ProjectDirLock.acquireUntil(dir, deadline));
            }
            // CSOFF: IllegalCatch
        } catch (IOException | InterruptedException | TimeoutException | RuntimeException e) {
            // CSON: IllegalCatch
            close(acquired);
            throw e;
        }
        return new ProjectDirLocks(acquired);
    }

    @Override
    public void close() {
        close(locks);
    }

    private static void close(List<ProjectDirLock> locks) {
        for (int i = locks.size() - 1; i >= 0; i--) {
            locks.get(i).close();
        }
    }
}
