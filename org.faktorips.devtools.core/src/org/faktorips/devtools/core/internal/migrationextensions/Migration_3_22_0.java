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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.internal.AbstractJaxbModelObject;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.util.message.MessageList;

/**
 * Marks classes generated for {@link IPolicyCmptType policy component types} as dirty to trigger
 * the base class change from {@link AbstractModelObject} to {@link AbstractJaxbModelObject} if JAXB
 * support is enabled.
 */
public class Migration_3_22_0 extends DefaultMigration {

    private Migration_3_22_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getTargetVersion() {
        return "3.22.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_22_0_description;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        MessageList messageList = super.migrate(monitor);
        MigrationUtil.updateBuilderSetDefaults(getIpsProject());
        return messageList;
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        // nothing to do
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_22_0(ipsProject, featureId);
        }
    }
}
