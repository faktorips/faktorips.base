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

package org.faktorips.devtools.core.internal.migrationextensions;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;

public class Migration_3_8_0 extends DefaultMigration {

    public Migration_3_8_0(IIpsProject ipsProject, String featureId) {
        super(ipsProject, featureId);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        // do nothing
    }

    @Override
    public String getTargetVersion() {
        return "3.8.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_8_0_description;
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_8_0(ipsProject, featureId);
        }

    }

}
