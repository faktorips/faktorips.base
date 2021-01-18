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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;

public class AssociationWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    public static final String ASSOCIATION_TYPE_COMPOSITION_IMAGE = "AssociationType-Composition.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE = "AssociationType-CompositionDetailToMaster.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_ASSOCIATION_IMAGE = "AssociationType-Association.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_AGGREGATION_IMAGE = "AssociationType-Aggregation.gif"; //$NON-NLS-1$

    private final boolean showChangingOverTimeOverlay;

    public AssociationWorkbenchAdapter() {
        this(true);
    }

    public AssociationWorkbenchAdapter(boolean showChangingOverTimeOverlay) {
        this.showChangingOverTimeOverlay = showChangingOverTimeOverlay;
    }

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
            IPolicyCmptTypeAssociation polAssociation = (IPolicyCmptTypeAssociation)association;
            if (polAssociation.isConfigurable()
                    && polAssociation.isConstrainedByProductStructure(association.getIpsProject())) {
                overlays[IDecoration.TOP_RIGHT] = OverlayIcons.PRODUCT_OVR;
            }
        }
        if (association instanceof IProductCmptTypeAssociation) {
            IProductCmptTypeAssociation productAssociation = (IProductCmptTypeAssociation)association;
            if (showChangingOverTimeOverlay && !productAssociation.isChangingOverTime()) {
                overlays[IDecoration.TOP_LEFT] = OverlayIcons.NOT_CHANGEOVERTIME_OVR;
            }
        }
        if (association.isConstrain()) {
            overlays[IDecoration.BOTTOM_RIGHT] = OverlayIcons.OVERRIDE_OVR;
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
