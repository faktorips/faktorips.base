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
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * Adds {@link IpsSrcFolderEntry#setUniqueQualifier(String)} if necessary and regenerate XML files
 * to decrease XML file size.
 */
public class Migration_3_20_0 extends DefaultMigration {

    private Migration_3_20_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getTargetVersion() {
        return "3.20.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_20_0_description;
    }

    @Override
    protected void migrate(IIpsSrcFile ipsSrcFile) throws CoreException {
        IpsObjectType ipsObjectType = ipsSrcFile.getIpsObjectType();
        if (IpsObjectType.ENUM_CONTENT.equals(ipsObjectType) || IpsObjectType.TABLE_CONTENTS.equals(ipsObjectType)
                || IpsObjectType.PRODUCT_CMPT.equals(ipsObjectType)) {
            ipsSrcFile.markAsDirty();
        }
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_20_0(ipsProject, featureId);
        }
    }
}
