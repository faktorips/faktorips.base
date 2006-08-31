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

package org.faktorips.devtools.core.ui.wizards.policycmpttype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;


/**
 *
 */
public class NewPcTypeWizard extends NewIpsObjectWizard {
    
    private PcTypePage typePage;
    
    public NewPcTypeWizard() {
        super(IpsObjectType.POLICY_CMPT_TYPE);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewPolicyCmptTypeWizard.gif"));
    }
    
    /** 
     * Overridden method.
     * @throws JavaModelException
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createFirstPage()
     */
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        typePage = new PcTypePage(selection);
        return typePage;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createAdditionalPages()
     */
    protected void createAdditionalPages() {
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#finishIpsObject(org.faktorips.devtools.core.model.IIpsObject)
     */
    protected void finishIpsObject(IIpsObject pdObject) throws CoreException {
        IPolicyCmptType type = (IPolicyCmptType)pdObject;
        String supertypeName = typePage.getSuperType(); 
        type.setSupertype(supertypeName);
        String postfix = IpsPlugin.getDefault().getIpsPreferences().getDefaultProductCmptTypePostfix();
        if (StringUtils.isNotEmpty(postfix)) {
        	type.setConfigurableByProductCmptType(true);
        	type.setUnqualifiedProductCmptType(type.getName() + postfix);
        } else {
        	type.setConfigurableByProductCmptType(false);
        }
        if (typePage.overrideAbstractMethods()) {
            IMethod[] abstractMethods = type.findOverrideMethodCandidates(true);
            type.overrideMethods(abstractMethods);
        }
    }

}
