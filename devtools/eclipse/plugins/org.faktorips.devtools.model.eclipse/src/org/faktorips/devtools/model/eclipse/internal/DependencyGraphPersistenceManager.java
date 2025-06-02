/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.model.builder.IDependencyGraph;
import org.faktorips.devtools.model.builder.IDependencyGraphPersistenceManager;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.builder.DependencyGraph;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.util.ArgumentCheck;

/**
 * This manager is instantiated at start up of the Faktor-IPS plug-in and the plug-in holds a single
 * instance of it during its life cycle. It is responsible for saving the states of the dependency
 * graphs that have been accessed during startup and shut down phase of the Faktor-IPS plug-in. The
 * states will be saved at shut down of the eclipse application. The dependency graph states are
 * saved to files located at the eclipse save location of the org.faktorips.devtools.model plug-in.
 * Once a state of a dependency graph has been saved this state can be retrieved by this persistence
 * manager.
 *
 * @author Peter Erzberger
 */
public class DependencyGraphPersistenceManager implements ISaveParticipant, IDependencyGraphPersistenceManager {

    @Override
    public DependencyGraph getDependencyGraph(IIpsProject project) {
        ArgumentCheck.notNull(project, this);
        File file = getDependencyGraphFile(project);
        if (file == null || !file.exists()) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(in);) {
            DependencyGraph graph = (DependencyGraph)ois.readObject();
            graph.setIpsProject(project);
            return graph;
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            IpsLog.log(new IpsStatus(IStatus.WARNING,
                    "An Exception occurred while trying to establish the last state of the dependency graph for the project " //$NON-NLS-1$
                            + project.getName(),
                    e));
            return new DependencyGraph(project);
            // CSON: IllegalCatch
        }
    }

    @Override
    public void doneSaving(ISaveContext context) {
        // Empty implementation.
    }

    @Override
    public void prepareToSave(ISaveContext context) {
        // Empty implementation.
    }

    @Override
    public void rollback(ISaveContext context) {
        // Empty implementation.
    }

    @Override
    public void saving(ISaveContext context) {
        if (context.getKind() == ISaveContext.FULL_SAVE) {
            @SuppressWarnings("deprecation")
            IpsModel model = IpsModel.get();
            IDependencyGraph[] graphs = model.getCachedDependencyGraphs();
            for (IDependencyGraph graph : graphs) {
                save(graph);
            }
        }
    }

    private File getDependencyGraphFile(IIpsProject project) {
        return IpsModelActivator.getStateLocation().append(getDependencyGraphFileName(project)).toFile();
    }

    private String getDependencyGraphFileName(IIpsProject project) {
        return "dependencygraph." + project.getName(); //$NON-NLS-1$
    }

    private void save(IDependencyGraph graph) {
        File file = getDependencyGraphFile(graph.getIpsProject());
        try (FileOutputStream fileOs = new FileOutputStream(file);
                ObjectOutputStream os = new ObjectOutputStream(fileOs);) {
            os.writeObject(graph);
        } catch (IOException e) {
            IpsLog.log(new IpsStatus(IStatus.WARNING,
                    "Unable to save dependency graph file " + file.getAbsolutePath(), e)); //$NON-NLS-1$
        }
    }

}
