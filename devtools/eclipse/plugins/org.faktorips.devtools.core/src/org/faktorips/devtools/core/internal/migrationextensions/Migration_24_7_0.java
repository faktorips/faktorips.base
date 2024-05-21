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
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.MessageList;

public class Migration_24_7_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "24.7.0"; //$NON-NLS-1$

    public Migration_24_7_0(IIpsProject projectToMigrate, String featureId) {
        this(projectToMigrate, featureId, VERSION);
    }

    Migration_24_7_0(IIpsProject projectToMigrate, String featureId, String migrationVersion) {
        super(projectToMigrate,
                featureId,
                Set.of(IpsObjectType.PRODUCT_CMPT, IpsObjectType.ENUM_CONTENT, IpsObjectType.TABLE_CONTENTS,
                        IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE),
                migrationVersion,
                Messages.Migration_24_7_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        return super.migrate(monitor);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) {
        IIpsObject ipsObject = srcFile.getIpsObject();
        if (ipsObject instanceof IProductCmpt productCmpt) {
            productCmpt.fixAllDifferencesToModel(srcFile.getIpsProject());
        }
        srcFile.save(null);
        super.migrate(srcFile);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_24_7_0(ipsProject, featureId);
        }
    }
}
