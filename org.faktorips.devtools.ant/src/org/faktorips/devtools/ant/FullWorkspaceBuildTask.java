/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.ant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Implements a custom Ant-Task, which triggers a full build on the current Workspace
 * 
 * @author Marcel Senf <marcel.senf@faktorzehn.de>
 */
public class FullWorkspaceBuildTask extends Task {

    /**
     * Excecutes the Ant-Task {@inheritDoc}
     */
    public void execute() throws BuildException {

        System.out.println("FullWorkspaceBuildTask: execution started");
        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create ProgressMonitor
        IProgressMonitor monitor = new NullProgressMonitor();

        try {
            workspace.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
            
            //Iterate over Projects in Workspace to find Warning and Errormarkers
            IProject projects[] = workspace.getRoot().getProjects();
            
            ArrayList projectsWithErrors = new ArrayList();
            for (int i = 0; i < projects.length; i++) {
                IProject project = projects[i];
                IMarker markers[] = project.findMarkers(IMarker.PROBLEM,true ,IResource.DEPTH_INFINITE);
                for (int j = 0; j < markers.length; j++) {
                    IMarker marker = markers[j];
                    Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
                    if(severity != null && severity.intValue() == IMarker.SEVERITY_ERROR){
                        projectsWithErrors.add(project);
                    }
                    System.out.println("Project: " + project.getName() + ", " + 
                                        getSeverityText(severity) + ": " + marker.getAttribute(IMarker.MESSAGE, "Problem has no message"));
                }
            }
            
            if(projectsWithErrors.size() > 0){
                throw new BuildException("Unable to complete the build. Errors occured in the following projects: " + 
                        getErroneousProjectsAsText(projectsWithErrors));
            }
        }
        catch(BuildException e){
            throw e;
        }
        catch(CoreException e){
            throw new BuildException(e.getStatus().toString());
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
        finally{
            System.out.println("FullWorkspaceBuildTask: execution finished");
        }
    }
    
    private String getSeverityText(Integer severityInteger){
        int severity = severityInteger.intValue();
        if(severity == IMarker.SEVERITY_ERROR){
            return "ERROR";
        }
        if(severity == IMarker.SEVERITY_WARNING){
            return "WARNING";
        }
        if(severity == IMarker.SEVERITY_INFO){
            return "INFO";
        }
        throw new IllegalArgumentException("Unexpected severity: " + severity);
    }
    
    private String getErroneousProjectsAsText(List projectList){
        StringBuffer buf = new StringBuffer();
        for (Iterator it = projectList.iterator(); it.hasNext();) {
            IProject project = (IProject)it.next();
            buf.append(project.getName());
            if(it.hasNext()){
                buf.append(", ");
            }
        }
        return buf.toString();
    }
    
}
