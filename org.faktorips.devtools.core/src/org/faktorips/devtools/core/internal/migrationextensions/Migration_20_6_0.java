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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.Severity;
import org.faktorips.util.message.MessageList;

public class Migration_20_6_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "20.6.0"; //$NON-NLS-1$
    private static final Set<IpsObjectType> TYPES_TO_MIGRATE = ImmutableSet.of(IpsObjectType.POLICY_CMPT_TYPE,
            IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.TABLE_CONTENTS, IpsObjectType.PRODUCT_CMPT,
            IpsObjectType.PRODUCT_TEMPLATE, IpsObjectType.TEST_CASE_TYPE, IpsObjectType.TEST_CASE);

    public Migration_20_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId, TYPES_TO_MIGRATE, VERSION,
                Messages.Migration_20_6_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        IIpsProjectProperties properties = getIpsProject().getProperties();
        properties.setDuplicateProductComponentSeverity(Severity.WARNING);
        getIpsProject().setProperties(properties);
        return super.migrate(monitor);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_20_6_0(ipsProject, featureId);
        }
    }
}
