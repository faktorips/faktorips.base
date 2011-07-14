/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.util.LocalizedStringsSet;

public class ValidationRuleMessagesPropertiesBuilder extends AbstractArtefactBuilder {

    private static final String MESSAGES_COMMENT = "MESSAGES_COMMENT";

    static final String MESSAGES_BASENAME = "messages";

    static final String MESSAGES_PREFIX = ".properties";

    private final Map<IFile, ValidationRuleMessagesGenerator> messageGeneratorMap;

    /**
     * This constructor setting the specified builder set and initializes a new map as
     * {@link #messageGeneratorMap}
     * 
     * @param builderSet The builder set this builder belongs to
     */
    public ValidationRuleMessagesPropertiesBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(ValidationRuleMessagesPropertiesBuilder.class));
        messageGeneratorMap = new HashMap<IFile, ValidationRuleMessagesGenerator>();
    }

    @Override
    public String getName() {
        return "ValidationRuleMessagesPropertiesBuilder";
    }

    /**
     * {@inheritDoc}
     * 
     * The {@link ValidationRuleMessagesPropertiesBuilder} have to check the modification state for
     * every the property files. In case of {@link IncrementalProjectBuilder#FULL_BUILD} we clear
     * every property file and try to read the existing file.
     * <p>
     * This method also creates the folder of the property file if it does not exists yet. We need
     * to do this before building because after otherwise the PDE-Builder may mark the folder as not
     * existing in the MANIFEST.MF.
     * 
     */
    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        for (IIpsPackageFragmentRoot srcRoot : project.getSourceIpsPackageFragmentRoots()) {
            if (buildKind == IncrementalProjectBuilder.FULL_BUILD) {
                getMessagesGenerator(srcRoot).loadMessages();
            }
            IFile propertyFile = getPropertyFile(srcRoot);
            if (propertyFile == null) {
                continue;
            }
            createFolderIfNotThere((IFolder)propertyFile.getParent());
        }
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        ValidationRuleMessagesGenerator messagesGenerator = getMessagesGenerator(ipsSrcFile);
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            messagesGenerator.generate((IPolicyCmptType)ipsObject);
        }
    }

    /**
     * If the property file was modified, the {@link ValidationRuleMessagesPropertiesBuilder} have
     * to save the new property file.
     * 
     * {@inheritDoc}
     */
    @Override
    public void afterBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
        super.afterBuildProcess(ipsProject, buildKind);
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot srcRoot : srcRoots) {
            String comment = NLS.bind(getLocalizedText(ipsProject, MESSAGES_COMMENT), ipsProject.getName() + "/"
                    + srcRoot.getName());
            ValidationRuleMessagesGenerator messagesGenerator = getMessagesGenerator(srcRoot);
            messagesGenerator.saveIfModified(comment, buildsDerivedArtefacts()
                    && getBuilderSet().isMarkNoneMergableResourcesAsDerived());
        }
    }

    protected IFile getPropertyFile(IIpsPackageFragmentRoot root) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        IFolder derivedFolder = entry.getOutputFolderForDerivedJavaFiles();

        IPath path = QNameUtil.toPath(getResourceBundlePackage(entry)).append(getMessagesFileName());
        IFile messagesFile = derivedFolder.getFile(path);
        return messagesFile;
    }

    protected String getMessagesFileName() {
        return MESSAGES_BASENAME + MESSAGES_PREFIX;
    }

    protected String getResourceBundleBaseName(IIpsSrcFolderEntry entry) {
        String baseName = getResourceBundlePackage(entry) + "." + MESSAGES_BASENAME;
        return baseName;
    }

    protected String getResourceBundlePackage(IIpsSrcFolderEntry entry) {
        String basePack = entry.getBasePackageNameForDerivedJavaClasses();
        return getBuilderSet().getInternalPackage(basePack, StringUtils.EMPTY);
    }

    protected ValidationRuleMessagesGenerator getMessagesGenerator(IIpsSrcFile ipsSrcFile) {
        IIpsPackageFragmentRoot root = ipsSrcFile.getIpsPackageFragment().getRoot();
        return getMessagesGenerator(root);
    }

    protected ValidationRuleMessagesGenerator getMessagesGenerator(IIpsPackageFragmentRoot root) {
        IFile propertyFile = getPropertyFile(root);
        ValidationRuleMessagesGenerator messagesGenerator = messageGeneratorMap.get(propertyFile);
        if (messagesGenerator == null) {
            messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile);
            messageGeneratorMap.put(propertyFile, messagesGenerator);
        }
        return messagesGenerator;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        ValidationRuleMessagesGenerator messagesGenerator = getMessagesGenerator(ipsSrcFile.getIpsPackageFragment()
                .getRoot());
        QualifiedNameType qualifiedNameType = ipsSrcFile.getQualifiedNameType();
        if (qualifiedNameType.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE) {
            messagesGenerator.deleteAllMessagesFor(qualifiedNameType.getName());
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

}
