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

import java.io.InputStream;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;

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
     * @see IIpsArtefactBuilder#build(IIpsSrcFile)
     */
    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
        AFile file = (AFile)ipsSrcFile.getEnclosingResource();
        InputStream is = file.getContents();
        build(ipsSrcFile, is);
    }

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
