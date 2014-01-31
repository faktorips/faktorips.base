/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * Operation that is intended to be used by {@link NewProductCmptWizard} to create the new
 * {@link IIpsSrcFile}.
 */
public class NewProductCmptOperation extends NewProductDefinitionOperation<NewProductCmptPMO> {

    public NewProductCmptOperation(NewProductCmptPMO pmo) {
        super(pmo);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Subclassing:</strong><br>
     * The {@link NewProductCmptOperation} implementation adds the ability to copy an existing
     * source file based upon the configuration of {@link NewProductCmptPMO}.
     */
    @Override
    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        if (getPmo().isCopyMode()) {
            return copyIpsSrcFile(monitor);
        } else {
            return super.createIpsSrcFile(monitor);
        }
    }

    private IIpsSrcFile copyIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment targetPackageFragment = getPmo().getIpsPackage();
        String fileName = IpsObjectType.PRODUCT_CMPT.getFileName(getPmo().getName());
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

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Subclassing:</strong><br>
     * The {@link NewProductCmptOperation} implementation sets the product component type and
     * runtime id as configured by the user. Furthermore, a product component generation is created
     * being valid from the effective date as provided by the user. Finally, all differences to the
     * model are fixed.
     */
    @Override
    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
        IProductCmpt newProductCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
        newProductCmpt.setProductCmptType(getPmo().getSelectedType().getQualifiedName());
        newProductCmpt.setRuntimeId(getPmo().getRuntimeId());

        if (!getPmo().isCopyMode()) {
            IProductCmptGeneration generation = (IProductCmptGeneration)newProductCmpt.newGeneration();
            generation.setValidFrom(getPmo().getEffectiveDate());
            newProductCmpt.fixAllDifferencesToModel(getPmo().getIpsProject());
        }

        monitor.worked(1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Subclassing:</strong><br>
     * The {@link NewProductCmptOperation} implementation adds the ability to also create a new
     * {@link IProductCmptLink} using the new product component as target. The link will be created
     * at the {@link IProductCmptGeneration} as configured by the {@link NewProductCmptPMO}.
     */
    @Override
    protected void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        monitor.beginTask(null, 2);

        IProductCmptTypeAssociation association = getPmo().getAddToAssociation();
        IProductCmptGeneration generationToAddTo = getPmo().getAddToProductCmptGeneration();
        // @formatter:off
        if (generationToAddTo != null
                && IpsUIPlugin.getDefault().isGenerationEditable(generationToAddTo)
                && association != null) {
            // @formatter:on
            IIpsSrcFile srcFile = generationToAddTo.getIpsSrcFile();
            if (getPmo().getValidator().validateAddToGeneration().isEmpty()) {
                boolean wasDirty = srcFile.isDirty();

                String targetQName = ipsSrcFile.getQualifiedNameType().getName();
                new LinkCreatorUtil(false).createLink(association, generationToAddTo, targetQName);
                monitor.worked(1);

                if (!wasDirty) {
                    try {
                        srcFile.save(true, new SubProgressMonitor(monitor, 1));
                    } catch (CoreException e) {
                        throw new CoreRuntimeException(e);
                    }
                }
            }
        }
    }

}
