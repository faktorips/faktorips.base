/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.ant.projectimporter;

import org.apache.tools.ant.BuildException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
/**
 * Implements a custom Ant-Task, which imports a given Directory to a running Eclipse Workspace as
 * Project.
 * 
 * @author Marcel Senf <marcel.senf@faktorzehn.de>
 */
public class ProjectImporter extends org.apache.tools.ant.Task {

    /** Directory from which to import Data */
    private String baseDir = "";
    /** Name of the Project */
    private String projectName = "";

    public ProjectImporter() {
        super();
    }

    public void setDir(String dir) {
        this.baseDir = dir;
    }

    public void setName(String name) {
        this.projectName = name;
    }

    public void execute() throws BuildException {


        
        // check if properties are set
        if (this.baseDir == "" || this.projectName == "") {
            throw new BuildException("Please provide both attributes, 'dir' AND 'name'");
        }


        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        System.out.println("Hello World. Workspace: " + workspace.toString());
        
//        IProgressMonitor monitor = (IProgressMonitor) getProject().getReferences().get(AntCorePlugin.ECLIPSE_PROGRESS_MONITOR);
//        System.out.println(monitor.toString());
    }
}
