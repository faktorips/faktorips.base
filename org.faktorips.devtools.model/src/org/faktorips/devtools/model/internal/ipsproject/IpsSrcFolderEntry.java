/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsSrcFolderEntry.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntry extends IpsObjectPathEntry implements IIpsSrcFolderEntry {

    public static final String DEFAULT_TOC_PATH = ClassloaderRuntimeRepository.TABLE_OF_CONTENTS_FILE;

    public static final String DEFAUTL_VALIDATION_MESSAGES_BUNDLE = "validation-messages"; //$NON-NLS-1$

    private static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$

    private static final String PROPERTY_SOURCE_FOLDER = "sourceFolder"; //$NON-NLS-1$

    private static final String PROPERTY_OUTPUT_FOLDER_MERGABLE = "outputFolderMergable"; //$NON-NLS-1$

    private static final String PROPERTY_BASE_PACKAGE_MERGABLE = "basePackageMergable"; //$NON-NLS-1$

    private static final String PROPERTY_BASE_PACKAGE_DERIVED = "basePackageDerived"; //$NON-NLS-1$

    private static final String PROPERTY_OUTPUT_FOLDER_DERIVED = "outputFolderDerived"; //$NON-NLS-1$

    private static final String PROPERTY_TOC_PATH = "tocPath"; //$NON-NLS-1$

    private static final String PROPERTY_VALIDATION_MESSAGES_BUNDLE = "validationMessagesBundle"; //$NON-NLS-1$

    private static final String PROPERTY_UNIQUE_QUALIFIER = "uniqueQualifier"; //$NON-NLS-1$

    /** the folder containing the IPS objects */
    private AFolder sourceFolder;

    /** the output folder containing the generated but mergeable Java files. */
    private AFolder outputFolderMergable;

    /** the name of the base package containing the generated but mergeable Java files. */
    private String basePackageMergable = StringUtils.EMPTY;

    private String tocPath = DEFAULT_TOC_PATH;

    private String validationMessagesBundle = DEFAUTL_VALIDATION_MESSAGES_BUNDLE;

    /** the output folder containing the Java files that are generated and derived. */
    private AFolder outputFolderDerived;

    /**
     * the name of the base package containing the Java files where the developer adds it's own
     * code.
     */
    private String basePackageDerived = StringUtils.EMPTY;

    /**
     * The qualifier may be specified if the base package is not unique over all dependent projects
     */
    private String uniqueQualifier = StringUtils.EMPTY;

    private IIpsPackageFragmentRoot root;

    public IpsSrcFolderEntry(IpsObjectPath path) {
        super(path);
    }

    public IpsSrcFolderEntry(IpsObjectPath path, AFolder sourceFolder) {
        super(path);
        ArgumentCheck.notNull(sourceFolder);
        setSourceFolder(sourceFolder);
    }

    /**
     * Returns a description of the xml format.
     */
    public static final String getXmlFormatDescription() {
        return "Sourcefolder:" + System.lineSeparator() //$NON-NLS-1$
                + "  <" + XML_ELEMENT + System.lineSeparator() //$NON-NLS-1$
                + "    type=\"src\"" + System.lineSeparator() //$NON-NLS-1$
                + "    sourceFolder=\"model\"            Folder in the project that contains the Faktor-IPS model and product definition files." //$NON-NLS-1$
                + System.lineSeparator()
                + "    outputFolderMergable=\"src\"      Folder in the project where the generator puts the java source files which content will be merged with " //$NON-NLS-1$
                + "the newly generated content during each build cycle." //$NON-NLS-1$
                + System.lineSeparator()
                + "    basePackageMergable=\"org.foo\"   The package prefix for all generated but mergable java files." //$NON-NLS-1$
                + System.lineSeparator()
                + "    tocPath=\"motor/motor-reposiory-toc.xml\" " + System.lineSeparator() //$NON-NLS-1$
                + "                                      The partial path of the resource containing the runtime repository table of content (toc)." //$NON-NLS-1$
                + System.lineSeparator()
                + "                                      The full path is derived from the basePackageMergeable by adding this partial path." //$NON-NLS-1$
                + System.lineSeparator()
                + "                                      The file is not part of the published interface so it is places in the internal package." //$NON-NLS-1$
                + System.lineSeparator()
                + "    validationMessagesBundle=\"motor.validation-messages\" " + System.lineSeparator() //$NON-NLS-1$
                + "                                      The partial name of the resource bundle containing the validation messages." //$NON-NLS-1$
                + System.lineSeparator()
                + "                                      The full resource bundle name is derived from basePackageDerived adding this parial name." //$NON-NLS-1$
                + System.lineSeparator()
                + "                                      For getting the name of the resulting property file, the resource bundle algorithm adds the locale and '.properties' to the bundle name." //$NON-NLS-1$
                + System.lineSeparator()
                + "    outputFolderDerived=\"\"          Folder within the project where the generator puts java source files that will be overridden during each build cycle and delete and " //$NON-NLS-1$
                + "regenerated during a clean build cycle." //$NON-NLS-1$
                + System.lineSeparator()
                + "                                      Other builders can choose to maintain user code in a separate folder which is defined here." //$NON-NLS-1$
                + System.lineSeparator()
                + "                                      If you use the standard builder, leave the atribute empty." //$NON-NLS-1$
                + System.lineSeparator()
                + "    basePackageDerived=\"\"          Package prefix for all generated derived Java classes in the output folder for derived sources. See above." //$NON-NLS-1$
                + System.lineSeparator()
                + "    uniqueQualifier=\"\">            Optional argument if the basePackage names are not unique for all referencing source folders." //$NON-NLS-1$
                + System.lineSeparator()
                + " </" + XML_ELEMENT + ">" + System.lineSeparator(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void setSourceFolder(AFolder newFolder) {
        sourceFolder = newFolder;
        root = new IpsPackageFragmentRoot(getIpsObjectPath().getIpsProject(),
                getIpsPackageFragmentRootName());
    }

    @Override
    public String getType() {
        return TYPE_SRC_FOLDER;
    }

    @Override
    public AFolder getSourceFolder() {
        return sourceFolder;
    }

    @Override
    public String getIpsPackageFragmentRootName() {
        return sourceFolder.getName();
    }

    @Override
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return getIpsProject().getIpsPackageFragmentRoot(getIpsPackageFragmentRootName());
    }

    @Override
    public AFolder getOutputFolderForMergableJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderMergable;
        }
        return getIpsObjectPath().getOutputFolderForMergableSources();
    }

    @Override
    public AFolder getSpecificOutputFolderForMergableJavaFiles() {
        return outputFolderMergable;
    }

    @Override
    public void setSpecificOutputFolderForMergableJavaFiles(AFolder outputFolder) {
        outputFolderMergable = outputFolder;
    }

    @Override
    public String getBasePackageNameForMergableJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageMergable;
        }
        return getIpsObjectPath().getBasePackageNameForMergableJavaClasses();
    }

    @Override
    public String getSpecificBasePackageNameForMergableJavaClasses() {
        return basePackageMergable;
    }

    @Override
    public void setSpecificBasePackageNameForMergableJavaClasses(String name) {
        basePackageMergable = name;
    }

    @Override
    public AFolder getOutputFolderForDerivedJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderDerived;
        }
        return getIpsObjectPath().getOutputFolderForDerivedSources();
    }

    @Override
    public AFolder getSpecificOutputFolderForDerivedJavaFiles() {
        return outputFolderDerived;
    }

    @Override
    public void setSpecificOutputFolderForDerivedJavaFiles(AFolder outputFolder) {
        outputFolderDerived = outputFolder;
    }

    @Override
    public String getBasePackageNameForDerivedJavaClasses() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return basePackageDerived;
        }
        return getIpsObjectPath().getBasePackageNameForDerivedJavaClasses();
    }

    @Override
    public String getSpecificBasePackageNameForDerivedJavaClasses() {
        return basePackageDerived;
    }

    @Override
    public void setSpecificBasePackageNameForDerivedJavaClasses(String name) {
        basePackageDerived = name;
    }

    @Override
    public boolean exists(QualifiedNameType qnt) throws CoreRuntimeException {
        if (sourceFolder == null) {
            return false;
        }
        return sourceFolder.getFile(qnt.toPath()).exists();
    }

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType qnt) {
        IIpsPackageFragment pack = root.getIpsPackageFragment(qnt.getPackageName());
        if (pack == null) {
            return null;
        }
        IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
        if (file == null || !file.exists()) {
            return null;
        }
        return file;
    }

    @Override
    public void initFromXml(Element element, AProject project) {
        super.initFromXml(element, project);
        String sourceFolderPath = element.getAttribute(PROPERTY_SOURCE_FOLDER);
        setSourceFolder(project.getFolder(java.nio.file.Path.of(sourceFolderPath)));
        String outputFolderPathMergable = element.getAttribute(PROPERTY_OUTPUT_FOLDER_MERGABLE);
        outputFolderMergable = outputFolderPathMergable.equals(StringUtils.EMPTY) ? null
                : project.getFolder(java.nio.file.Path.of(outputFolderPathMergable));
        basePackageMergable = element.getAttribute(PROPERTY_BASE_PACKAGE_MERGABLE);
        tocPath = element.getAttribute(PROPERTY_TOC_PATH);
        if (StringUtils.isEmpty(tocPath)) {
            tocPath = DEFAULT_TOC_PATH;
        }
        validationMessagesBundle = element.getAttribute(PROPERTY_VALIDATION_MESSAGES_BUNDLE);
        if (StringUtils.isEmpty(validationMessagesBundle)) {
            validationMessagesBundle = DEFAUTL_VALIDATION_MESSAGES_BUNDLE;
        }
        String outputFolderPathDerived = element.getAttribute(PROPERTY_OUTPUT_FOLDER_DERIVED);
        outputFolderDerived = outputFolderPathDerived.equals(StringUtils.EMPTY) ? null
                : project.getFolder(java.nio.file.Path.of(outputFolderPathDerived));
        basePackageDerived = element.getAttribute(PROPERTY_BASE_PACKAGE_DERIVED);
        uniqueQualifier = StringUtils.trimToEmpty(element.getAttribute(PROPERTY_UNIQUE_QUALIFIER));
    }

    @Override
    public Element toXml(Document doc) {
        Element element = super.toXml(doc);
        element.setAttribute(PROPERTY_TYPE, TYPE_SRC_FOLDER);
        element.setAttribute(PROPERTY_SOURCE_FOLDER, sourceFolder.getProjectRelativePath().toString());
        element.setAttribute(PROPERTY_OUTPUT_FOLDER_MERGABLE, outputFolderMergable == null ? StringUtils.EMPTY
                : outputFolderMergable.getProjectRelativePath().toString());
        element.setAttribute(PROPERTY_BASE_PACKAGE_MERGABLE, basePackageMergable == null ? StringUtils.EMPTY
                : basePackageMergable);
        element.setAttribute(PROPERTY_TOC_PATH, tocPath == null ? "" : tocPath); //$NON-NLS-1$
        element.setAttribute(PROPERTY_VALIDATION_MESSAGES_BUNDLE, validationMessagesBundle == null ? StringUtils.EMPTY
                : validationMessagesBundle);
        element.setAttribute(PROPERTY_OUTPUT_FOLDER_DERIVED, outputFolderDerived == null ? StringUtils.EMPTY
                : outputFolderDerived.getProjectRelativePath().toString());
        element.setAttribute(PROPERTY_BASE_PACKAGE_DERIVED, basePackageDerived == null ? StringUtils.EMPTY
                : basePackageDerived);
        if (StringUtils.isNotEmpty(uniqueQualifier)) {
            element.setAttribute(PROPERTY_UNIQUE_QUALIFIER, uniqueQualifier);
        }
        return element;
    }

    @Override
    public MessageList validate() {
        MessageList result = new MessageList();
        // the sourceFolder will never be null (see this#initFromXml)
        result.add(validateIfFolderExists(sourceFolder));
        if (sourceFolder.getProjectRelativePath().getNameCount() > 1) {
            String text = MessageFormat.format(Messages.IpsSrcFolderEntry_srcFolderMustBeADirectChildOfTheProject,
                    sourceFolder
                            .getProjectRelativePath().toString());
            Message msg = new Message(MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT, text, Message.ERROR,
                    this);
            result.add(msg);
        }
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            result.add(validateOutputFolder());
        }
        result.add(validateUniqueBasePackage());
        return result;
    }

    private MessageList validateOutputFolder() {
        MessageList result = new MessageList();
        if (outputFolderMergable == null) {
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING,
                    Messages.IpsSrcFolderEntry_outputfoldermergablesrcmissing, Message.ERROR, this));
        }
        if (outputFolderDerived == null) {
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING,
                    Messages.IpsSrcFolderEntry_outputfoldersrcderivedmissing, Message.ERROR, this));
        }
        if (outputFolderMergable != null && !outputFolderMergable.exists()) {
            String text = MessageFormat.format(Messages.IpsSrcFolderEntry_outputfolderdoesntexist,
                    outputFolderMergable.getWorkspaceRelativePath());
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST, text, Message.ERROR, this));
        }
        if (outputFolderDerived != null && !outputFolderDerived.exists()) {
            String text = MessageFormat.format(Messages.IpsSrcFolderEntry_outputfolderdoesntexist,
                    outputFolderDerived.getWorkspaceRelativePath());
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_DERIVED_DOESNT_EXIST, text, Message.ERROR, this));
        }
        return result;
    }

    /**
     * Validate that the given folder exists.
     */
    private MessageList validateIfFolderExists(AFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()) {
            String text = MessageFormat.format(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

    private MessageList validateUniqueBasePackage() {
        MessageList ml = new MessageList();
        ml.add(validateUniqueBasePackage(getIpsObjectPath().getSourceFolderEntries()));
        Set<IIpsProject> allReferencedIpsProjects = new HashSet<>(getIpsObjectPath()
                .getAllReferencedIpsProjects());
        /*
         * Avoid testing source folder entries against themselves. A project can be referenced
         * multiple times in the same IPS object path. Thus use a set and remove this project.
         */
        allReferencedIpsProjects.remove(getIpsProject());
        for (IIpsProject refProject : allReferencedIpsProjects) {
            ml.add(validateUniqueBasePackage(refProject.getIpsObjectPath().getSourceFolderEntries()));
        }
        return ml;
    }

    private MessageList validateUniqueBasePackage(IIpsSrcFolderEntry[] entries) {
        MessageList ml = new MessageList();
        for (IIpsSrcFolderEntry entry : entries) {
            if (!equals(entry)) {
                ml.add(validateUniqueBasePackage(entry));
            }
        }
        return ml;
    }

    protected MessageList validateUniqueBasePackage(IIpsSrcFolderEntry entry) {
        MessageList ml = new MessageList();
        if (getUniqueBasePackageNameForMergableArtifacts()
                .equals(entry.getUniqueBasePackageNameForMergableArtifacts())) {
            ml.newError(MSGCODE_DUPLICATE_BASE_PACKAGE, MessageFormat.format(
                    Messages.IpsSrcFolderEntry_error_duplicateMergableBasePackage,
                    getUniqueBasePackageNameForMergableArtifacts()), this);
        }
        if (getUniqueBasePackageNameForDerivedArtifacts().equals(entry.getUniqueBasePackageNameForDerivedArtifacts())) {
            ml.newError(MSGCODE_DUPLICATE_BASE_PACKAGE, MessageFormat.format(
                    Messages.IpsSrcFolderEntry_error_duplicateDerivedBasePackage,
                    getUniqueBasePackageNameForDerivedArtifacts()), this);
        }
        return ml;
    }

    @Override
    public String getUniqueBasePackageNameForMergableArtifacts() {
        return QNameUtil.concat(getBasePackageNameForMergableJavaClasses(), getUniqueQualifier());
    }

    @Override
    public String getUniqueBasePackageNameForDerivedArtifacts() {
        return QNameUtil.concat(getBasePackageNameForDerivedJavaClasses(), getUniqueQualifier());
    }

    @Override
    public String getUniqueQualifier() {
        return uniqueQualifier;
    }

    @Override
    public void setUniqueQualifier(String uniqueQualifier) {
        this.uniqueQualifier = uniqueQualifier;
    }

    @Override
    public String toString() {
        return "SrcFolderEntry[" + sourceFolder.getProjectRelativePath().toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void setBasePackageRelativeTocPath(String newPath) {
        tocPath = newPath;
    }

    @Override
    public String getBasePackageRelativeTocPath() {
        return tocPath;
    }

    public String getFullTocPath() {
        String path = QNameUtil.toPath(getBasePackageNameForMergableJavaClasses()).toString();
        if (StringUtils.isEmpty(path)) {
            return tocPath;
        }
        return path + IPath.SEPARATOR + tocPath;
    }

    @Override
    public void setValidationMessagesBundle(String validationMessagesBundle) {
        this.validationMessagesBundle = validationMessagesBundle;
    }

    @Override
    public String getValidationMessagesBundle() {
        return validationMessagesBundle;
    }

    @Override
    public boolean containsResource(String resourcePath) {
        AFile file = getSourceFolder().getFile(resourcePath);
        return file.exists();
    }

    /**
     * Interprets the given path as relative to the referenced source folder.
     */
    @Override
    public InputStream getResourceAsStream(String pathAsString) {
        AFile file = getSourceFolder().getFile(pathAsString);
        return file.getContents();
    }
}
