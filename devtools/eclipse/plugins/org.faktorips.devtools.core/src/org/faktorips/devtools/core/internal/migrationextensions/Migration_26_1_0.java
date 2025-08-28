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
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.MessageList;

public class Migration_26_1_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "26.1.0"; //$NON-NLS-1$

    public Migration_26_1_0(IIpsProject projectToMigrate, String featureId) {
        this(projectToMigrate, featureId, VERSION);
    }

    /**
     * Creates a new migration operation for the given project.
     *
     * @param projectToMigrate The IPS project that should be migrated
     * @param featureId The ID of the feature this migration belongs to
     */
    Migration_26_1_0(IIpsProject projectToMigrate, String featureId, String migrationVersion) {
        super(projectToMigrate,
                featureId,
                Set.of(IIpsModel.get().getIpsObjectTypes()),
                migrationVersion,
                Messages.Migration_26_1_0_description);
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        updateManifest();
        return super.migrate(monitor);
    }

    /**
     * Factory for creating instances of this migration operation.
     */
    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_26_1_0(ipsProject, featureId);
        }
    }
}
