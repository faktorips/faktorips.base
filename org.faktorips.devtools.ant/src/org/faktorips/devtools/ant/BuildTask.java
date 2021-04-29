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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Implements a custom Ant task, which triggers a build on the current workspace. Alternatively one
 * or more {@code EclipseProject} nested tags can be specified to indicate for which projects a full
 * build should be executed. The {@code EclipseProject} tag has a {@code name} attribute where the
 * name of the Eclipse project within the workspace can be specified. If the specified project
 * doesn't exist in the workspace the {@code EclipseProject} entry will be ignored during build and
 * a information will be logged to system out.
 */
public class BuildTask extends AbstractIpsTask {

    private List<EclipseProject> eclipseProjects = new ArrayList<>();
    private boolean fullBuild = true;
    private boolean ipsOnly = false;

    public BuildTask() {
        super("BuildTask");
    }

    public void addEclipseProject(EclipseProject eclipsProject) {
        eclipseProjects.add(eclipsProject);
    }

    public boolean isFullBuild() {
        return fullBuild;
    }

    public void setFullBuild(boolean fullBuild) {
        this.fullBuild = fullBuild;
    }

    public boolean isIpsOnly() {
        return ipsOnly;
    }

    public void setIpsOnly(boolean ipsOnly) {
        this.ipsOnly = ipsOnly;
    }

    /**
     * Excecutes the Ant task.
     */
    @Override
    public void executeInternal() throws Exception {
        WorkspaceJob job = new WorkspaceJob("build") {
            @Override
            public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                buildWorkspace(monitor);
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.BUILD);
        job.schedule();
        job.join();
        IStatus result = job.getResult();
        if (result.getSeverity() == Status.ERROR) {
            if (result.getException() instanceof RuntimeException) {
                throw (RuntimeException)result.getException();
            }
            throw new RuntimeException("Error while building Faktor-IPS: " + result.getMessage(),
                    result.getException());
        }
    }

    private IProject[] buildEclipseProjects(IWorkspace workspace) throws CoreException {
        List<IProject> existingProjects = new ArrayList<>();
        for (EclipseProject eclipseProject : eclipseProjects) {
            String name = eclipseProject.getName();
            if (name != null) {
                IProject project = workspace.getRoot().getProject(name);
                if (project.exists()) {
                    existingProjects.add(project);
                    System.out.print("start building project: " + project.getName());
                    IIpsProject ipsProject = IIpsModel.get().getIpsProject(project.getName());
                    if (ipsProject.exists()) {
                        System.out.println(", Faktor-IPS builder set: " + ipsProject.getIpsArtefactBuilderSet().getId()
                                + ", version: " + ipsProject.getIpsArtefactBuilderSet().getVersion());
                    } else {
                        System.out.println();
                    }
                    if (fullBuild) {
                        System.out.println("Perform full build");
                        if (ipsOnly) {
                            project.build(IncrementalProjectBuilder.FULL_BUILD,
                                    IpsBuilder.BUILDER_ID,
                                    Collections.emptyMap(),
                                    null);
                        } else {
                            project.build(IncrementalProjectBuilder.FULL_BUILD, null);
                        }
                    } else {
                        System.out.println("Perform incremental build");
                        if (ipsOnly) {
                            project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,
                                    IpsBuilder.BUILDER_ID,
                                    Collections.emptyMap(),
                                    null);
                        } else {
                            project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
                        }
                    }
                    System.out.println("finished building project " + project.getName());
                } else {
                    logProblem(project, IMarker.SEVERITY_WARNING, "Unable to locate the project " + project.getName()
                            + "within the workspace. The project will be skipped.");

                }
            }
        }
        return existingProjects.toArray(new IProject[existingProjects.size()]);

    }

    private void logProblem(IProject project, Integer severity, String text) {
        System.out.println("Project: " + project.getName() + ", " + getSeverityText(severity) + ": " + text);
    }

    private void handleMarkers(IProject[] projects) throws CoreException {
        Set<IProject> projectsWithErrors = new HashSet<>();
        for (IProject project : projects) {
            IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            for (IMarker marker : markers) {
                Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
                if (severity != null && severity.intValue() == IMarker.SEVERITY_ERROR) {
                    projectsWithErrors.add(project);
                }
                logProblem(project, severity,
                        marker.getAttribute(IMarker.MESSAGE, "Problem has no message"));
            }
        }

        if (projectsWithErrors.size() > 0) {
            throw new BuildException("Unable to complete the build. Errors occurred in the following projects: "
                    + getErroneousProjectsAsText(projectsWithErrors));
        }
    }

    private String getSeverityText(Integer severity) {
        if (severity == null) {
            return "";
        }

        switch (severity) {
            case IMarker.SEVERITY_ERROR:
                return "ERROR";
            case IMarker.SEVERITY_WARNING:
                return "WARNING";
            case IMarker.SEVERITY_INFO:
                return "INFO";
            default:
                return "Severity-" + severity;
        }
    }

    private String getErroneousProjectsAsText(Set<IProject> projectSet) {
        return projectSet.stream().map(IProject::getName).collect(Collectors.joining(", "));
    }

    private void buildWorkspace(IProgressMonitor monitor) throws CoreException {
        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = null;
        if (eclipseProjects.isEmpty()) {
            // Iterate over Projects in Workspace to find Warning and Errormarkers
            projects = workspace.getRoot().getProjects();
            if (projects.length > 0) {
                System.out.println("The following IPS-Projects are about to be built: ");
            }
            for (IProject project2 : projects) {
                IIpsProject ipsProject = IIpsModel.get()
                        .getIpsProject(project2.getName());
                if (ipsProject.exists()) {
                    System.out.println("IPS-Project: " + ipsProject.getName() + ", IPS-Builder Set: "
                            + ipsProject.getIpsArtefactBuilderSet().getId() + ", Version: "
                            + ipsProject.getIpsArtefactBuilderSet().getVersion());
                }
            }
            if (fullBuild) {
                System.out.println("Perform full build");
                workspace.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
            } else {
                System.out.println("Perform incremental build");
                workspace.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
            }
        } else {
            projects = buildEclipseProjects(workspace);
        }
        handleMarkers(projects);
    }

    /**
     * This class covers the nested tag EclipseProject.
     * 
     * @author Peter Erzberger
     */
    public static class EclipseProject {

        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
