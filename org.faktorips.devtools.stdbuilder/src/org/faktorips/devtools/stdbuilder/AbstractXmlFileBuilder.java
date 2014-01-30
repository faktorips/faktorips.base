/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

// TODO This class should be moved to devtools.core
/**
 * This abstract xml file builder handles general purposes building xml files in a java package
 * structure.
 */
public abstract class AbstractXmlFileBuilder extends AbstractArtefactBuilder {

    private IpsObjectType ipsObjectType;

    public AbstractXmlFileBuilder(IpsObjectType type, DefaultBuilderSet builderSet) {
        super(builderSet);
        ArgumentCheck.notNull(type, this);
        ipsObjectType = type;
    }

    @Override
    public DefaultBuilderSet getBuilderSet() {
        return (DefaultBuilderSet)super.getBuilderSet();
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
            ByteArrayInputStream content = convertContentAsStream(newContent, charSet);
            if (!newlyCreated) {
                String currentContent = getContentAsString(copy.getContents(), charSet);
                if (!newContent.equals(currentContent)) {
                    writeToFile(copy, content, true, true);
                }
            } else {
                writeToFile(copy, content, true, false);
            }
        } catch (CoreException e) {
            throw new CoreException(new IpsStatus("Unable to create a content file for the file: " //$NON-NLS-1$
                    + file.getName(), e));
        }
    }

    /**
     * Returns the relative path to the generated XML file.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to generate
     * @return the relative path to the generated XML file
     */
    public IPath getXmlContentRelativeFile(IIpsSrcFile ipsSrcFile) {
        String packageString = getBuilderSet().getPackageName(ipsSrcFile, isBuildingInternalArtifacts(),
                !buildsDerivedArtefacts());
        IPath pathToPack = new Path(packageString.replace('.', '/'));
        return pathToPack.append(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName())).addFileExtension("xml");
    }

    /**
     * Returns the handle to the file where the xml content for the given ips source file is stored.
     */
    public IFile getXmlContentFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        return ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true)
                .getFile(getXmlContentRelativeFile(ipsSrcFile));
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

    @Override
    public boolean isBuildingInternalArtifacts() {
        return getBuilderSet().isGeneratePublishedInterfaces();
    }

}
