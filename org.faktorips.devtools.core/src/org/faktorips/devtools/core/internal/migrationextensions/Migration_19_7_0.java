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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.util.message.MessageList;

/**
 * Marks classes generated for {@link IPolicyCmptType}, {@link IProductCmptType} and
 * {@link ITableStructure} as dirty to trigger the clean build.
 */
public class Migration_19_7_0 extends DefaultMigration {

    private static final Set<IpsObjectType> TYPES_TO_MIGRATE = ImmutableSet.of(IpsObjectType.POLICY_CMPT_TYPE,
            IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.TABLE_STRUCTURE);

    private Migration_19_7_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getTargetVersion() {
        return "19.7.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return Messages.Migration_19_7_0;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        MessageList messageList = super.migrate(monitor);
        MigrationUtil.updateBuilderSetDefaults(getIpsProject());
        return messageList;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (TYPES_TO_MIGRATE.contains(srcFile.getIpsObjectType())) {
            srcFile.markAsDirty();
        }
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_19_7_0(ipsProject, featureId);
        }
    }
}
