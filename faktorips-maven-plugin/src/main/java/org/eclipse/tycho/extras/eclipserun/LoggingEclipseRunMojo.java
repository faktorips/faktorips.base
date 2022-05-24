/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.eclipse.tycho.extras.eclipserun;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.EquinoxInstallation;
import org.eclipse.sisu.equinox.launching.EquinoxInstallationFactory;
import org.eclipse.sisu.equinox.launching.EquinoxLauncher;
import org.eclipse.tycho.core.maven.ToolchainProvider;
import org.eclipse.tycho.launching.LaunchConfiguration;
import org.eclipse.tycho.plugins.p2.extras.Repository;
import org.faktorips.maven.plugin.mojo.internal.BuildLogPrintStream;

public class LoggingEclipseRunMojo extends EclipseRunMojo {

    private static final ConcurrentMap<String, Object> WORKSPACE_LOCKS = new ConcurrentHashMap<>();

    private final File work;
    private final boolean clearWorkspaceBeforeLaunch;
    private final int forkedProcessTimeoutInSeconds;
    private final EquinoxLauncher launcher;
    private final String projectName;

    private final List<String> filter;

    // CSOFF: ParameterNumber
    public LoggingEclipseRunMojo(File work, boolean clearWorkspaceBeforeLaunch, MavenProject project,
            List<Dependency> dependencies, boolean addDefaultDependencies, String executionEnvironment,
            List<Repository> repositories, MavenSession session, List<String> jvmArgs, boolean skip,
            List<String> applicationsArgs, int forkedProcessTimeoutInSeconds, Map<String, String> environmentVariables,
            EquinoxInstallationFactory installationFactory, EquinoxLauncher launcher,
            ToolchainProvider toolchainProvider, EquinoxServiceFactory equinox, Logger logger,
            ToolchainManager toolchainManager, String projectName, Log mavenLog, List<String> filter) {
        // CSON: ParameterNumber
        super(work,
                clearWorkspaceBeforeLaunch,
                project,
                dependencies,
                addDefaultDependencies,
                executionEnvironment,
                repositories,
                session,
                jvmArgs,
                skip,
                applicationsArgs,
                forkedProcessTimeoutInSeconds,
                environmentVariables,
                installationFactory,
                launcher,
                toolchainProvider,
                equinox,
                logger,
                toolchainManager);
        this.work = work;
        this.clearWorkspaceBeforeLaunch = clearWorkspaceBeforeLaunch;
        this.forkedProcessTimeoutInSeconds = forkedProcessTimeoutInSeconds;
        this.launcher = launcher;
        this.projectName = projectName;
        super.setLog(mavenLog);
        this.filter = filter;
    }

    @Override
    void runEclipse(EquinoxInstallation runtime) throws MojoExecutionException {
        BuildLogPrintStream logStream = null;
        PrintStream stdStream = null;
        int returnCode;
        File expectedLog;
        try {
            File workspace = new File(work, "data").getAbsoluteFile();
            synchronized (WORKSPACE_LOCKS.computeIfAbsent(workspace.getAbsolutePath(), k -> new Object())) {
                if (clearWorkspaceBeforeLaunch) {
                    FileUtils.deleteDirectory(workspace);
                }
                LaunchConfiguration cli = createCommandLine(runtime);
                expectedLog = new File(workspace, ".metadata/.log");
                getLog().info("Expected eclipse log file: " + expectedLog.getCanonicalPath());

                // set new print stream as system out
                logStream = new BuildLogPrintStream(projectName, filter);
                stdStream = System.out;
                System.setOut(logStream.getPrintStream());

                // launch eclipse
                returnCode = launcher.execute(cli, forkedProcessTimeoutInSeconds);
            }
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            throw new MojoExecutionException("Error while executing platform", e);
        } finally {
            // set old system out
            if (stdStream != null) {
                System.setOut(stdStream);
            }
            log(logStream);
        }
        // CSON: IllegalCatch

        if (returnCode != 0) {
            throw new MojoExecutionException("Error while executing platform: return code=" + returnCode
                    + ", see content of " + expectedLog + "for more details.");
        }
    }

    private void log(BuildLogPrintStream logger) {
        if (logger != null) {
            logger.flush();
            try {
                for (String string : logger.getFilteredLogContent().split("\\R")) {
                    if (string.contains("ERROR")) {
                        getLog().error(logger.getProjectName() + ": " + string);
                    } else {
                        getLog().info(logger.getProjectName() + ": " + string);
                    }
                }
            } catch (IOException e) {
                // if we read the log from the catch block, we want the original build exception
                // to be thrown
                getLog().error("Error while trying to read the eclipse build log: ", e);
            } finally {
                logger.cleanUp();
            }
        }
    }
}
