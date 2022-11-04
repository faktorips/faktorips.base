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

import org.eclipse.core.resources.ResourcesPlugin;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.plugin.IpsCompositeSaveParticipant;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class EclipseIpsModelActivator implements BundleActivator {

    public static final String PLUGIN_ID = "org.faktorips.devtools.model.eclipse"; //$NON-NLS-1$

    private static DependencyGraphPersistenceManager dependencyGraphPersistenceManager;

    @Override
    public void start(BundleContext context) throws Exception {
        IpsModelActivator.get().setWorkspaceAbstractionsImplementation(new EclipseWorkspaceImplementation());
        dependencyGraphPersistenceManager = new DependencyGraphPersistenceManager();
        IpsCompositeSaveParticipant saveParticipant = new IpsCompositeSaveParticipant();
        saveParticipant.addSaveParticipant(dependencyGraphPersistenceManager);
        ResourcesPlugin.getWorkspace().addSaveParticipant(PLUGIN_ID, saveParticipant);
        getIpsModel().startListeningToResourceChanges();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        getIpsModel().stopListeningToResourceChanges();
        dependencyGraphPersistenceManager = null;
    }

    static EclipseIpsModel getIpsModel() {
        return (EclipseIpsModel)IIpsModel.get();
    }

    /**
     * Returns the persistence manager for the dependency graphs.
     */
    public static DependencyGraphPersistenceManager getDependencyGraphPersistenceManager() {
        return dependencyGraphPersistenceManager;
    }

}
