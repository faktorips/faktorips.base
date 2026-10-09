/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.propertybuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.builder.labels.LabelAndDescriptionPropertiesBuilder;
import org.faktorips.devtools.model.builder.plugin.IpsBuilderPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.runtime.internal.IpsStringUtils;

public abstract class AbstractLocalizedPropertiesBuilder extends AbstractArtefactBuilder {

    public static final String MESSAGES_EXTENSION = "properties";

    private final Map<AFile, AbstractPropertiesGenerator> propertiesGeneratorMap = new HashMap<>();

    public AbstractLocalizedPropertiesBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet);
    }

    public AbstractLocalizedPropertiesBuilder(IIpsArtefactBuilderSet builderSet,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
    }

    protected abstract AbstractPropertiesGenerator createNewMessageGenerator(AFile propertyFile,
            ISupportedLanguage supportedLanguage);

    protected abstract String getResourceBundleBaseName(IIpsSrcFolderEntry entry);

    @Override
    public JavaBuilderSet getBuilderSet() {
        return (JavaBuilderSet)super.getBuilderSet();
    }

    /**
     * {@inheritDoc}
     *
     * The {@link AbstractLocalizedPropertiesBuilder} has to check the modification state for every
     * property file. In case of {@link IncrementalProjectBuilder#FULL_BUILD} we clear every
     * property file and try to read the existing file.
     * <p>
     * This method also creates the folder of the property file if it does not exist yet. We need to
     * do this before building because otherwise the PDE-Builder may mark the folder as not existing
     * in the MANIFEST.MF.
     *
     */
    @Override
    public void beforeBuildProcess(IIpsProject project, ABuildKind buildKind) {
        super.beforeBuildProcess(project, buildKind);
        for (IIpsPackageFragmentRoot srcRoot : project.getSourceIpsPackageFragmentRoots()) {
            for (ISupportedLanguage supportedLanguage : project.getReadOnlyProperties().getSupportedLanguages()) {
                AFile propertyFile = getPropertyFile(srcRoot, supportedLanguage);
                loadMessagesOrRecover(srcRoot, supportedLanguage, propertyFile, buildKind);
                createFolderIfNotThere((AFolder)propertyFile.getParent());
            }
        }
    }

    private void loadMessagesOrRecover(IIpsPackageFragmentRoot srcRoot,
            ISupportedLanguage supportedLanguage,
            AFile propertyFile,
            ABuildKind buildKind) {
        try {
            AbstractPropertiesGenerator generator = getMessagesGenerator(srcRoot, supportedLanguage);
            if (buildKind == ABuildKind.FULL) {
                generator.loadMessages();
            }
            // CSOFF: Illegal Catch
        } catch (RuntimeException e) {
            // CSON: Illegal Catch
            logAndDeleteCorruptedFile(propertyFile, e);
            propertiesGeneratorMap.remove(propertyFile);
            getMessagesGenerator(srcRoot, supportedLanguage);
        }
    }

    private void logAndDeleteCorruptedFile(AFile propertyFile, RuntimeException cause) {
        String content = readContentsQuietly(propertyFile);
        IpsLog.get().log(new Status(IStatus.WARNING, IpsBuilderPlugin.PLUGIN_ID,
                "Could not read generated file " + propertyFile + ", it will be deleted and regenerated. Content:\n" //$NON-NLS-1$ //$NON-NLS-2$
                        + content,
                cause));
        try {
            if (propertyFile.exists()) {
                propertyFile.delete(null);
            }
        } catch (IpsException e) {
            IpsLog.get().log(new Status(IStatus.ERROR, IpsBuilderPlugin.PLUGIN_ID,
                    "Could not delete corrupted generated file " + propertyFile, e));
        }
    }

    private String readContentsQuietly(AFile propertyFile) {
        try (InputStream contents = propertyFile.getContents()) {
            return new String(contents.readAllBytes(), StandardCharsets.ISO_8859_1);
            // CSOFF: Illegal Catch
        } catch (IOException | RuntimeException e) {
            // CSON: Illegal Catch
            return "<content could not be read: " + e.getMessage() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * If the property file was modified, the {@link LabelAndDescriptionPropertiesBuilder} have to
     * save the new property file.
     *
     * {@inheritDoc}
     */
    @Override
    public void afterBuildProcess(IIpsProject ipsProject, ABuildKind buildKind) {
        super.afterBuildProcess(ipsProject, buildKind);
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot srcRoot : srcRoots) {
            for (ISupportedLanguage supportedLanguage : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
                AbstractPropertiesGenerator messagesGenerator = getMessagesGenerator(srcRoot, supportedLanguage);
                messagesGenerator.saveIfModified();
            }
        }
    }

    /* protected */public AFile getPropertyFile(IIpsPackageFragmentRoot root, ISupportedLanguage supportedLanguage) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        AFolder derivedFolder = entry.getOutputFolderForDerivedJavaFiles();
        String resourceBundleBaseName = getResourceBundleBaseName(entry)
                + getMessagesFileSuffix(supportedLanguage.getLocale());
        Path path = QNameUtil.toPath(resourceBundleBaseName);
        if (path == null) {
            throw new IpsException("Can't find resource bundle for messages of " + entry);
        }
        Path file = path.getFileName();
        String fileName = file == null ? IpsStringUtils.EMPTY : file.toString();
        path = path.resolveSibling(fileName + '.' + MESSAGES_EXTENSION);
        return derivedFolder.getFile(path);
    }

    private String getMessagesFileSuffix(Locale locale) {
        return "_" + locale.toString();
    }

    protected AbstractPropertiesGenerator getMessagesGenerator(IIpsSrcFile ipsSrcFile,
            ISupportedLanguage supportedLanguage) {
        IIpsPackageFragmentRoot root = ipsSrcFile.getIpsPackageFragment().getRoot();
        return getMessagesGenerator(root, supportedLanguage);
    }

    /* protected */public AbstractPropertiesGenerator getMessagesGenerator(IIpsPackageFragmentRoot root,
            ISupportedLanguage supportedLanguage) {
        AFile propertyFile = getPropertyFile(root, supportedLanguage);
        return propertiesGeneratorMap.computeIfAbsent(propertyFile,
                f -> createNewMessageGenerator(f, supportedLanguage));
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) {
        for (ISupportedLanguage supportedLanguage : ipsSrcFile.getIpsProject().getReadOnlyProperties()
                .getSupportedLanguages()) {
            AbstractPropertiesGenerator messagesGenerator = getMessagesGenerator(ipsSrcFile.getIpsPackageFragment()
                    .getRoot(), supportedLanguage);
            QualifiedNameType qualifiedNameType = ipsSrcFile.getQualifiedNameType();
            messagesGenerator.deleteAllMessagesFor(qualifiedNameType);
        }
    }

    /**
     * The property-files built by this builder are 100% generated files.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return getBuilderSet().isGeneratePublishedInterfaces();
    }

}
