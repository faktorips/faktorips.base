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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsSrcFolderEntry;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.util.message.MessageList;

/**
 * Adds {@link IpsSrcFolderEntry#setUniqueQualifier(String)} if necessary.
 */
public class Migration_3_18_0 extends DefaultMigration {

    private Migration_3_18_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        srcFile.markAsDirty();
    }

    @Override
    public String getTargetVersion() {
        return "3.18.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_18_0_description;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException {
        MessageList messageList = super.migrate(monitor);
        IIpsProject ipsProject = getIpsProject();
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
        setUniqueQualifiersWhereNecessary(ipsProjectProperties);
        removeDiscontinuedProperties(ipsProjectProperties);
        ipsProject.setProperties(ipsProjectProperties);
        return messageList;
    }

    private void removeDiscontinuedProperties(IIpsProjectProperties ipsProjectProperties) {
        IIpsArtefactBuilderSetConfigModel builderSetConfig = ipsProjectProperties.getBuilderSetConfig();
        if (builderSetConfig instanceof IpsArtefactBuilderSetConfigModel) {
            ((IpsArtefactBuilderSetConfigModel)builderSetConfig).removeProperty("useJavaEnumTypes"); //$NON-NLS-1$
            ((IpsArtefactBuilderSetConfigModel)builderSetConfig).removeProperty("useTypesafeCollections"); //$NON-NLS-1$
        }
    }

    private void setUniqueQualifiersWhereNecessary(IIpsProjectProperties ipsProjectProperties) {
        IIpsObjectPath ipsObjectPath = ipsProjectProperties.getIpsObjectPath();
        IIpsSrcFolderEntry[] sourceFolderEntries = ipsObjectPath.getSourceFolderEntries();
        if (sourceFolderEntries.length > 1) {
            for (int i = 1; i < sourceFolderEntries.length; i++) {
                IpsSrcFolderEntry ipsSrcFolderEntry = (IpsSrcFolderEntry)sourceFolderEntries[i];
                ipsSrcFolderEntry.setUniqueQualifier(ipsSrcFolderEntry.getIpsPackageFragmentRootName());
            }
        }
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOperation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_18_0(ipsProject, featureId);
        }
    }
}
