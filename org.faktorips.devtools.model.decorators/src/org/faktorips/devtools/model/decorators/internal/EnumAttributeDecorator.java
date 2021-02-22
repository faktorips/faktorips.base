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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.internal.IpsStringUtils;

public class EnumAttributeDecorator implements IIpsObjectPartDecorator {

    private static final String ICON = "EnumAttribute.gif"; //$NON-NLS-1$

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IEnumAttribute) {
            IEnumAttribute enumAttribute = (IEnumAttribute)ipsObjectPart;
            List<String> overlayList = new ArrayList<String>(2);

            try {
                IIpsProject ipsProject = enumAttribute.getIpsProject();
                boolean isUniqueIdentifier = enumAttribute.findIsUnique(ipsProject);
                if (isUniqueIdentifier) {
                    overlayList.add(OverlayIcons.KEY_OVR);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            if (enumAttribute.isInherited()) {
                overlayList.add(OverlayIcons.OVERRIDE_OVR);
            }

            return IIpsDecorators.getImageHandling().getSharedOverlayImage(ICON,
                    overlayList.toArray(new String[overlayList.size()]));
        }

        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(ICON, true);
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IEnumAttribute) {
            IEnumAttribute enumAttribute = (IEnumAttribute)ipsObjectPart;
            String label = enumAttribute.getName();
            try {
                Datatype datatype = enumAttribute.findDatatype(enumAttribute.getIpsProject());
                String datatypeName = (datatype == null) ? "" : datatype.getName(); //$NON-NLS-1$
                if (!(IpsStringUtils.isEmpty(datatypeName))) {
                    label += " : " + datatypeName; //$NON-NLS-1$
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return label;
        } else {
            return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
        }
    }

}
