/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.deltapresentation.DeltaCompositeIcon;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.DeltaType.Kind;
import org.faktorips.devtools.model.productcmpt.IDeltaEntry;

/**
 * 
 */
public class DeltaLabelProvider extends LabelProvider {
    private WorkbenchLabelProvider workbenchLabelProvider;

    private ResourceManager resourceManager;

    public DeltaLabelProvider() {
        workbenchLabelProvider = new WorkbenchLabelProvider();
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public Image getImage(Object element) {
        ImageDescriptor descriptor = null;
        if (element instanceof DeltaTypeWrapper) {
            descriptor = getBaseImage(((DeltaTypeWrapper)element).getDeltaType());
        } else if (element instanceof IDeltaEntry) {
            descriptor = getImageDescriptorForDeltaEntry((IDeltaEntry)element);
        }
        return getImageForDescriptor(element, descriptor);
    }

    private ImageDescriptor getBaseImage(DeltaType deltaType) {
        return switch (deltaType) {
            case MISSING_PROPERTY_VALUE, VALUE_HOLDER_MISMATCH -> IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("DeltaTypeMissingPropertyValue.gif"); //$NON-NLS-1$
            case VALUE_WITHOUT_PROPERTY -> IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("DeltaTypeValueWithoutProperty.gif"); //$NON-NLS-1$
            case PROPERTY_TYPE_MISMATCH -> IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("DeltaTypePropertyTypeMismatch.gif"); //$NON-NLS-1$
            case VALUE_SET_MISMATCH -> IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("DeltaTypeValueSetMismatch.gif"); //$NON-NLS-1$
            case LINK_WITHOUT_ASSOCIATION -> IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("DeltaTypeLinkWithoutAssociation.gif"); //$NON-NLS-1$
            case HIDDEN_ATTRIBUTE_MISMATCH -> IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("DeltaTypeHiddenAttributeMismatch.gif"); //$NON-NLS-1$
            case INVALID_GENERATIONS -> IpsUIPlugin.getImageHandling()
                    .createImageDescriptor("DeltaTypeUnusedGeneration.gif"); //$NON-NLS-1$
            case LINK_CHANGING_OVER_TIME_MISMATCH, MISSING_TEMPLATE_LINK, REMOVED_TEMPLATE_LINK -> IIpsDecorators
                    .getDefaultImageDescriptor(ProductCmptTypeAssociation.class);
            default -> null;
        };
    }

    private ImageDescriptor getImageDescriptorForDeltaEntry(IDeltaEntry deltaEntry) {
        Image baseImage = getBaseImage(deltaEntry);
        return getOverlayedImageDescriptor(baseImage, deltaEntry.getDeltaType().getKind());
    }

    private Image getBaseImage(IDeltaEntry deltaEntry) {
        return IpsUIPlugin.getImageHandling().getDefaultImage(deltaEntry.getPartType());
    }

    private ImageDescriptor getOverlayedImageDescriptor(Image baseImage, DeltaType.Kind kind) {
        if (kind == Kind.ADD) {
            return DeltaCompositeIcon.createAddImage(baseImage);
        } else if (kind == Kind.DELETE) {
            return DeltaCompositeIcon.createDeleteImage(baseImage);
        } else {
            return DeltaCompositeIcon.createModifyImage(baseImage);
        }
    }

    private Image getImageForDescriptor(Object element, ImageDescriptor descriptor) {
        if (descriptor == null) {
            return getFallbackImage(element);
        } else {
            return resourceManager.get(descriptor);
        }
    }

    private Image getFallbackImage(Object element) {
        if (element instanceof ProductCmptGenerationsDeltaViewItem) {
            return workbenchLabelProvider.getImage(((ProductCmptGenerationsDeltaViewItem)element).getDelta());
        }
        return workbenchLabelProvider.getImage(element);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof DeltaTypeWrapper) {
            return ((DeltaTypeWrapper)element).getDeltaType().getDescription();
        }
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDescription();
        }
        if (element instanceof ProductCmptGenerationsDeltaViewItem) {
            return ((ProductCmptGenerationsDeltaViewItem)element).getDates();
        }
        return workbenchLabelProvider.getText(element);
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        workbenchLabelProvider.dispose();
    }

}
