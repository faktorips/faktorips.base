/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.eclipse.internal.EclipseIpsModelActivator;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.MessageList;

public class Migration_22_12_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "22.12.0"; //$NON-NLS-1$

    public Migration_22_12_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                new LinkedHashSet<>(Arrays.asList(IIpsModel.get().getIpsObjectTypes())),
                VERSION,
                Messages.Migration_22_12_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        updatePluginId(".project", monitor); //$NON-NLS-1$
        updatePluginId(".classpath", monitor); //$NON-NLS-1$
        reloadProjectData();
        return super.migrate(monitor);
    }

    private void reloadProjectData() {
        @SuppressWarnings("deprecation")
        IpsModel ipsModel = IpsModel.get();
        ipsModel.clearProjectSpecificCaches(getIpsProject());
    }

    private void updatePluginId(String fileName, IProgressMonitor monitor) {
        IFile file = getIpsProject().getProject().getFile(fileName).unwrap();
        Migration_21_6_0.update(file, c -> c.replace(IpsModelActivator.PLUGIN_ID, EclipseIpsModelActivator.PLUGIN_ID),
                monitor);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_22_12_0(ipsProject, featureId);
        }
    }
}
