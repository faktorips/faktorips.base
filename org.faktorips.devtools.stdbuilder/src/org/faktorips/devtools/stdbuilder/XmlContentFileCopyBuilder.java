package org.faktorips.devtools.stdbuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * An implementation of the IpsArtefactBuilder interface that copies the XML content files for
 * product components and tables in the according destination package. Before a file is copied the
 * ending is changed from the IpsObject specific ending to .xml.
 * 
 * @author Peter Erzberger
 */
// TODO test case has to be written
public class XmlContentFileCopyBuilder implements IIpsArtefactBuilder {

    private IJavaPackageStructure packageStructure;
    private IpsObjectType ipsObjectType;
    private String kind;

    public XmlContentFileCopyBuilder(IpsObjectType type, IJavaPackageStructure structure, String kind) {
        super();
        ArgumentCheck.notNull(kind, this);
        ArgumentCheck.notNull(structure, this);
        ArgumentCheck.notNull(type, this);
        ipsObjectType = type;
        packageStructure = structure;
        this.kind = kind;
    }

    /**
     * Empty implementation.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#beforeFullBuild()
     */
    public void beforeFullBuild() throws CoreException {
    }

    /**
     * Empty implementation.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#afterFullBuild()
     */
    public void afterFullBuild() throws CoreException {
    }

    /**
     * Empty implementation.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#beforeBuild(org.faktorips.devtools.core.model.IIpsObject,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
    }

    /**
     * Empty implementation.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#afterBuild(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
    }

    private String getContentAsString(InputStream is, String charSet) throws CoreException{
        try {
            return StringUtil.readFromInputStream(is, charSet);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    private ByteArrayInputStream convertContentAsStream(String content, String charSet) throws CoreException{
    
        try {
            return new ByteArrayInputStream(content.getBytes(charSet));
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }
    
    /**
     * Copies the xml content file of the provided IpsObject and changes the name of the extension
     * into .xml.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#build(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        InputStream is = null;
        try {
            is = file.getContents(true);
            IFile copy = getXmlContentFile(ipsSrcFile);
            IFolder folder = (IFolder)copy.getParent();
            if (!folder.exists()) {
                createFolder(folder);
            }
            if (copy.exists()) {
                String charSet = ipsSrcFile.getIpsProject().getProject().getDefaultCharset();
                String newContent = getContentAsString(is, charSet);
                String currentContent = getContentAsString(copy.getContents(), charSet);
                if(newContent.equals(currentContent)){
                    return;
                }
                is = convertContentAsStream(newContent, charSet);
                copy.setContents(is, true, true, null);
            } else {
                copy.create(is, true, null);
            }
        } catch (CoreException e) {
            throw new CoreException(new IpsStatus("Unable to create a content file for the file: "
                    + file.getName(), e));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new CoreException(new IpsStatus("Unable to close the input stream for the file: "
                            + file.getName(), e));
                }
            }
        }
    }

    private IFolder getXmlContentFileFolder(IIpsSrcFile ipsSrcFile) throws CoreException {
        String packageString = packageStructure.getPackage(kind, ipsSrcFile);
        IPath pathToPack = new Path(packageString.replace('.', '/'));
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination().getFolder(
            pathToPack);
    }

    private IFile getXmlContentFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        IFolder folder = getXmlContentFileFolder(ipsSrcFile);
        return folder.getFile(StringUtil.getFilenameWithoutExtension(file.getName()) + ".xml");
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#delete(org.faktorips.devtools.core.model.IIpsSrcFile)
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = getXmlContentFile(ipsSrcFile);
        if (file.exists()) {
            file.delete(true, null);
        }
    }

    private void createFolder(IFolder folder) throws CoreException {
        while (!folder.getParent().exists()) {
            createFolder((IFolder)folder.getParent());
        }
        folder.create(true, true, null);
    }

    /**
     * Returs true if the provided IpsObject is of the same type as the IpsObjectType this builder
     * is initialized with.
     * 
     * @see org.faktorips.devtools.core.model.IIpsArtefactBuilder#isBuilderFor(IIpsObject)
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return ipsObjectType.equals(ipsSrcFile.getIpsObjectType());
    }
}
