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

package org.faktorips.devtools.core.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * This manager is instantiated at start up of the faktor ips plugin and the plugin holds a single
 * instance of it during its life cycle. It is responsible for saving the states of the dependency
 * graphs that have been accessed during startup and shut down phase of the faktor ips plugin. The
 * states will be saved at shut down of the eclipse application. The dependency graph states are
 * saved to files located at the eclipse save location of the org.faktorips.devtools.core plugin.
 * Once a state of a dependency graph has been saved this state can be retrieved by this persistence
 * manager.
 * 
 * @author Peter Erzberger
 */
public class DependencyGraphPersistenceManager implements ISaveParticipant {

    /**
     * Returns the last persisted dependency graph for the provided IpsProject if available.
     * Otherwise <code>null</code> will be returned.
     * 
     * @throws CoreException if any exceptions occur while trying to load the dependency graph from
     *             the file system
     * @throws NullPointerException if the provided project is <code>null</code>
     */
    public DependencyGraph getDependencyGraph(IIpsProject project) throws CoreException {
        ArgumentCheck.notNull(project, this);
        File file = getDependencyGraphFile(project);
        if (file == null || !file.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            FileInputStream in = new FileInputStream(file);
            ois = new ObjectInputStream(in);
            DependencyGraph graph = (DependencyGraph)ois.readObject();
            ois.close();
            graph.setIpsProject(project);
            return graph;
        } catch (Exception e) {
            IpsPlugin
                    .log(new IpsStatus(
                            IStatus.WARNING,
                            "An Exception occurred while trying to establish the last state of the dependency graph for the project " + project.getName(), e)); //$NON-NLS-1$
            return new DependencyGraph(project);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e1) {
                    IpsPlugin
                            .log(new IpsStatus(
                                    "Unable to close the input stream while of the dependency graph file " + file.getAbsolutePath(), e1)); //$NON-NLS-1$
                    return new DependencyGraph(project);
                }
            }
        }

    }

    /**
     * Empty implementeration of the {@link ISaveParticipant} interface method
     */
    public void doneSaving(ISaveContext context) {
    }

    /**
     * Empty implementeration of the {@link ISaveParticipant} interface method
     */
    public void prepareToSave(ISaveContext context) throws CoreException {
    }

    /**
     * Empty implementeration of the {@link ISaveParticipant} interface method
     */
    public void rollback(ISaveContext context) {
    }

    /**
     * {@inheritDoc}
     */
    public void saving(ISaveContext context) throws CoreException {
        if (context.getKind() == ISaveContext.FULL_SAVE) {
            IpsPlugin plugin = IpsPlugin.getDefault();
            IpsModel model = (IpsModel)plugin.getIpsModel();
            DependencyGraph[] graphs = model.getCachedDependencyGraphs();
            for (int i = 0; i < graphs.length; i++) {
                save(graphs[i]);
            }
        }
    }

    private File getDependencyGraphFile(IIpsProject project) {
        return IpsPlugin.getDefault().getStateLocation().append(getDependencyGraphFileName(project)).toFile();
    }

    private String getDependencyGraphFileName(IIpsProject project) {
        return "dependencygraph." + project.getName(); //$NON-NLS-1$
    }

    private void save(DependencyGraph graph) {
        ObjectOutputStream os = null;
        File file = getDependencyGraphFile(graph.getIpsProject());
        try {
            FileOutputStream fileOs = new FileOutputStream(file);
            os = new ObjectOutputStream(fileOs);
            os.writeObject(graph);
            os.flush();
            os.close();
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus(IStatus.WARNING,
                    "Unable to save dependency graph file " + file.getAbsolutePath(), e)); //$NON-NLS-1$
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    IpsPlugin.log(new IpsStatus("Unable to close outputstream for dependency graph file " //$NON-NLS-1$
                            + file.getAbsolutePath()));
                }
            }
        }
    }
}
