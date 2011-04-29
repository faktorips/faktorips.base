/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Implements a custom Ant-Task, which triggers a full build on the current Workspace. Alternatively
 * one or more <b>EclipseProject</b> nested tags can be specified to indicate for which projects a
 * full build should be executed. The <b>EclipseProject</b> tag has a <b>name</b> attribute where
 * the name of the eclipse project within the workspace can be specified. If the specified project
 * doesn't exist in the workspace the EclipseProject entry will be ignored during build and a
 * information will be logged to system out.
 * 
 * @author Marcel Senf <marcel.senf@faktorzehn.de>
 * @author Peter Erzberger <peter.erzberger@faktorzehn.de>
 */
public class FullBuildTask extends AbstractIpsTask {

    private List eclipseProjects = new ArrayList();

    public FullBuildTask() {
        super("FullBuildTask");
    }

    public void addEclipseProject(EclipseProject eclipsProject) {
        eclipseProjects.add(eclipsProject);
    }

    /**
     * Excecutes the Ant-Task {@inheritDoc}
     */
    public void executeInternal() throws Exception {

        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create ProgressMonitor
        IProgressMonitor monitor = new NullProgressMonitor();

        IProject projects[] = null;
        if (eclipseProjects.isEmpty()) {
            // Iterate over Projects in Workspace to find Warning and Errormarkers
            projects = workspace.getRoot().getProjects();
            if (projects.length > 0) {
                System.out.println("The following IPS-Projects are about to be built: ");
            }
            for (int i = 0; i < projects.length; i++) {
                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projects[i].getName());
                if (ipsProject.exists()) {
                    System.out.println("IPS-Project: " + ipsProject.getName() + ", IPS-Builder Set: "
                            + ipsProject.getIpsArtefactBuilderSet().getId() + ", Version: "
                            + ipsProject.getIpsArtefactBuilderSet().getVersion());
                }
            }
            workspace.build(IncrementalProjectBuilder.FULL_BUILD, monitor);

        } else {
            projects = buildEclipseProjects(workspace);
        }

        handleMarkers(projects);
    }

    private IProject[] buildEclipseProjects(IWorkspace workspace) throws CoreException {
        List existingProjects = new ArrayList();
        for (Iterator it = eclipseProjects.iterator(); it.hasNext();) {
            EclipseProject eclipseProject = (EclipseProject)it.next();
            String name = eclipseProject.getName();
            if (name != null) {
                IProject project = workspace.getRoot().getProject(name);
                if (project.exists()) {
                    existingProjects.add(project);
                    System.out.print("start building project: " + project.getName());
                    IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
                    if (ipsProject.exists()) {
                        System.out.println(", Faktor-IPS builder set: " + ipsProject.getIpsArtefactBuilderSet().getId()
                                + ", version: " + ipsProject.getIpsArtefactBuilderSet().getVersion());
                    } else {
                        System.out.println();
                    }
                    project.build(IncrementalProjectBuilder.FULL_BUILD, null);
                    System.out.println("finished building project " + project.getName());
                } else {
                    logProblem(project, IMarker.SEVERITY_WARNING, "Unable to locate the project " + project.getName()
                            + "within the workspace. The project will be skipped.");

                }
            }
        }
        return (IProject[])existingProjects.toArray(new IProject[existingProjects.size()]);

    }

    private void logProblem(IProject project, int severity, String text) {
        System.out.println("Project: " + project.getName() + ", " + getSeverityText(severity) + ": " + text);

    }

    private void handleMarkers(IProject[] projects) throws CoreException {
        Set projectsWithErrors = new HashSet();
        for (int i = 0; i < projects.length; i++) {
            IProject project = projects[i];
            IMarker markers[] = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            for (int j = 0; j < markers.length; j++) {
                IMarker marker = markers[j];
                Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
                if (severity != null && severity.intValue() == IMarker.SEVERITY_ERROR) {
                    projectsWithErrors.add(project);
                }
                logProblem(project, severity.intValue(), marker.getAttribute(IMarker.MESSAGE, "Problem has no message"));
            }
        }

        if (projectsWithErrors.size() > 0) {
            throw new BuildException("Unable to complete the build. Errors occurred in the following projects: "
                    + getErroneousProjectsAsText(projectsWithErrors));
        }
    }

    private String getSeverityText(int severity) {
        if (severity == IMarker.SEVERITY_ERROR) {
            return "ERROR";
        }
        if (severity == IMarker.SEVERITY_WARNING) {
            return "WARNING";
        }
        if (severity == IMarker.SEVERITY_INFO) {
            return "INFO";
        }
        throw new IllegalArgumentException("Unexpected severity: " + severity);
    }

    private String getErroneousProjectsAsText(Set projectSet) {
        StringBuffer buf = new StringBuffer();
        for (Iterator it = projectSet.iterator(); it.hasNext();) {
            IProject project = (IProject)it.next();
            buf.append(project.getName());
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
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
