/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.ant;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Implements a custom Ant-Task, which triggers a FaktorIPS-Builder build on a project in the
 * current Workspace. The <b>IpsProjectBuild</b> tag has a <b>name</b> attribute where the name of
 * the eclipse project within the workspace can be specified. If the specified project doesn't exist
 * in the workspace the IpsProjectBuild entry will be ignored during build and a information will be
 * logged to system out. This does not trigger a full build as the FullBuildTask does, only the
 * FaktorIPS-Builder is run.
 * 
 * @author Daniel Hohenberger <daniel.hohenberger@faktorzehn.de>
 */
public class IpsProjectBuildTask extends AbstractIpsTask {

    /**
     * The name of the project to build.
     */
    private String name;

    public IpsProjectBuildTask() {
        super("IpsProjectBuildTask");
    }

    /**
     * Excecutes the Ant-Task {@inheritDoc}
     */
    public void executeInternal() throws Exception {

        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        IProject project = buildIpsProject(workspace);

        handleMarkers(project);
    }

    private IProject buildIpsProject(IWorkspace workspace) throws CoreException {
        IProject project = null;
        if (name != null) {
            project = workspace.getRoot().getProject(name);
            if (project.exists()) {
                System.out.print("start building project: " + project.getName());
                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project.getName());
                if (ipsProject.exists()) {
                    System.out.println(", Faktor-IPS builder set: " + ipsProject.getIpsArtefactBuilderSet().getId()
                            + ", version: " + ipsProject.getIpsArtefactBuilderSet().getVersion());
                } else {
                    System.out.println();
                }
                project.build(IncrementalProjectBuilder.FULL_BUILD, "FaktorIPS-Builder", null, null);
                System.out.println("finished building project " + project.getName());
            } else {
                logProblem(project, IMarker.SEVERITY_WARNING, "Unable to locate the project " + project.getName()
                        + "within the workspace. The project will be skipped.");

            }
        }
        return project;

    }

    private void logProblem(IProject project, int severity, String text) {
        System.out.println("Project: " + project.getName() + ", " + getSeverityText(severity) + ": " + text);

    }

    private void handleMarkers(IProject project) throws CoreException {
        boolean projectHasErrors = false;
        IMarker markers[] = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        for (int j = 0; j < markers.length; j++) {
            IMarker marker = markers[j];
            Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
            if (severity != null && severity.intValue() == IMarker.SEVERITY_ERROR) {
                projectHasErrors = true;
            }
            logProblem(project, severity.intValue(), marker.getAttribute(IMarker.MESSAGE, "Problem has no message"));
        }

        if (projectHasErrors) {
            throw new BuildException("Unable to complete the build. Errors occurred in the following project: "
                    + project.getName());
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
