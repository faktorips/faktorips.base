/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plainjava.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaImplementation;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaWorkspace;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Reproduces the deadlock seen with {@code mvn -T 4C}: two modules are validated in parallel, one
 * references the other, and both request their builder set for the first time.
 * <ul>
 * <li>Thread 1: {@code IpsProject A -> IpsModel -> (builder set init) -> IpsProject B}</li>
 * <li>Thread 2: {@code IpsProject B -> IpsModel}</li>
 * </ul>
 * Thread 2 keeps requesting the builder set of project B while thread 1 initializes project A, so
 * that the narrow window between "thread 1 holds the model" and "thread 1 reaches project B" is hit
 * reliably.
 */
public class ParallelBuilderSetInitializationDeadlockTest {

    private static final int ITERATIONS = 50;
    private static final long TIMEOUT_SECONDS = 10;

    private static final String IPS_PROJECT_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <IpsProject changesInTimeNamingConvention="FIPS" modelProject="true" persistentProject="false" productDefinitionProject="true" runtimeIdPrefix="">
                <RequiredIpsFeatures>
                    <RequiredIpsFeature id="org.faktorips.feature" minVersion="3.18.0"/>
                </RequiredIpsFeatures>
                <IpsArtefactBuilderSet id="org.faktorips.devtools.stdbuilder.ipsstdbuilderset"/>
                <IpsObjectPath basePackageDerived="test" basePackageMergable="test" outputDefinedPerSrcFolder="false" outputFolderDerivedSources="derived" outputFolderMergableSources="src">
                    <Entry basePackageDerived="" basePackageMergable="" outputFolderDerived="" outputFolderMergable="" reexported="true" sourceFolder="model" tocPath="toc.xml" type="src" validationMessagesBundle="validation-messages"/>
                    %s
                </IpsObjectPath>
                <IpsProjectProperties/>
            </IpsProject>
            """;

    private File workspaceDir;

    @BeforeEach
    public void setUp() throws IOException {
        workspaceDir = Files.createTempDirectory(getClass().getSimpleName()).toFile();
        createProject("projectA", "<Entry type=\"project\" referencedIpsProject=\"projectB\"/>");
        createProject("projectB", "");
        PlainJavaImplementation.get().setWorkspace(new PlainJavaWorkspace(workspaceDir));
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(workspaceDir);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    public void testGetIpsArtefactBuilderSet_ParallelOnReferencingProjects_NoDeadlock() throws Exception {
        for (int i = 0; i < ITERATIONS; i++) {
            assertNoDeadlock(i);
        }
    }

    @SuppressWarnings("deprecation")
    private void assertNoDeadlock(int iteration) throws InterruptedException {
        IpsModel.reInit();
        IIpsModel model = IIpsModel.get();
        IIpsProject projectA = model.getIpsProject(PlainJavaImplementation.get().getWorkspace().getRoot()
                .getProject("projectA"));
        // the instance referenced by project A is the one the parallel build of module B works on
        IIpsProject projectB = projectA.getAllReferencedIpsProjects().get(0);

        CountDownLatch start = new CountDownLatch(1);
        AtomicBoolean initializationOfAFinished = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread threadA = newThread("mvn-builder-projectA", start, failure, () -> {
            projectA.getIpsArtefactBuilderSet();
            initializationOfAFinished.set(true);
        });
        Thread threadB = newThread("mvn-builder-projectB", start, failure, () -> {
            while (!initializationOfAFinished.get()) {
                projectB.getIpsArtefactBuilderSet();
            }
        });
        threadA.start();
        threadB.start();
        start.countDown();

        threadA.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        threadB.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));

        boolean deadlocked = threadA.isAlive() || threadB.isAlive();
        String report = deadlocked ? deadlockReport() : "";
        if (deadlocked) {
            threadA.interrupt();
            threadB.interrupt();
        }
        assertThat("Iteration " + iteration + ": deadlock while initializing builder sets in parallel:\n" + report,
                deadlocked, is(false));
        if (failure.get() != null) {
            throw new AssertionError("Unexpected exception in iteration " + iteration, failure.get());
        }
    }

    private static Thread newThread(String name,
            CountDownLatch start,
            AtomicReference<Throwable> failure,
            Runnable action) {
        Thread thread = new Thread(() -> {
            try {
                start.await();
                action.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException | Error e) {
                failure.compareAndSet(null, e);
            }
        }, name);
        thread.setDaemon(true);
        return thread;
    }

    private static String deadlockReport() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] ids = threadMXBean.findDeadlockedThreads();
        if (ids == null) {
            return "(no Java-level deadlock reported by the JVM)";
        }
        StringBuilder sb = new StringBuilder();
        for (ThreadInfo info : threadMXBean.getThreadInfo(ids, true, true)) {
            sb.append(info);
        }
        return sb.toString();
    }

    private void createProject(String name, String additionalEntries) throws IOException {
        File projectDir = new File(workspaceDir, name);
        File modelDir = new File(projectDir, "model");
        Files.createDirectories(modelDir.toPath());
        Files.writeString(new File(projectDir, ".ipsproject").toPath(),
                IPS_PROJECT_TEMPLATE.formatted(additionalEntries), StandardCharsets.UTF_8);
    }

}
