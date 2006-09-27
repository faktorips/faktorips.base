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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Implements a custom Ant-Task, which triggers a full build on the current Workspace
 * 
 * @author Marcel Senf <marcel.senf@faktorzehn.de>
 */
public class FullBuild extends Task {

    /**
     * Excecutes the Ant-Task {@inheritDoc}
     */
    public void execute() throws BuildException {

        // Fetch Workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create ProgressMonitor
        IProgressMonitor monitor = new NullProgressMonitor();

        try {
            workspace.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
        }
        catch (Exception e) {
            throw new BuildException(e);
        }

    }
    
    
}
