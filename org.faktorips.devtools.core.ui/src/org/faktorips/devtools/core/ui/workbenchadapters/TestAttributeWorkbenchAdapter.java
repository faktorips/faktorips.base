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
import org.faktorips.devtools.core.internal.model.testcasetype.TestAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class TestAttributeWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof TestAttribute) {
            TestAttribute testAttribute = (TestAttribute)ipsObjectPart;
            try {
                // TODO v2 - hier koennen wir doch auch ein festes Image nehmen, oder? (von jörg)
                IPolicyCmptTypeAttribute attribute = testAttribute.findAttribute(testAttribute.getIpsProject());
                if (attribute != null) {
                    return IpsUIPlugin.getImageHandling().getImageDescriptor(attribute);
                }
            } catch (CoreException e) {
                // ignore exception, return default image
            }
            return getDefaultImageDescriptor();
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor("DeltaTypeMissingPropertyValue.gif", true); //$NON-NLS-1$
    }

}
