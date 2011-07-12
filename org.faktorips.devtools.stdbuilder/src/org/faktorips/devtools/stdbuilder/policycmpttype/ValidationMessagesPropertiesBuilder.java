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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.util.LocalizedStringsSet;

public class ValidationMessagesPropertiesBuilder extends AbstractArtefactBuilder {

    private static final String MESSAGES_COMMENT = "MESSAGES_COMMENT";

    static final String MESSAGES_BASENAME = "messages";

    static final String MESSAGES_PREFIX = ".properties";

    // matching a text that follows '{' and is followed by '{' or ','
    public static final Pattern REPLACEMENT_PARAMETER_REGEXT = Pattern.compile("(?<=(\\{))[\\p{L}0-9]+(?=([,\\}]))");

    private final Map<IFile, ValidationMessages> messagesMap;

    /**
     * This constructor setting the specified builder set and initializes a new map as
     * {@link #messagesMap}
     * 
     * @param builderSet The builder set this builder belongs to
     */
    public ValidationMessagesPropertiesBuilder(IIpsArtefactBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(ValidationMessagesPropertiesBuilder.class));
        messagesMap = new HashMap<IFile, ValidationMessages>();
    }

    @Override
    public String getName() {
        return "ValidationMessagesPropertiesBuilder";
    }

    /**
     * {@inheritDoc}
     * 
     * The {@link ValidationMessagesPropertiesBuilder} have to check the modification state for every the
     * property files. In case of {@link IncrementalProjectBuilder#FULL_BUILD} we clear every
     * property file and try to read the existing file.
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
                getMessages(srcRoot).clear();
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
        ValidationMessages messages = getMessages(ipsSrcFile);
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
            List<IValidationRule> validationRules = policyCmptType.getValidationRules();
            for (IValidationRule validationRule : validationRules) {
                messages.put(getMessageKey(validationRule), getMessageText(validationRule));
            }
        }
    }

    /**
     * Getting the message text from {@link IValidationRule} and convert the replace parameters to
     * match java {@link MessageFormat}
     * 
     * @param validationRule The validationRule holding the message text
     * @return the text of validationRule with converted replacement parameters
     */
    protected String getMessageText(IValidationRule validationRule) {
        String messageText = validationRule.getMessageText();
        StringBuilder result = new StringBuilder();

        Matcher matcher = REPLACEMENT_PARAMETER_REGEXT.matcher(messageText);
        int lastEnd = 0;
        Map<String, Integer> parameterNameToIndex = new HashMap<String, Integer>();
        while (matcher.find()) {
            result.append(messageText.substring(lastEnd, matcher.start()));
            String parameterName = matcher.group();
            Integer argumentIndex = parameterNameToIndex.get(parameterName);
            if (argumentIndex == null) {
                argumentIndex = parameterNameToIndex.size();
                parameterNameToIndex.put(parameterName, argumentIndex);
            }
            result.append(argumentIndex);
            lastEnd = matcher.end();
        }
        result.append(messageText.substring(lastEnd));
        return result.toString();
    }

    /**
     * If the property file was modified, the {@link ValidationMessagesPropertiesBuilder} have to save the new
     * property file.
     * 
     * {@inheritDoc}
     */
    @Override
    public void afterBuildProcess(IIpsProject ipsProject, int buildKind) throws CoreException {
        super.afterBuildProcess(ipsProject, buildKind);
        IIpsPackageFragmentRoot[] srcRoots = ipsProject.getSourceIpsPackageFragmentRoots();
        for (IIpsPackageFragmentRoot srcRoot : srcRoots) {
            ValidationMessages messages = getMessages(srcRoot);
            if (messages.isModified()) {
                String comments = NLS.bind(getLocalizedText(ipsProject, MESSAGES_COMMENT), ipsProject.getName() + "/"
                        + srcRoot.getName());
                IFile propertyFile = getPropertyFile(srcRoot);
                storeMessagesToFile(propertyFile, messages, comments);
            }
        }
    }

    protected void storeMessagesToFile(IFile propertyFile, ValidationMessages messages, String comments)
            throws CoreException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        messages.store(outputStream, comments);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        createFileIfNotThere(propertyFile);
        propertyFile.setContents(inputStream, true, true, new NullProgressMonitor());
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

    protected ValidationMessages getMessages(IIpsSrcFile ipsSrcFile) {
        IIpsPackageFragmentRoot root = ipsSrcFile.getIpsPackageFragment().getRoot();
        return getMessages(root);
    }

    protected ValidationMessages getMessages(IIpsPackageFragmentRoot root) {
        IFile propertyFile = getPropertyFile(root);
        ValidationMessages messages = messagesMap.get(propertyFile);
        if (messages == null) {
            messages = new ValidationMessages();
            loadMessagesFromFile(propertyFile, messages);
        }
        return messages;
    }

    protected void loadMessagesFromFile(IFile propertyFile, ValidationMessages messages) {
        try {
            if (propertyFile.exists()) {
                messages.load(propertyFile.getContents());
            }
        } catch (CoreException e) {
            StdBuilderPlugin.log(e);
        }
        messagesMap.put(propertyFile, messages);
    }

    protected String getMessageKey(IValidationRule validationRule) {
        IIpsObject ipsObject = validationRule.getIpsObject();
        return ipsObject.getQualifiedName() + "_" + validationRule.getName();
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        ValidationMessages messages = getMessages(ipsSrcFile);
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsObject;
            List<IValidationRule> validationRules = policyCmptType.getValidationRules();
            for (IValidationRule validationRule : validationRules) {
                messages.remove(getMessageKey(validationRule));
            }
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
