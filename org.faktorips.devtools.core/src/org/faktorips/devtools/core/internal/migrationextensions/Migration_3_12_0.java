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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.util.message.MessageList;

/**
 * Migration to version 3.12.0 Changes the .ipsproject settings file as follows:
 * <ul>
 * <li>renames the tag "productRelease" to "ProductRelease"</li>
 * <li>introduces a new tag "Version". The "productRelease"-attribute "version" is moved to the new
 * tag "Version"</li>
 * </ul>
 */
public class Migration_3_12_0 extends AbstractIpsProjectMigrationOperation {

    public Migration_3_12_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_12_0_description;
    }

    @Override
    public String getTargetVersion() {
        return "3.12.0"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Simply reads and writes the properties. The properties are designed to read the old XML but
     * only write the new correct form.
     */
    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        IIpsProjectProperties properties = getIpsProject().getProperties();
        getIpsProject().setProperties(properties);
        return new MessageList();
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_12_0(ipsProject, featureId);
        }
    }
}
