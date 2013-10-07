/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

public class AssociationWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    public static final String ASSOCIATION_TYPE_COMPOSITION_IMAGE = "AssociationType-Composition.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE = "AssociationType-CompositionDetailToMaster.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_ASSOCIATION_IMAGE = "AssociationType-Association.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_AGGREGATION_IMAGE = "AssociationType-Aggregation.gif"; //$NON-NLS-1$

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAssociation) {
            IAssociation association = (IAssociation)ipsObjectPart;
            String baseName = getImageBaseName(association);
            String[] overlays = getImageOverlays(association);
            return IpsUIPlugin.getImageHandling().getSharedOverlayImage(baseName, overlays);
        }
        return null;
    }

    private String getImageBaseName(IAssociation association) {
        String baseName = ""; //$NON-NLS-1$
        if (association.getAssociationType() == AssociationType.AGGREGATION) {
            baseName = ASSOCIATION_TYPE_AGGREGATION_IMAGE;
        } else if (association.getAssociationType() == AssociationType.ASSOCIATION) {
            baseName = ASSOCIATION_TYPE_ASSOCIATION_IMAGE;
        } else if (association.getAssociationType() == AssociationType.COMPOSITION_DETAIL_TO_MASTER) {
            baseName = ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE;
        } else if (association.getAssociationType() == AssociationType.COMPOSITION_MASTER_TO_DETAIL) {
            baseName = ASSOCIATION_TYPE_COMPOSITION_IMAGE;
        }
        return baseName;
    }

    private String[] getImageOverlays(IAssociation association) {
        String[] overlays = new String[4];

        if (association instanceof IPolicyCmptTypeAssociation) {
            try {
                IPolicyCmptTypeAssociation polAssociation = (IPolicyCmptTypeAssociation)association;
                if (polAssociation.isConfigured()
                        && polAssociation.isConstrainedByProductStructure(association.getIpsProject())) {
                    overlays[IDecoration.TOP_RIGHT] = OverlayIcons.PRODUCT_OVR;
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        if (association instanceof IProductCmptTypeAssociation) {
            IProductCmptTypeAssociation productAssociation = (IProductCmptTypeAssociation)association;
            if (!productAssociation.isChangingOverTime()) {
                overlays[IDecoration.BOTTOM_RIGHT] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
            }
        }

        if (association.isConstrains()) {
            overlays[IDecoration.TOP_LEFT] = OverlayIcons.OVERRIDE_OVR;
        }
        return overlays;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(ASSOCIATION_TYPE_ASSOCIATION_IMAGE, true);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAssociation) {
            IAssociation association = (IAssociation)ipsObjectPart;
            if (association.is1ToMany()) {
                return association.getTargetRolePlural();
            }
            return association.getTargetRoleSingular();
        } else {
            return super.getLabel(ipsObjectPart);
        }
    }

}
