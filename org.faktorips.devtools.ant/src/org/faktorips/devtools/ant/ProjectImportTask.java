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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Implements a custom Ant-Task, which imports a given Directory to a running Eclipse Workspace as
 * Project.
 * 
 * @author Marcel Senf <marcel.senf@faktorzehn.de>
 */
public class ProjectImportTask extends AbstractIpsTask {

    /**
     * path to project dir file
     */
    private String projectDir = "";

    public ProjectImportTask() {
        super("ProjectImportTask");
    }

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
     * Executes the Ant-Task {@inheritDoc}
     */
    public void executeInternal() throws Exception {

        // Check Dir-Attribute
        checkDir();

        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create
        IProgressMonitor monitor = new NullProgressMonitor();

        // get description provieded in .project File
        InputStream inputStream = new FileInputStream(this.getProjectFile());
        IProjectDescription description = null;

        try {
            description = workspace.loadProjectDescription(inputStream);
        } finally {
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
        System.out.println("importing: " + project.getName());
        project.create(description, monitor);

        // copy files

        RecursiveCopy copyUtil = new RecursiveCopy();
        copyUtil.copyDir(this.getDir(), project.getLocation().toString());

        project.open(monitor);
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        // build is done via FullBuild-Target separately
    }

    /**
     * Does some Security-Checks on provided Directory-Attribute
     * 
     * @author Marcel Senf <marcel.senf@faktorzehn.de>
     * @throws BuildException
     */
    private void checkDir() throws BuildException {

        if (this.getDir() == null || this.getDir().equals("")) {
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
