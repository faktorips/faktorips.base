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
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class WorkingDirectoryTest {

    private static final String FIX_PATH = "/fix/absolute/path/of/someDir";
    private static final String CHECK_SUM = DigestUtils.sha1Hex(new File(FIX_PATH).getAbsolutePath());

    @TempDir
    File tempDir;

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

    @Test
    public void testJvmLockFor_returnsSameInstanceForSamePath() {
        File workDir = new File(tempDir, "work");

        Object lock1 = WorkingDirectory.jvmLockFor(workDir);
        Object lock2 = WorkingDirectory.jvmLockFor(workDir);

        assertThat(lock1, sameInstance(lock2));
    }

    @Test
    public void testLockFileFor_isNextToWorkDir() {
        File workDir = new File(tempDir, "sha1abc");

        File lockFile = WorkingDirectory.lockFileFor(workDir);

        assertThat(lockFile.getParentFile(), is(workDir.getParentFile()));
        assertThat(lockFile.getName(), is(workDir.getName() + ".lock"));
    }

    @Test
    public void testConcurrentAccess_doesNotThrowOverlappingFileLockException() throws Exception {
        File workDir = new File(tempDir, "work");
        workDir.mkdirs();
        File lockFile = WorkingDirectory.lockFileFor(workDir);
        lockFile.getParentFile().mkdirs();

        int threadCount = 2;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                synchronized (WorkingDirectory.jvmLockFor(workDir)) {
                    try (FileChannel ch = FileChannel.open(lockFile.toPath(),
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                            FileLock ignored = ch.lock()) {
                        // critical section: just verify the lock was obtained
                    }
                }
                return null;
            }));
        }

        ready.await();
        start.countDown();

        for (Future<Void> future : futures) {
            assertDoesNotThrow(() -> future.get(5, TimeUnit.SECONDS));
        }

        pool.shutdown();
    }
}
