/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.runtime.util.StringBuilderJoiner;

public class MethodDecorator implements IIpsObjectPartDecorator {

    public static final String METHOD_IMAGE_NAME = "MethodPublic.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(METHOD_IMAGE_NAME, true);
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IMethod) {
            IMethod method = (IMethod)ipsObjectPart;
            String[] overlays = getOverlays(method);
            return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(METHOD_IMAGE_NAME, overlays);
        } else {
            return getDefaultImageDescriptor();
        }
    }

    protected String[] getOverlays(IMethod method) {
        String[] overlays = new String[4];
        try {
            if (method.findOverriddenMethod(method.getIpsProject()) != null) {
                overlays[3] = OverlayIcons.OVERRIDE;
            }
        } catch (IpsException e) {
            IpsLog.log(e);
        }
        if (method.isAbstract()) {
            overlays[1] = OverlayIcons.ABSTRACT;
        }
        return overlays;
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IBaseMethod) {
            IBaseMethod method = (IBaseMethod)ipsObjectPart;
            StringBuilder builder = new StringBuilder(method.getName());
            builder.append('(');
            StringBuilderJoiner.join(builder, method.getParameters(),
                    p -> builder.append(QNameUtil.getUnqualifiedName(p.getDatatype())));
            builder.append(") : "); //$NON-NLS-1$
            builder.append(QNameUtil.getUnqualifiedName(method.getDatatype()));
            return builder.toString();
        } else {
            return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
        }
    }

}
