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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
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

public class Migration_24_1_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "24.1.0"; //$NON-NLS-1$
    private static final Set<String> ALLOWED_XML_CONTENT = Set.of(
            "<" + IpsObjectType.TABLE_CONTENTS.getXmlElementName(),
            "<" + IpsObjectType.ENUM_CONTENT.getXmlElementName(),
            "<" + IpsObjectType.PRODUCT_CMPT.getXmlElementName());

    public Migration_24_1_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                Set.of(IpsObjectType.PRODUCT_CMPT, IpsObjectType.ENUM_CONTENT, IpsObjectType.TABLE_CONTENTS, IpsObjectType.POLICY_CMPT_TYPE, IpsObjectType.PRODUCT_CMPT_TYPE),
                VERSION,
                Messages.Migration_24_1_0_description);
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
            if (isFipsXml(member)) {
                member.delete(monitor);
            }
        }
    }

    private boolean isFipsXml(AResource member) {
        String fileName = member.getName();
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot < 0 || !".xml".equalsIgnoreCase(fileName.substring(lastDot))) {
            return false;
        }
        String line = readSecondLine(member.getLocation());
        return ALLOWED_XML_CONTENT.stream().anyMatch(s -> line.startsWith(s));
    }

    private String readSecondLine(Path fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName.toFile()));) {
            // ignore first line
            br.readLine();
            String secondLine = br.readLine();
            return secondLine == null ? "" : secondLine;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_24_1_0(ipsProject, featureId);
        }
    }
}
