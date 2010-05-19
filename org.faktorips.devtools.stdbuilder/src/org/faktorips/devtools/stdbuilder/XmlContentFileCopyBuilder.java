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

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;

/**
 * An implementation of the IpsArtefactBuilder interface that copies the XML content files for
 * product components and tables in the according destination package. Before a file is copied the
 * ending is changed from the IpsObject specific ending to .xml.
 * 
 * @author Peter Erzberger
 */
public class XmlContentFileCopyBuilder extends AbstractXmlFileBuilder {

    public XmlContentFileCopyBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet, String kind) {
        super(type, builderSet, kind);
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
    public String getName() {
        return "XmlContentFileCopyBuilder"; //$NON-NLS-1$
    }

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
