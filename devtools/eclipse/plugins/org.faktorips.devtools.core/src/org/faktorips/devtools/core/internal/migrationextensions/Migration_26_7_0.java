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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

public class Migration_26_7_0 extends MarkAsDirtyMigration {

    private static final String VERSION = "26.7.0"; //$NON-NLS-1$
    private final Set<String> derrivedFilesToDelete = new HashSet<>();
    private final List<AFolder> outputFolderForDerivedFiles = new ArrayList<>();

    public Migration_26_7_0(IIpsProject projectToMigrate, String featureId) {
        this(projectToMigrate, featureId, VERSION);
    }

    /**
     * Creates a new migration operation for the given project.
     *
     * @param projectToMigrate The IPS project that should be migrated
     * @param featureId The ID of the feature this migration belongs to
     */
    Migration_26_7_0(IIpsProject projectToMigrate, String featureId, String migrationVersion) {
        super(projectToMigrate,
                featureId,
                Set.of(IIpsModel.get().getIpsObjectTypes()),
                migrationVersion,
                Messages.Migration_26_7_0_description);
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws IpsException, InvocationTargetException {
        updateManifest();
        IIpsProjectProperties readOnlyProperties = getIpsProject().getReadOnlyProperties();
        IIpsObjectPath ipsObjectPath = readOnlyProperties.getIpsObjectPath();
        Set<ISupportedLanguage> languages = readOnlyProperties.getSupportedLanguages();

        Stream.of(ipsObjectPath.getEntries())
                .filter(IIpsSrcFolderEntry.class::isInstance)
                .map(IIpsSrcFolderEntry.class::cast)
                .forEach(src -> {
                    // file name of validation messages per IpsObjectPathEntry
                    generateFileNamesForLanguages(languages, src.getValidationMessagesBundle());

                    var rootFolderName = getIpsPackageFragmentRootFolderName(src);
                    // file name of model and description
                    generateFileNamesForLanguages(languages, rootFolderName);

                    var folder = src.getOutputFolderForDerivedJavaFiles();
                    if (folder != null && folder.exists()) {
                        outputFolderForDerivedFiles.add(folder);
                    }
                });

        // file name of enum content and type
        generateFileNamesForEnums(languages, IpsObjectType.ENUM_CONTENT);
        generateFileNamesForEnums(languages, IpsObjectType.ENUM_TYPE);

        var folder = ipsObjectPath.getOutputFolderForDerivedSources();
        if (folder != null && folder.exists()) {
            outputFolderForDerivedFiles.add(folder);
        }

        try {
            for (AFolder derivedFolder : outputFolderForDerivedFiles) {
                processContainer(derivedFolder.unwrap());
            }
        } catch (CoreException e) {
            throw new IpsException("Error in migration, failed to delete derived properties file: ", e); //$NON-NLS-1$
        }
        return super.migrate(monitor);
    }

    private String getIpsPackageFragmentRootFolderName(IIpsSrcFolderEntry entry) {
        String packageFragmentRootName = entry.getIpsPackageFragmentRootName().replace("\\", ".").replace("/", "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if (packageFragmentRootName.indexOf(".") > 0) { //$NON-NLS-1$
            packageFragmentRootName = packageFragmentRootName.substring(packageFragmentRootName.lastIndexOf(".") + 1, //$NON-NLS-1$
                    packageFragmentRootName.length());
        }
        return packageFragmentRootName + "-label-and-descriptions"; //$NON-NLS-1$
    }

    private void generateFileNamesForEnums(Set<ISupportedLanguage> languages, IpsObjectType ipsObjectType) {
        getIpsProject().findAllIpsSrcFiles(ipsObjectType).stream().forEach(content -> {
            var fileName = content.getName().replace("." + ipsObjectType.getFileExtension(), IpsStringUtils.EMPTY); //$NON-NLS-1$
            generateFileNamesForLanguages(languages, fileName);
        });
    }

    private void generateFileNamesForLanguages(Set<ISupportedLanguage> languages, String fileName) {
        languages.stream().forEach(language -> {
            derrivedFilesToDelete.add(fileName + "_" + language.getLocale().getLanguage() + ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
        });
    }

    private void processContainer(IContainer container) throws CoreException {
        IResource[] members = container.members();
        for (IResource member : members) {
            if (member instanceof IContainer) {
                processContainer((IContainer)member);
            } else if (member instanceof IFile) {
                processFile((IFile)member);
            }
        }
    }

    private void processFile(IFile member) throws CoreException {
        if (derrivedFilesToDelete.contains(member.getName())) {
            member.delete(true, new NullProgressMonitor());
        }
    }

    /**
     * Factory for creating instances of this migration operation.
     */
    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_26_7_0(ipsProject, featureId);
        }
    }
}
