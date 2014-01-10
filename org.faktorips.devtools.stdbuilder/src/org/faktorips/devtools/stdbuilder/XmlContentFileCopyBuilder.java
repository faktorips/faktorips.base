/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * An implementation of the IpsArtefactBuilder interface that copies the XML content files for
 * product components and tables in the according destination package. Before a file is copied the
 * ending is changed from the IpsObject specific ending to .xml.
 * 
 * @author Peter Erzberger
 */
public abstract class XmlContentFileCopyBuilder extends AbstractXmlFileBuilder {

    public XmlContentFileCopyBuilder(IpsObjectType type, DefaultBuilderSet builderSet) {
        super(type, builderSet);
    }

    /**
     * Copies the xml content file of the provided IpsObject and changes the name of the extension
     * into .xml.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder#build(org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile)
     */
    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IFile file = (IFile)ipsSrcFile.getEnclosingResource();
        InputStream is = file.getContents(true);
        build(ipsSrcFile, getContentAsString(is, ipsSrcFile.getIpsProject().getXmlFileCharset()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getName();

    /**
     * {@inheritDoc}
     * 
     * Returns true.
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }
}
