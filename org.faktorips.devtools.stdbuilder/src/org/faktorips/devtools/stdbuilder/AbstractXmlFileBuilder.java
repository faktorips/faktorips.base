/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

// TODO This class should be moved to devtools.core
public abstract class AbstractXmlFileBuilder extends AbstractArtefactBuilder {

    private IpsObjectType ipsObjectType;
    private String kind;

    public AbstractXmlFileBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet, String kind) {
        super(builderSet);
        ArgumentCheck.notNull(kind, this);
        ArgumentCheck.notNull(type, this);
        ipsObjectType = type;
        this.kind = kind;
    }

    private ByteArrayInputStream convertContentAsStream(String content, String charSet) throws CoreException {

        try {
            return new ByteArrayInputStream(content.getBytes(charSet));
        } catch (UnsupportedEncodingException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    /**
     * Copies the xml content (which is either the given string <code>newContent</code> or the
     * content of the given <code>IIpsSrcFile</code> if <code>newContent</code> is <code>null</code>
     * ).
     * 
     * @param ipsSrcFile The sourcefile to process.
     * @param newContent The content of the sourcefile to process. Must not be <code>null</code>.
     * 
     * @throws CoreException If any errors occur during the build.
     * @throws NullPointerException When <code>newContent</code> is <code>null</code>.
     */
    protected void build(IIpsSrcFile ipsSrcFile, String newContent) throws CoreException {
        ArgumentCheck.notNull(newContent);

        String charSet = ipsSrcFile.getIpsProject().getXmlFileCharset();
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        try {
            IFile copy = getXmlContentFile(ipsSrcFile);
            boolean newlyCreated = createFileIfNotThere(copy);
            if (!newlyCreated) {
                String currentContent = getContentAsString(copy.getContents(), charSet);
                if (!newContent.equals(currentContent)) {
                    copy.setContents(convertContentAsStream(newContent, charSet), true, true, null);
                }
            } else {
                copy.setContents(convertContentAsStream(newContent, charSet), true, false, null);
            }
        } catch (CoreException e) {
            throw new CoreException(new IpsStatus("Unable to create a content file for the file: " //$NON-NLS-1$
                    + file.getName(), e));
        }
    }

    private IFolder getXmlContentFileFolder(IIpsSrcFile ipsSrcFile) throws CoreException {
        String packageString = getBuilderSet().getPackage(kind, ipsSrcFile);
        IPath pathToPack = new Path(packageString.replace('.', '/'));
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true).getFolder(pathToPack);
    }

    /**
     * Returns the handle to the file where the xml content for the given ips source file is stored.
     */
    public IFile getXmlContentFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        IFolder folder = getXmlContentFileFolder(ipsSrcFile);
        return folder.getFile(StringUtil.getFilenameWithoutExtension(file.getName()) + ".xml"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = getXmlContentFile(ipsSrcFile);
        if (file.exists()) {
            file.delete(true, null);
        }
    }

    /**
     * Returns true if the provided IpsObject is of the same type as the IpsObjectType this builder
     * is initialised with.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder#isBuilderFor(IIpsObject)
     */
    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return ipsObjectType.equals(ipsSrcFile.getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "XmlContentFileCopyBuilder"; //$NON-NLS-1$
    }

    protected String getContentAsString(InputStream is, String charSet) throws CoreException {
        try {
            return StringUtil.readFromInputStream(is, charSet);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

}
