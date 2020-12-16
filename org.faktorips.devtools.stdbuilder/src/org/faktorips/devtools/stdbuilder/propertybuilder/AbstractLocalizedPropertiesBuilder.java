/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.propertybuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.labels.LabelAndDescriptionPropertiesBuilder;
import org.faktorips.util.LocalizedStringsSet;

public abstract class AbstractLocalizedPropertiesBuilder extends AbstractArtefactBuilder {

    public static final String MESSAGES_EXTENSION = "properties";

    private final Map<IFile, AbstractPropertiesGenerator> propertiesGeneratorMap = new HashMap<IFile, AbstractPropertiesGenerator>();

    public AbstractLocalizedPropertiesBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet);
    }

    public AbstractLocalizedPropertiesBuilder(IIpsArtefactBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
    }

    protected abstract AbstractPropertiesGenerator createNewMessageGenerator(IFile propertyFile,
            ISupportedLanguage supportedLanguage);

    protected abstract String getResourceBundleBaseName(IIpsSrcFolderEntry entry);

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    /**
     * {@inheritDoc}
     * 
     * The {@link AbstractLocalizedPropertiesBuilder} has to check the modification state for every
     * property file. In case of {@link IncrementalProjectBuilder#FULL_BUILD} we clear every
     * property file and try to read the existing file.
     * <p>
     * This method also creates the folder of the property file if it does not exists yet. We need
     * to do this before building because otherwise the PDE-Builder may mark the folder as not
     * existing in the MANIFEST.MF.
     * 
     */
    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        for (IIpsPackageFragmentRoot srcRoot : project.getSourceIpsPackageFragmentRoots()) {
            for (ISupportedLanguage supportedLanguage : project.getReadOnlyProperties().getSupportedLanguages()) {
                if (buildKind == IncrementalProjectBuilder.FULL_BUILD) {
                    getMessagesGenerator(srcRoot, supportedLanguage).loadMessages();
                }
                IFile propertyFile = getPropertyFile(srcRoot, supportedLanguage);
                if (propertyFile == null) {
                    continue;
                }
                createFolderIfNotThere((IFolder)propertyFile.getParent());
            }
        }
    }

    /**
     * If the property file was modified, the {@link LabelAndDescriptionPropertiesBuilder} have to
     * save the new property file.
     * 
     * {@inheritDoc}
     */
    @Override
    public void afterBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
        super.afterBuildProcess(ipsProject, buildKind);
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot srcRoot : srcRoots) {
            for (ISupportedLanguage supportedLanguage : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
                AbstractPropertiesGenerator messagesGenerator = getMessagesGenerator(srcRoot, supportedLanguage);
                messagesGenerator.saveIfModified();
            }
        }
    }

    /* protected */public IFile getPropertyFile(IIpsPackageFragmentRoot root, ISupportedLanguage supportedLanguage) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        IFolder derivedFolder = entry.getOutputFolderForDerivedJavaFiles();
        String resourceBundleBaseName = getResourceBundleBaseName(entry)
                + getMessagesFileSuffix(supportedLanguage.getLocale());
        IPath path = QNameUtil.toPath(resourceBundleBaseName).addFileExtension(MESSAGES_EXTENSION);
        IFile messagesFile = derivedFolder.getFile(path);
        return messagesFile;
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
        IFile propertyFile = getPropertyFile(root, supportedLanguage);
        AbstractPropertiesGenerator messagesGenerator = propertiesGeneratorMap.get(propertyFile);
        if (messagesGenerator == null) {
            messagesGenerator = createNewMessageGenerator(propertyFile, supportedLanguage);
            propertiesGeneratorMap.put(propertyFile, messagesGenerator);
        }
        return messagesGenerator;
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