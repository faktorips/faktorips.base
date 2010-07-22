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

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.EnumUtil;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

public class EnumAttributeWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    private final static String ICON = "EnumAttribute.gif";

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IEnumAttribute) {
            IEnumAttribute enumAttribute = (IEnumAttribute)ipsObjectPart;
            List<String> overlayList = new ArrayList<String>(2);

            try {
                IIpsProject ipsProject = enumAttribute.getIpsProject();
                boolean isUniqueIdentifier = EnumUtil.findEnumAttributeIsUnique(enumAttribute, ipsProject);
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
                String datatypeName = (datatype == null) ? "" : datatype.getName();
                if (!(StringUtils.isEmpty(datatypeName))) {
                    label += " : " + datatypeName;
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
