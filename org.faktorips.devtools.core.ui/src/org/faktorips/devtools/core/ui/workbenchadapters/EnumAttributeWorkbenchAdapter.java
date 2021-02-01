/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class EnumAttributeWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    private static final String ICON = "EnumAttribute.gif"; //$NON-NLS-1$

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
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

            return IpsUIPlugin.getImageHandling().getSharedOverlayImage(ICON,
                    overlayList.toArray(new String[overlayList.size()]));
        }

        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(ICON, true);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IEnumAttribute) {
            IEnumAttribute enumAttribute = (IEnumAttribute)ipsObjectPart;
            String label = enumAttribute.getName();
            try {
                Datatype datatype = enumAttribute.findDatatype(enumAttribute.getIpsProject());
                String datatypeName = (datatype == null) ? "" : datatype.getName(); //$NON-NLS-1$
                if (!(StringUtils.isEmpty(datatypeName))) {
                    label += " : " + datatypeName; //$NON-NLS-1$
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return label;
        } else {
            return super.getLabel(ipsObjectPart);
        }
    }

}
