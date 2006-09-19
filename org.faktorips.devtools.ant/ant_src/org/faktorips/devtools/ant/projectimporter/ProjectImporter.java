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

package org.faktorips.devtools.ant.projectimporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.ant.util.Copy;

/**
 * Implements a custom Ant-Task, which imports a given Directory to a running Eclipse Workspace as
 * Project.
 * 
 * @author Marcel Senf <marcel.senf@faktorzehn.de>
 */
public class ProjectImporter extends org.apache.tools.ant.Task {

    /**
     * path to project dir file
     * 
     * @deprecated Please access via Set/Get Methods
     */
    private String projectDir = "";

   
    /**
     * Sets the ANT-Attribute which describes the location of the Eclipseproject to import.
     * 
     * @param dir Path to the Project as String
     */
    public void setDir(String dir) {
        this.projectDir = dir;
    }

    /**
     * Returns the Path of the Eclipseproject to import as String
     * 
     * @return Path as String
     */
    public String getDir() {
        return this.projectDir;
    }

    /**
     * Assembles the Path to the .project File
     * 
     * @return File
     */
    private File getProjectFile() {
        return new File(this.getDir() + "/.project");

    }

    /**
     * Excecutes the Ant-Task {@inheritDoc}
     */
    public void execute() throws BuildException {

        // Check Dir-Attribute
        this.checkDir();

        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create
        IProgressMonitor monitor = new NullProgressMonitor();

        try {
            // get description provieded in .project File
            InputStream inputStream = new FileInputStream(this.getProjectFile());
            IProjectDescription description = null;

            try {
                description = workspace.loadProjectDescription(inputStream);
            }
            catch (Exception e) {
                throw new BuildException(e);
            }
            finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            // create new project with name provided in description
            IProject project = workspace.getRoot().getProject(description.getName());

            // check if project already exists in current workspace
            if (project.exists()) {
                throw new BuildException("Project " + project.getName() + " does already exist.");
            }
            project.create(description, monitor);

            // copy files

            Copy copyUtil = new Copy();
            copyUtil.copyDir(this.getDir(), project.getLocation().toString());

            // open and rebuild the project

            project.open(monitor);
            project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);

            project.close(monitor);
            
        }
        catch (Exception e) {
            throw new BuildException(e);
        }

    }
    

    /**
     * Does some Security-Checks on provided Directory-Attribute
     * 
     * @author Marcel Senf <marcel.senf@faktorzehn.de>
     * @throws BuildException
     */
    private void checkDir() throws BuildException {

        if (this.getDir().equals("") || this.getDir() == null) {
            throw new BuildException("Please provide the 'dir' attribute.");
        }

        if (!new File(this.getDir()).exists()) {
            throw new BuildException("Directory " + this.getDir() + " doesn't exist.");
        }

        if (!new File(this.getDir()).isDirectory()) {
            throw new BuildException("Provided 'dir' " + this.getDir() + " is not a Directory.");
        }

        if (!new File(this.getDir()).canRead()) {
            throw new BuildException("Provided 'dir' " + this.getDir() + " is not readable.");
        }
    }

}
