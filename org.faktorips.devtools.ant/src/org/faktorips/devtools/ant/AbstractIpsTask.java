/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.versionmanager.IIpsFeatureVersionManager;

/**
 * Base class for all Faktor-IPS ant tasks.
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
     * Excecutes the Ant-Task
     * 
     * {@inheritDoc}
     */
    public final void execute() throws BuildException {

        System.out.println(getTaskName() + ": execution started");
        IIpsFeatureVersionManager[] managers = IpsPlugin.getDefault().getIpsFeatureVersionManagers();
        System.out.print("Installed Faktor-IPS Features: ");
        StringBuffer versionSecionBuf = new StringBuffer();
        for (int i = 0; i < managers.length; i++) {
            versionSecionBuf.append("[Feature: "); //$NON-NLS-1$
            versionSecionBuf.append(managers[i].getFeatureId());
            versionSecionBuf.append(", Version: "); //$NON-NLS-1$
            versionSecionBuf.append(managers[i].getCurrentVersion());
            versionSecionBuf.append("]"); //$NON-NLS-1$
            if (i < managers.length - 1) {
                versionSecionBuf.append(", ");
            }
        }
        System.out.println(versionSecionBuf.toString());

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
