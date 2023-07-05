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
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.ipsproject.IpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.MessageList;

public class Migration_23_12_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "23.12.0"; //$NON-NLS-1$

    public Migration_23_12_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                Set.of(IpsObjectType.PRODUCT_CMPT, IpsObjectType.ENUM_CONTENT, IpsObjectType.TABLE_CONTENTS),
                VERSION,
                Messages.Migration_23_12_0_description);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        IIpsObjectPath ipsObjectPath = getIpsProject().getIpsObjectPath();
        AFolder derivedFolder = ipsObjectPath.getOutputFolderForDerivedSources();
        deleteDerivedFolder(monitor, derivedFolder);

        for (IIpsObjectPathEntry entry : ipsObjectPath.getEntries()) {
            if (entry instanceof IpsSrcFolderEntry srcFolderEntry) {
                derivedFolder = srcFolderEntry.getOutputFolderForDerivedJavaFiles();
                deleteDerivedFolder(monitor, derivedFolder);
            }
        }

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

    private void deleteDerivedFolder(IProgressMonitor monitor, AFolder derivedFolder) {
        SortedSet<? extends AResource> members = derivedFolder.getMembers();
        for (AResource member : members) {
            if (member.getType() == AResourceType.FOLDER) {
                deleteDerivedFolder(monitor, (AFolder)member);
            }
            if (isXml(member)) {
                member.delete(monitor);
            }
        }
    }

    private boolean isXml(AResource member) {
        String fileName = member.getName();
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot < 0) {
            return false;
        }
        return ".xml".equalsIgnoreCase(fileName.substring(lastDot));
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_23_12_0(ipsProject, featureId);
        }
    }
}
