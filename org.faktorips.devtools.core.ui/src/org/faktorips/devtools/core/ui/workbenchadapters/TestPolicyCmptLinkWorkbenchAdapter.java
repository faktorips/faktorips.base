/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class TestPolicyCmptLinkWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {
    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof ITestPolicyCmptLink) {
            ITestPolicyCmptLink policyCmptLink = (ITestPolicyCmptLink)ipsObjectPart;
            if (policyCmptLink.isAccoziation()) {
                // return the linked product cmpt image if the target relates a product cmpt,
                // or return the linked policy cmpt if target not found or no product cmpt is
                // related
                try {
                    ITestPolicyCmpt cmpt = policyCmptLink.findTarget();
                    if (cmpt != null && cmpt.hasProductCmpt()) {
                        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("LinkProductCmpt.gif", true); //$NON-NLS-1$
                    }
                } catch (CoreException e) {
                    // ignored exception, return default image
                }
                return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("LinkedPolicyCmptType.gif", true); //$NON-NLS-1$
            } else {
                try {
                    ITestPolicyCmptTypeParameter param = policyCmptLink.findTestPolicyCmptTypeParameter(policyCmptLink
                            .getIpsProject());
                    if (param != null) {
                        IPolicyCmptTypeAssociation association = param.findAssociation(policyCmptLink.getIpsProject());
                        if (association != null) {
                            return IpsUIPlugin.getImageHandling().getImageDescriptor(association);
                        }
                    }
                } catch (CoreException e) {
                    // ignore exception, return default image
                }
                return getDefaultImageDescriptor();
            }
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("AssociationType-Composition.gif", true); //$NON-NLS-1$
    }
}
