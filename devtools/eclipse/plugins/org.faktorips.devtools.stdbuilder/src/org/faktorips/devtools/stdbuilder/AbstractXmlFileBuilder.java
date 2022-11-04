/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.util.PathUtil;
import org.faktorips.devtools.model.builder.AbstractArtefactBuilder;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;
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

    private ByteArrayInputStream convertContentAsStream(String content, String charSet) {

        try {
            return new ByteArrayInputStream(content.getBytes(charSet));
        } catch (UnsupportedEncodingException e) {
            throw new IpsException(new IpsStatus(e));
        }
    }

    /**
     * Writes the new XML content to the output file for the given {@link IIpsSrcFile} creating it
     * if necessary and keeping the old version in the local history otherwise.
     * 
     * @param ipsSrcFile The source file to process
     * @param newContent The content of the source file to process. Must not be <code>null</code>
     * 
     * @throws IpsException if any errors occur during the build
     * @throws NullPointerException when <code>newContent</code> is <code>null</code>
     */
    protected void build(IIpsSrcFile ipsSrcFile, String newContent) {
        ArgumentCheck.notNull(newContent);

        String charSet = ipsSrcFile.getIpsProject().getXmlFileCharset();
        build(ipsSrcFile, convertContentAsStream(newContent, charSet));
    }

    /**
     * Writes the new XML content to the output file for the given {@link IIpsSrcFile} creating it
     * if necessary and keeping the old version in the local history otherwise.
     * 
     * @param ipsSrcFile The source file to process
     * @param newContent The content of the source file to process. Must not be <code>null</code>
     * 
     * @throws IpsException if any errors occur during the build
     * @throws NullPointerException when <code>newContent</code> is <code>null</code>
     */
    protected void build(IIpsSrcFile ipsSrcFile, InputStream newContent) {
        ArgumentCheck.notNull(newContent);

        AFile file = (AFile)ipsSrcFile.getEnclosingResource();
        try {
            AFile copy = getXmlContentFile(ipsSrcFile);
            boolean newlyCreated = createFileIfNotThere(copy);
            writeToFile(copy, newContent, !newlyCreated);
        } catch (IpsException e) {
            throw new IpsException(new IpsStatus("Unable to create a content file for the file: " //$NON-NLS-1$
                    + file.getName(), e));
        }
    }

    /**
     * Returns the relative path to the generated XML file.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to generate
     * @return the relative path to the generated XML file
     */
    public Path getXmlContentRelativeFile(IIpsSrcFile ipsSrcFile) {
        String packageString = getBuilderSet().getPackageName(ipsSrcFile, isBuildingInternalArtifacts(),
                !buildsDerivedArtefacts());
        Path pathToPack = Path.of(packageString.replace('.', '/'));
        return pathToPack.resolve(StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + ".xml");
    }

    /**
     * Returns the handle to the file where the xml content for the given ips source file is stored.
     */
    public AFile getXmlContentFile(IIpsSrcFile ipsSrcFile) {
        return ((AFolder)ipsSrcFile.getIpsPackageFragment().getRoot().getArtefactDestination(true).getResource())
                .getFile(PathUtil.toPortableString(getXmlContentRelativeFile(ipsSrcFile)));
    }

    @Override
    public void delete(IIpsSrcFile ipsSrcFile) {
        AFile file = getXmlContentFile(ipsSrcFile);
        if (file.exists()) {
            file.delete(null);
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

    @Override
    public String getName() {
        return "XmlContentFileCopyBuilder"; //$NON-NLS-1$
    }

    @Override
    public boolean isBuildingInternalArtifacts() {
        return getBuilderSet().isGeneratePublishedInterfaces();
    }

}
