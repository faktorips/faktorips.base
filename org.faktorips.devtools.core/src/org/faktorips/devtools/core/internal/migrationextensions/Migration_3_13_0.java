/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * Migration to version 3.13.0 changes all table contents xml files. It renames the tag "Generation"
 * to "Rows"</li>
 */
public class Migration_3_13_0 extends DefaultMigration {

    public Migration_3_13_0(IIpsProject ipsProject, String featureId) {
        super(ipsProject, featureId);
    }

    @Override
    public String getTargetVersion() {
        return "3.13.0"; //$NON-NLS-1$
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (IpsObjectType.TABLE_CONTENTS.equals(srcFile.getIpsObjectType())) {
            srcFile.markAsDirty();
        }
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_13_0_description;
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_13_0(ipsProject, featureId);
        }
    }
}
