/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
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
            try {
                IIpsProject ipsProject = enumAttribute.getIpsProject();
                // TODO warum dieser Fall extra???
                if (enumAttribute.isInherited() && enumAttribute.findSuperEnumAttribute(ipsProject) == null) {
                    return IpsUIPlugin.getImageHandling().getSharedOverlayImage(ICON, OverlayIcons.OVERRIDE_OVR,
                            IDecoration.TOP_RIGHT);
                }

                boolean isUniqueIdentifier = EnumUtil.findEnumAttributeIsUnique(enumAttribute, ipsProject);
                if (enumAttribute.isInherited() && isUniqueIdentifier
                        && enumAttribute.findSuperEnumAttribute(ipsProject) == null) {
                    return IpsUIPlugin.getImageHandling().getSharedOverlayImage(ICON,
                            new String[] { OverlayIcons.KEY_OVR, OverlayIcons.OVERRIDE_OVR });
                }

                if (isUniqueIdentifier) {
                    return IpsUIPlugin.getImageHandling().getSharedOverlayImage(ICON, OverlayIcons.KEY_OVR,
                            IDecoration.TOP_LEFT);
                }

                if (enumAttribute.isInherited()) {
                    return IpsUIPlugin.getImageHandling().getSharedOverlayImage(ICON, OverlayIcons.OVERRIDE_OVR,
                            IDecoration.TOP_RIGHT);
                }

                return getDefaultImageDescriptor();

            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
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
            if (!StringUtils.isEmpty(enumAttribute.getDatatype())) {
                label += " : " + enumAttribute.getDatatype();
            }
            return label;
        } else {
            return super.getLabel(ipsObjectPart);
        }
    }

}
