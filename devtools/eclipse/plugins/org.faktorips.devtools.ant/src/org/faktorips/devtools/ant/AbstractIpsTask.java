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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * Base class for all Faktor-IPS Ant tasks.
 *
 * @author Peter Erzberger
 */
public abstract class AbstractIpsTask extends Task {

    /**
     * Optional status file for communication of failed tasks to the calling Maven job without
     * throwing a {@link BuildException}.
     */
    private String statusFile;

    /**
     * Creates a new task with the specified name.
     */
    public AbstractIpsTask(String taskName) {
        setTaskName(taskName);
    }

    public String getStatusFile() {
        return statusFile;
    }

    public void setStatusFile(String statusFile) {
        this.statusFile = statusFile;
    }

    /**
     * Executes the Ant task
     *
     * {@inheritDoc}
     */
    @Override
    public final void execute() {
        getPreviousFailedTask().ifPresentOrElse(
                failedTask -> System.out.println("Skipping " + getTaskName() + " because " + failedTask + " failed."),
                () -> {
                    System.out.println(getTaskName() + ": execution started");
                    logInstalledFeatures();

                    // CSOFF: IllegalCatch
                    try {
                        executeInternal();
                    } catch (BuildException e) {
                        throw e;
                    } catch (CoreException e) {
                        throw new BuildException(e.getStatus().toString(), e);
                    } catch (Exception e) {
                        throw new BuildException(e);
                    } finally {
                        System.out.println(getTaskName() + ": execution finished");
                    }
                    // CSON: IllegalCatch
                });
    }

    private void logInstalledFeatures() {
        IIpsFeatureVersionManager[] managers = IIpsModelExtensions.get().getIpsFeatureVersionManagers();
        System.out.print("Installed Faktor-IPS Features: ");
        StringBuilder sb = new StringBuilder();
        StringBuilderJoiner.join(sb, managers, manager -> {
            sb.append("[Feature: ");
            sb.append(manager.getFeatureId());
            sb.append(", Version: ");
            sb.append(manager.getCurrentVersion());
            sb.append("]");
        });
        System.out.println(sb.toString());
    }

    /**
     * Fails the task. If a {@link #getStatusFile() statusFile} is set, the given message is written
     * in that file to signal a failed task without throwing an exception, otherwise a
     * {@link BuildException} is thrown with the given message.
     *
     * @param message explaining the failure
     */
    protected void fail(String message) {
        if (IpsStringUtils.isNotBlank(statusFile)) {
            try {
                Files.writeString(java.nio.file.Path.of(statusFile),
                        "[" + getTaskName() + "] " + message);
            } catch (IOException e) {
                log("Could not write status file " + statusFile, e, Project.MSG_ERR);
            }
        } else {
            throw new BuildException(message);
        }
    }

    private Optional<String> getPreviousFailedTask() {
        if (IpsStringUtils.isNotBlank(statusFile)) {
            Path statusFilePath = Path.of(statusFile);
            try {
                String status = Files.readString(statusFilePath);
                if (IpsStringUtils.isNotBlank(status)) {
                    return Optional.of(status.substring(1, status.indexOf(']')));
                }
            } catch (IOException e) {
                log("Could not read status file " + statusFile, e, Project.MSG_ERR);
            }
        }
        return Optional.empty();
    }

    /**
     * Waits until the given supplier returns a non-null value, polling every 500ms. Throws a
     * {@link BuildException} if the value is still null after the timeout.
     * <p>
     * This is needed because on Windows the OSGi DS activation order can trigger a circular
     * reference in m2e (felix.scr), which causes m2e services to be temporarily unavailable. Felix
     * retries the activation asynchronously, so waiting here.
     *
     * @param supplier the service to poll
     * @param serviceName used in the error message
     * @param timeoutMs maximum time to wait in milliseconds
     * @return the non-null service
     */
    protected <T> T waitForService(Supplier<T> supplier, String serviceName, long timeoutMs) {
        System.out.println("waiting for OSGi service: " + serviceName);
        long deadline = System.currentTimeMillis() + timeoutMs;
        int attempts = 0;
        while (System.currentTimeMillis() < deadline) {
            T value = supplier.get();
            attempts++;
            if (value != null) {
                if (attempts > 1) {
                    System.out.println("service " + serviceName + " available after " + attempts + " attempt(s)");
                }
                return value;
            }
            System.out.println(
                    "service " + serviceName + " not yet available (attempt " + attempts + "), retrying in 500ms...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BuildException("Interrupted while waiting for " + serviceName, e);
            }
        }
        throw new BuildException("OSGi service " + serviceName
                + " was not available after " + timeoutMs + "ms. "
                + "This may be caused by a circular reference in m2e DS activation (felix.scr).");
    }

    /**
     * The execution logic of the task needs to be implemented within this method
     *
     * @throws Exception checked exceptions can just be delegated within this method
     */
    protected abstract void executeInternal() throws Exception;

}
