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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;
import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * Base class for all Faktor-IPS Ant tasks.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractIpsTask extends Task {

    /**
     * Creates a new task with the specified name.
     */
    public AbstractIpsTask(String taskName) {
        setTaskName(taskName);
    }

    /**
     * Excecutes the Ant task
     * 
     * {@inheritDoc}
     */
    @Override
    public final void execute() {
        // CSOFF: IllegalCatch
        System.out.println(getTaskName() + ": execution started");
        IIpsFeatureVersionManager[] managers = IpsPlugin.getDefault().getIpsFeatureVersionManagers();
        System.out.print("Installed Faktor-IPS Features: ");
        StringBuilder sb = new StringBuilder();
        StringBuilderJoiner.join(sb, managers, manager -> {
            sb.append("[Feature: ");
            sb.append(manager.getFeatureId());
            sb.append(", Version: ");
            sb.append(manager.getCurrentVersion());
            sb.append("]");
        });
        System.out.println(sb.toString());

        try {
            executeInternal();
        } catch (BuildException e) {
            throw e;
        } catch (CoreException e) {
            throw new BuildException(e.getStatus().toString(), e);
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            System.out.println(getTaskName() + ": execution finished");
        }
        // CSON: IllegalCatch
    }

    /**
     * The execution logic of the task needs to be implemented within this method
     * 
     * @throws Exception checked exceptions can just be delegated within this method
     */
    protected abstract void executeInternal() throws Exception;

}
