/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * Operation that is intended to be used by {@link NewProductWizard} to create the new
 * {@link IIpsSrcFile}.
 */
public class CopyProductCmptOperation extends NewProductCmptOperation {

    public CopyProductCmptOperation(NewProductCmptPMO pmo) {
        super(pmo);
    }

    @Override
    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        return copyIpsSrcFile(monitor);
    }

    private IIpsSrcFile copyIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment targetPackageFragment = getPmo().getIpsPackage();
        String fileName = getPmo().getCopyProductCmpt().getIpsObjectType().getFileName(getPmo().getName());
        // @formatter:off
        return targetPackageFragment.createIpsFile(
                fileName,
                getContentsOfIpsObject(getPmo().getCopyProductCmpt()),
                true,
                new SubProgressMonitor(monitor, 1));
        // @formatter:on
    }

    private String getContentsOfIpsObject(IIpsObject ipsObject) {
        String encoding = ipsObject.getIpsProject().getXmlFileCharset();
        Element xml = ipsObject.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
        try {
            return XmlUtil.nodeToString(xml, encoding);
        } catch (TransformerException e) {
            // This is a programming error, re-throw as runtime exception
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
        initProductCmpt(ipsSrcFile);
        monitor.worked(1);

    }

}
