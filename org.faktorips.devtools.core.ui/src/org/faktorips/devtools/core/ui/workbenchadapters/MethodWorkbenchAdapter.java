/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.util.QNameUtil;

public class MethodWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    public static final String METHOD_IMAGE_NAME = "MethodPublic.gif";

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(METHOD_IMAGE_NAME, true);
    }

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IMethod) {
            IMethod method = (IMethod)ipsObjectPart;
            String[] overlays = new String[4];
            try {
                if (method.findOverriddenMethod(method.getIpsProject()) != null) {
                    overlays[3] = OverlayIcons.OVERRIDE_OVR;
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            if (method.isAbstract()) {
                overlays[1] = OverlayIcons.ABSTRACT_OVR;
            }
            return IpsUIPlugin.getImageHandling().getSharedOverlayImage(METHOD_IMAGE_NAME, overlays);
        } else {
            return getDefaultImageDescriptor();
        }
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IMethod) {
            IMethod method = (IMethod)ipsObjectPart;
            StringBuffer buffer = new StringBuffer(method.getName());
            buffer.append('(');
            IParameter[] params = method.getParameters();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    buffer.append(", "); //$NON-NLS-1$
                }
                buffer.append(QNameUtil.getUnqualifiedName(params[i].getDatatype()));
            }
            buffer.append(") : "); //$NON-NLS-1$
            buffer.append(QNameUtil.getUnqualifiedName(method.getDatatype()));
            return buffer.toString();
        } else {
            return super.getLabel(ipsObjectPart);
        }
    }

}
