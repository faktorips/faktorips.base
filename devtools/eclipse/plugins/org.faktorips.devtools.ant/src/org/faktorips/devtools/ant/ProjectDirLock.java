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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A lock guarding a single project/module directory against concurrent modification by more than
 * one {@link MavenProjectImportTask} or {@link MavenProjectRefreshTask} at a time, be it from two
 * threads of the same JVM or from separate JVMs, e.g. a parallel Maven build ({@code -T}) that runs
 * several Eclipse instances importing/refreshing overlapping directories at once.
 * <p>
 * The lock file is placed in the temp directory rather than in the guarded directory itself,
 * because the projects using these tasks check their working tree with {@code git status} and would
 * fail the build for an unexpected file.
 * <p>
 * The import and the refresh task lock the very same file for a given directory - named after the
 * SHA-1 hash of its canonical path - so the two tasks mutually exclude each other instead of only
 * guarding against concurrent instances of the same task.
 */
final class ProjectDirLock implements AutoCloseable {

    static final long DEFAULT_LOCK_TIMEOUT_MS = 5 * 60 * 1000L;

    private static final String LOCK_DIR = "org.faktorips.project-locks";
    private static final long LOCK_POLL_INTERVAL_MS = 500L;

    /**
     * Monitors per lock file, preventing {@link java.nio.channels.OverlappingFileLockException}
     * when two tasks of the same JVM lock the same directory.
     */
    private static final ConcurrentMap<String, ReentrantLock> JVM_LOCKS = new ConcurrentHashMap<>();

    private final File dir;
    private final ReentrantLock jvmLock;
    private final FileChannel channel;
    private final FileLock fileLock;
    private final AtomicBoolean closed = new AtomicBoolean();

    private ProjectDirLock(File dir, ReentrantLock jvmLock, FileChannel channel, FileLock fileLock) {
        this.dir = dir;
        this.jvmLock = jvmLock;
        this.channel = channel;
        this.fileLock = fileLock;
    }

    /**
     * Acquires the lock guarding {@code dir}, waiting up to {@code lockTimeoutMs} for another
     * process to release it first.
     *
     * @throws TimeoutException if the lock could not be acquired within {@code lockTimeoutMs}
     */
    // CSOFF: ThrowsCount
    static ProjectDirLock acquire(File dir, long lockTimeoutMs)
            throws IOException, InterruptedException, TimeoutException {
        // CSON: ThrowsCount
        return acquireUntil(dir, System.currentTimeMillis() + lockTimeoutMs);
    }

    /**
     * Same as {@link #acquire(File, long)}, but with an absolute deadline instead of a timeout
     * relative to the call. This lets {@link ProjectDirLocks} bound the wait for several
     * directories by one shared deadline instead of a fresh timeout per directory, which would let
     * the total wait grow with the number of directories.
     *
     * @throws TimeoutException if the lock could not be acquired before {@code deadline}
     */
    // CSOFF: ThrowsCount
    static ProjectDirLock acquireUntil(File dir, long deadline)
            throws IOException, InterruptedException, TimeoutException {
        // CSON: ThrowsCount
        Path lockFile = lockFileFor(dir);
        Path lockFileDir = lockFile.getParent();
        if (lockFileDir != null) {
            Files.createDirectories(lockFileDir);
        }
        ReentrantLock jvmLock = JVM_LOCKS.computeIfAbsent(lockFile.toString(), k -> new ReentrantLock());
        long remainingMs = Math.max(0, deadline - System.currentTimeMillis());
        if (!jvmLock.tryLock(remainingMs, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("Timed out waiting for the lock " + lockFile
                    + ". Another thread in this process is importing/refreshing " + dir + " concurrently.");
        }
        FileChannel channel = null;
        try {
            channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock fileLock = tryLockUntil(channel, lockFile, dir, deadline);
            System.out.println("acquired lock for " + dir);
            return new ProjectDirLock(dir, jvmLock, channel, fileLock);
            // CSOFF: IllegalCatch
        } catch (IOException | InterruptedException | TimeoutException | RuntimeException e) {
            // CSON: IllegalCatch
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException closeFailure) {
                e.addSuppressed(closeFailure);
            } finally {
                jvmLock.unlock();
            }
            throw e;
        }
    }

    // CSOFF: ThrowsCount
    private static FileLock tryLockUntil(FileChannel channel, Path lockFile, File dir, long deadline)
            throws IOException, InterruptedException, TimeoutException {
        // CSON: ThrowsCount
        while (true) {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                return lock;
            }
            if (System.currentTimeMillis() >= deadline) {
                throw new TimeoutException("Timed out waiting for the lock " + lockFile
                        + ". Another process is importing/refreshing " + dir + " concurrently.");
            }
            System.out.println("waiting for another process using " + dir);
            Thread.sleep(LOCK_POLL_INTERVAL_MS);
        }
    }

    /**
     * Idempotent: a second call is a no-op instead of throwing {@link IllegalMonitorStateException}
     * from an unmatched {@code jvmLock.unlock()}.
     * <p>
     * Failures while releasing the lock are logged and swallowed instead of thrown: by the time
     * this runs, the import/refresh this lock guarded has already succeeded or failed on its own
     * merits, so a transient failure to release an advisory file lock must not turn a successful
     * build into a failed one.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        try {
            fileLock.release();
        } catch (IOException e) {
            System.out.println("Could not release lock, ignoring: " + e.getMessage());
        }
        try {
            channel.close();
        } catch (IOException e) {
            System.out.println("Could not close lock file channel, ignoring: " + e.getMessage());
        }
        jvmLock.unlock();
        System.out.println("released lock for " + dir);
    }

    static Path lockFileFor(File dir) {
        String digest = sha1Hex(canonicalize(dir).toString());
        return Path.of(System.getProperty("java.io.tmpdir"), LOCK_DIR, digest + ".lock");
    }

    static Path canonicalize(File file) {
        try {
            return file.getCanonicalFile().toPath();
        } catch (IOException e) {
            return file.getAbsoluteFile().toPath();
        }
    }

    private static String sha1Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }
}
