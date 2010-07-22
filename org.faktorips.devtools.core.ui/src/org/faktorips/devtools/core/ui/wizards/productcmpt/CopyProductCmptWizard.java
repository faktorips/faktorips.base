/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.INewIpsObjectWizard;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * Wizard to create a new product component as copy of a given product component.
 */
public class CopyProductCmptWizard extends Wizard implements INewIpsObjectWizard {

    private IStructuredSelection selection;
    private ProductCmptPage productCmptPage;
    private IProductCmpt sourceProductCmpt;

    public CopyProductCmptWizard(IProductCmpt productCmpt) {
        ArgumentCheck.notNull(productCmpt);
        setWindowTitle(Messages.CopyProductCmptWizard_titleCopyProductComponent);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewProductCmptWizard.png")); //$NON-NLS-1$
        sourceProductCmpt = productCmpt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void addPages() {
        try {
            productCmptPage = createFirstPage(selection);
            addPage(productCmptPage);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private ProductCmptPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        productCmptPage = new ProductCmptPage(selection);
        try {
            productCmptPage.setDefaultProductCmpt(sourceProductCmpt);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return productCmptPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        try {
            IIpsPackageFragment pack = productCmptPage.getIpsPackageFragment();
            IIpsSrcFile srcFile = pack.createIpsFile(
                    getIpsObjectType().getFileName(productCmptPage.getIpsObjectName()),
                    getContentsOfIpsObject(sourceProductCmpt), true, null);
            finishIpsObject((IProductCmpt)srcFile.getIpsObject());
            srcFile.save(true, null);
            IpsUIPlugin.getDefault().openEditor(srcFile);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }

    private void finishIpsObject(IProductCmpt productCmpt) throws CoreException {
        productCmpt.setProductCmptType(productCmptPage.getProductCmptType());
        productCmpt.setRuntimeId(productCmptPage.getRuntimeId());
    }

    private String getContentsOfIpsObject(IIpsObject ipsObject) {
        String encoding = ipsObject.getIpsProject().getXmlFileCharset();
        try {
            return XmlUtil.nodeToString(ipsObject.toXml(IpsPlugin.getDefault().newDocumentBuilder().newDocument()),
                    encoding);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
            // This is a programing error, rethrow as runtime exception
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.PRODUCT_CMPT;
    }
}
