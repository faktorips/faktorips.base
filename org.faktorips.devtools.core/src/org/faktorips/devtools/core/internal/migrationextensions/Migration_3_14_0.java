/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * An empty migration to version 3.14.0. No source files changes with this migration.
 */

public class Migration_3_14_0 extends DefaultMigration {

    public Migration_3_14_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public final String getDescription() {
        return Messages.Migration_3_14_0_description;
    }

    @Override
    public String getTargetVersion() {
        return "3.14.0"; //$NON-NLS-1$
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        // do nothing

    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_14_0(ipsProject, featureId);
        }
    }

}
