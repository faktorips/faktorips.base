/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;

/**
 * Base class for all Faktor-IPS ant tasks.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractIpsTask extends Task {


    /**
     * Creates a new task with the specified name.
     */
    public AbstractIpsTask(String taskName){
        setTaskName(taskName);
    }
    
    /**
     * Excecutes the Ant-Task 
     * 
     * {@inheritDoc}
     */
    public final void execute() throws BuildException {

        System.out.println(getTaskName() + ": execution started");

        try {
            executeInternal();
        } catch (BuildException e) {
            throw e;
        } catch (CoreException e) {
            throw new BuildException(e.getStatus().toString());
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            System.out.println(getTaskName() + ": execution finished");
        }
    }

    /**
     * The execution logic of the task needs to be implemented within this method
     *  
     * @throws Exception checked exceptions can just be delegated within this methods
     */
    protected abstract void executeInternal() throws Exception;
    
}
