/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;


/**
 *
 */
public class NewProductCmptTypeWizard extends NewIpsObjectWizard {
    
    private ProductCmptTypePage typePage;
    
    public NewProductCmptTypeWizard() {
        super(IpsObjectType.PRODUCT_CMPT_TYPE_V2);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewProductCmptTypeWizard.png")); //$NON-NLS-1$
    }
    
    /** 
     * {@inheritDoc}
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        typePage = new ProductCmptTypePage(selection);
        return typePage;
    }

    /** 
     * {@inheritDoc}
     */
    protected void createAdditionalPages() {
    }

    /** 
     * {@inheritDoc}
     */
    protected void finishIpsObject(IIpsObject ipsObject) throws CoreException {
        IProductCmptType type = (IProductCmptType)ipsObject;
        String supertypeName = typePage.getSuperType(); 
        type.setSupertype(supertypeName);
        if (typePage.overrideAbstractMethods()) {
            IMethod[] abstractMethods = type.findOverrideMethodCandidates(true, ipsObject.getIpsProject());
            type.overrideMethods(abstractMethods);
        }
    }

}
