/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.runtime.ClassloaderRuntimeRepository;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsSrcFolderEntry.
 * 
 * @author Jan Ortmann
 */
public class IpsSrcFolderEntry extends IpsObjectPathEntry implements IIpsSrcFolderEntry {

    private final static String DEFAULT_TOC_PATH = ClassloaderRuntimeRepository.TABLE_OF_CONTENTS_FILE;

    /**
     * Returns a description of the xml format.
     */
    public final static String getXmlFormatDescription() {
        return "Sourcefolder:" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "  <" + XML_ELEMENT + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    type=\"src\"" + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    sourceFolder=\"model\"            Folder in the project that contains the Faktor-IPS model and product definition files." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    outputFolderMergable=\"src\"      Folder in the project where the generator puts the java source files which content will be merged with " + //$NON-NLS-1$
                "the newly generated content during each build cycle." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "    basePackageMergable=\"org.foo\"   The package prefix for all generated but mergable java files." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    tocPath=\"motor/motor-reposiory-toc.xml\" " + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                      The partial path of the resource containing the runtime repository table of content (toc)." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                      The full path is derived from the basePackageMergeable by adding this partial path." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                      The file is not part of the published interface so it is places in the internal package." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    outputFolderDerived=\"\"          Folder within the project where the generator puts java source files that will be overridden during each build cycle and delete and " + //$NON-NLS-1$
                "regenerated during a clean build cycle." //$NON-NLS-1$
                + SystemUtils.LINE_SEPARATOR
                + "                                      Other builders can choose to maintain user code in a separate folder which is defined here." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "                                      If you use the standard builder, leave the atribute empty." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + "    basePackageDerived=\"\">          Package prefix for all generated derived Java classes in the output folder for derived sources. See above." + SystemUtils.LINE_SEPARATOR //$NON-NLS-1$
                + " </" + XML_ELEMENT + ">" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /** the folder containig the ips objects */
    private IFolder sourceFolder;

    /** the output folder containing the generated but mergable Java files. */
    private IFolder outputFolderMergable;

    /** the name of the base package containing the generated but mergable Java files. */
    private String basePackageMergable = ""; //$NON-NLS-1$

    private String tocPath = DEFAULT_TOC_PATH;

    /** the output folder containing the Java files that are generated and derived. */
    private IFolder outputFolderDerived;

    /**
     * the name of the base package containing the Java files where the developer adds it's own
     * code.
     */
    private String basePackageDerived = ""; //$NON-NLS-1$

    private IIpsPackageFragmentRoot root;

    public IpsSrcFolderEntry(IpsObjectPath path) {
        super(path);
    }

    public IpsSrcFolderEntry(IpsObjectPath path, IFolder sourceFolder) {
        super(path);
        ArgumentCheck.notNull(sourceFolder);
        setSourceFolder(sourceFolder);
    }

    private void setSourceFolder(IFolder newFolder) {
        sourceFolder = newFolder;
        root = new IpsPackageFragmentRoot((IpsProject)getIpsObjectPath().getIpsProject(),
                getIpsPackageFragmentRootName());
    }

    @Override
    public String getType() {
        return TYPE_SRC_FOLDER;
    }

    @Override
    public IFolder getSourceFolder() {
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
    public IFolder getOutputFolderForMergableJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderMergable;
        }
        return getIpsObjectPath().getOutputFolderForMergableSources();
    }

    @Override
    public IFolder getSpecificOutputFolderForMergableJavaFiles() {
        return outputFolderMergable;
    }

    @Override
    public void setSpecificOutputFolderForMergableJavaFiles(IFolder outputFolder) {
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
    public IFolder getOutputFolderForDerivedJavaFiles() {
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder()) {
            return outputFolderDerived;
        }
        return getIpsObjectPath().getOutputFolderForDerivedSources();
    }

    @Override
    public IFolder getSpecificOutputFolderForDerivedJavaFiles() {
        return outputFolderDerived;
    }

    @Override
    public void setSpecificOutputFolderForDerivedJavaFiles(IFolder outputFolder) {
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
    public boolean exists(QualifiedNameType qnt) throws CoreException {
        if (sourceFolder == null) {
            return false;
        }
        return sourceFolder.getFile(qnt.toPath()).exists();
    }

    @Override
    public void findIpsSrcFilesInternal(IpsObjectType type,
            String packageFragment,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {

        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot()).findIpsSourceFiles(type, packageFragment, result);
    }

    @Override
    public IIpsSrcFile findIpsSrcFileInternal(QualifiedNameType qnt, Set<IIpsObjectPathEntry> visitedEntries)
            throws CoreException {

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
    public void findIpsSrcFilesStartingWithInternal(IpsObjectType type,
            String prefix,
            boolean ignoreCase,
            List<IIpsSrcFile> result,
            Set<IIpsObjectPathEntry> visitedEntries) throws CoreException {
        ((IpsPackageFragmentRoot)getIpsPackageFragmentRoot()).findIpsSourceFilesStartingWithInternal(type, prefix,
                ignoreCase, result);
    }

    @Override
    public void initFromXml(Element element, IProject project) {
        String sourceFolderPath = element.getAttribute("sourceFolder"); //$NON-NLS-1$
        setSourceFolder(project.getFolder(new Path(sourceFolderPath)));
        String outputFolderPathMergable = element.getAttribute("outputFolderMergable"); //$NON-NLS-1$
        outputFolderMergable = outputFolderPathMergable.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathMergable));
        basePackageMergable = element.getAttribute("basePackageMergable"); //$NON-NLS-1$
        tocPath = element.getAttribute("tocPath"); //$NON-NLS-1$
        if (StringUtils.isEmpty(tocPath)) {
            tocPath = DEFAULT_TOC_PATH;
        }
        String outputFolderPathDerived = element.getAttribute("outputFolderDerived"); //$NON-NLS-1$
        outputFolderDerived = outputFolderPathDerived.equals("") ? null : project.getFolder(new Path( //$NON-NLS-1$
                outputFolderPathDerived));
        basePackageDerived = element.getAttribute("basePackageDerived"); //$NON-NLS-1$
    }

    @Override
    public Element toXml(Document doc) {
        Element element = doc.createElement(IpsObjectPathEntry.XML_ELEMENT);
        element.setAttribute("type", TYPE_SRC_FOLDER); //$NON-NLS-1$
        element.setAttribute("sourceFolder", sourceFolder.getProjectRelativePath().toString()); //$NON-NLS-1$
        element.setAttribute("outputFolderMergable", outputFolderMergable == null ? "" : outputFolderMergable //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageMergable", basePackageMergable == null ? "" : basePackageMergable); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("tocPath", tocPath == null ? "" : tocPath); //$NON-NLS-1$ //$NON-NLS-2$
        element.setAttribute("outputFolderDerived", outputFolderDerived == null ? "" : outputFolderDerived //$NON-NLS-1$ //$NON-NLS-2$
                .getProjectRelativePath().toString());
        element.setAttribute("basePackageDerived", basePackageDerived == null ? "" : basePackageDerived); //$NON-NLS-1$ //$NON-NLS-2$
        return element;
    }

    @Override
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();
        // the sourceFolder will never be null (see this#initFromXml)
        result.add(validateIfFolderExists(sourceFolder));
        if (sourceFolder.getProjectRelativePath().segmentCount() > 1) {
            String text = NLS.bind(Messages.IpsSrcFolderEntry_srcFolderMustBeADirectChildOfTheProject, sourceFolder
                    .getProjectRelativePath().toString());
            Message msg = new Message(MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT, text, Message.ERROR,
                    this);
            result.add(msg);
        }
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder() && outputFolderMergable == null) {
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING,
                    Messages.IpsSrcFolderEntry_outputfoldermergablesrcmissing, Message.ERROR));
        }
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder() && outputFolderDerived == null) {
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING,
                    Messages.IpsSrcFolderEntry_outputfoldersrcderivedmissing, Message.ERROR));
        }
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder() && outputFolderMergable != null
                && !outputFolderMergable.exists()) {
            String text = NLS.bind(Messages.IpsSrcFolderEntry_outputfolderdoesntexist,
                    outputFolderMergable.getFullPath());
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST, text, Message.ERROR));
        }
        if (getIpsObjectPath().isOutputDefinedPerSrcFolder() && outputFolderDerived != null
                && !outputFolderDerived.exists()) {
            String text = NLS.bind(Messages.IpsSrcFolderEntry_outputfolderdoesntexist,
                    outputFolderDerived.getFullPath());
            result.add(new Message(MSGCODE_OUTPUT_FOLDER_DERIVED_DOESNT_EXIST, text, Message.ERROR));
        }

        return result;
    }

    /**
     * Validate that the given folder exists.
     */
    private MessageList validateIfFolderExists(IFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()) {
            String text = NLS.bind(Messages.IpsSrcFolderEntry_msgMissingFolder, folder.getName());
            Message msg = new Message(MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
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
            return tocPath.toString();
        }
        return path + IPath.SEPARATOR + tocPath;
    }

    /**
     * Interprets the given path as relative to the referenced sourcefolder.
     */
    @Override
    public InputStream getRessourceAsStream(String pathAsString) throws CoreException {
        IFile file = getSourceFolder().getFile(pathAsString);
        if (file.exists()) {
            return file.getContents();
        }
        return null;
    }

}
