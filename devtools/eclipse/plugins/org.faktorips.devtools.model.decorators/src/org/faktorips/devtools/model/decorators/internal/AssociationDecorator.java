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
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.decorators.IIpsObjectPartDecorator;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;

public class AssociationDecorator implements IIpsObjectPartDecorator {

    public static final String ASSOCIATION_TYPE_COMPOSITION_IMAGE = "AssociationType-Composition.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE = "AssociationType-CompositionDetailToMaster.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_ASSOCIATION_IMAGE = "AssociationType-Association.gif"; //$NON-NLS-1$
    public static final String ASSOCIATION_TYPE_AGGREGATION_IMAGE = "AssociationType-Aggregation.gif"; //$NON-NLS-1$

    private final boolean showChangingOverTimeOverlay;

    public AssociationDecorator() {
        this(true);
    }

    public AssociationDecorator(boolean showChangingOverTimeOverlay) {
        this.showChangingOverTimeOverlay = showChangingOverTimeOverlay;
    }

    @Override
    public ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAssociation association) {
            String baseName = getImageBaseName(association);
            String[] overlays = getImageOverlays(association);
            return IIpsDecorators.getImageHandling().getSharedOverlayImageDescriptor(baseName, overlays);
        }
        return getDefaultImageDescriptor();
    }

    private String getImageBaseName(IAssociation association) {
        AssociationType associationType = association.getAssociationType();
        if (associationType != null) {
            return switch (associationType) {
                case AGGREGATION -> ASSOCIATION_TYPE_AGGREGATION_IMAGE;
                case ASSOCIATION -> ASSOCIATION_TYPE_ASSOCIATION_IMAGE;
                case COMPOSITION_DETAIL_TO_MASTER -> ASSOCIATION_TYPE_COMPOSITION_DETAIL_TO_MASTER_IMAGE;
                case COMPOSITION_MASTER_TO_DETAIL -> ASSOCIATION_TYPE_COMPOSITION_IMAGE;
            };
        }
        throw new IllegalArgumentException(
                association + " has unknown " + AssociationType.class.getSimpleName() + ": " + associationType); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String[] getImageOverlays(IAssociation association) {
        String[] overlays = new String[4];
        if (association instanceof IPolicyCmptTypeAssociation polAssociation) {
            if (polAssociation.isConfigurable()
                    && polAssociation.isConstrainedByProductStructure(association.getIpsProject())) {
                overlays[IDecoration.TOP_RIGHT] = OverlayIcons.PRODUCT_RELEVANT;
            }
        }
        if (association instanceof IProductCmptTypeAssociation productAssociation) {
            if (showChangingOverTimeOverlay && !productAssociation.isChangingOverTime()) {
                overlays[IDecoration.TOP_LEFT] = OverlayIcons.STATIC;
            }
        }
        if (association.isConstrain()) {
            overlays[IDecoration.BOTTOM_RIGHT] = OverlayIcons.OVERRIDE;
        }
        if (association.isDeprecated()) {
            overlays[IDecoration.BOTTOM_LEFT] = OverlayIcons.DEPRECATED;
        }
        return overlays;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IIpsDecorators.getImageHandling().getSharedImageDescriptor(ASSOCIATION_TYPE_ASSOCIATION_IMAGE, true);
    }

    @Override
    public String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAssociation association) {
            if (association.is1ToMany()) {
                return association.getTargetRolePlural();
            }
            return association.getTargetRoleSingular();
        } else {
            return IIpsObjectPartDecorator.super.getLabel(ipsObjectPart);
        }
    }

}
