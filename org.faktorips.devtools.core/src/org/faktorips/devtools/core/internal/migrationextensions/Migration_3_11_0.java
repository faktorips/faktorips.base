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
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;

public class Migration_3_11_0 extends DefaultMigration {

    public Migration_3_11_0(IIpsProject ipsProject, String featureId) {
        super(ipsProject, featureId);
    }

    @Override
    public String getTargetVersion() {
        return "3.11.0"; //$NON-NLS-1$
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (IpsObjectType.TABLE_STRUCTURE.equals(srcFile.getIpsObjectType())
                || IpsObjectType.ENUM_TYPE.equals(srcFile.getIpsObjectType())) {
            srcFile.markAsDirty();
        }
        if (IpsObjectType.ENUM_TYPE.equals(srcFile.getIpsObjectType())) {
            addEnumLiteralName(srcFile);
        }
    }

    private void addEnumLiteralName(IIpsSrcFile srcFile) throws CoreException {
        IEnumType enumType = ((IEnumType)srcFile.getIpsObject());
        if (!enumType.isAbstract() && !enumType.containsEnumLiteralNameAttribute()) {
            enumType.newEnumLiteralNameAttribute();
        }
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_11_0_description;
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_11_0(ipsProject, featureId);
        }

    }

}
