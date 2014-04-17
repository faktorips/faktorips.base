/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.LinkChangingOverTimeMismatchEntry;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.deltapresentation.DeltaCompositeIcon;

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
        } else if (element instanceof IDeltaEntryForProperty) {
            descriptor = getImageDescriptorForDeltaEntryForProperty((IDeltaEntryForProperty)element);
        } else if (element instanceof IDeltaEntry) {
            descriptor = getImageDescriptorForDeltaEntry((IDeltaEntry)element);
        }
        return getImageForDescriptor(element, descriptor);
    }

    private ImageDescriptor getBaseImage(DeltaType deltaType) {
        if (deltaType == DeltaType.MISSING_PROPERTY_VALUE) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeMissingPropertyValue.gif"); //$NON-NLS-1$
        } else if (deltaType == DeltaType.VALUE_WITHOUT_PROPERTY) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeValueWithoutProperty.gif"); //$NON-NLS-1$
        } else if (deltaType == DeltaType.PROPERTY_TYPE_MISMATCH) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypePropertyTypeMismatch.gif"); //$NON-NLS-1$
        } else if (deltaType == DeltaType.VALUE_SET_MISMATCH) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeValueSetMismatch.gif"); //$NON-NLS-1$
        } else if (deltaType == DeltaType.LINK_WITHOUT_ASSOCIATION) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeLinkWithoutAssociation.gif"); //$NON-NLS-1$
        } else if (deltaType == DeltaType.LINK_CHANGING_OVER_TIME_MISMATCH) {
            return IpsUIPlugin.getImageHandling().getDefaultImageDescriptor(ProductCmptTypeAssociation.class);
        } else if (deltaType == DeltaType.VALUE_HOLDER_MISMATCH) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeMissingPropertyValue.gif"); //$NON-NLS-1$
        } else if (deltaType == DeltaType.HIDDEN_ATTRIBUTE_MISMATCH) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeHiddenAttributeMismatch.gif"); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    private ImageDescriptor getImageDescriptorForDeltaEntryForProperty(IDeltaEntryForProperty entry) {
        ImageDescriptor descriptor;
        Image baseImage = getBaseImage(entry.getPropertyType());
        if (entry.getDeltaType() == DeltaType.MISSING_PROPERTY_VALUE) {
            descriptor = DeltaCompositeIcon.createAddImage(baseImage);
        } else if (entry.getDeltaType() == DeltaType.VALUE_WITHOUT_PROPERTY) {
            descriptor = DeltaCompositeIcon.createDeleteImage(baseImage);
        } else {
            descriptor = DeltaCompositeIcon.createModifyImage(baseImage);
        }
        return descriptor;
    }

    private Image getBaseImage(ProductCmptPropertyType propertyType) {
        return propertyType == null ? null : IpsUIPlugin.getImageHandling().getDefaultImage(
                propertyType.getValueImplementationClass());
    }

    private ImageDescriptor getImageDescriptorForDeltaEntry(IDeltaEntry deltaEntry) {
        ImageDescriptor baseImageDescriptor = getBaseImage(deltaEntry.getDeltaType());
        baseImageDescriptor = getChaningOvertimeDecorationIfRequired(deltaEntry, baseImageDescriptor);
        return baseImageDescriptor;
    }

    private ImageDescriptor getChaningOvertimeDecorationIfRequired(IDeltaEntry deltaEntry,
            ImageDescriptor baseImageDescriptor) {
        ImageDescriptor resultDescriptor = baseImageDescriptor;
        if (deltaEntry.getDeltaType() == DeltaType.LINK_CHANGING_OVER_TIME_MISMATCH) {
            LinkChangingOverTimeMismatchEntry mismatchEntry = (LinkChangingOverTimeMismatchEntry)deltaEntry;
            resultDescriptor = getChanginOverTimeMismatchDescriptor(mismatchEntry, baseImageDescriptor);
        }
        return resultDescriptor;
    }

    private ImageDescriptor getChanginOverTimeMismatchDescriptor(LinkChangingOverTimeMismatchEntry mismatchEntry,
            ImageDescriptor baseImageDescriptor) {
        Image baseImage = (Image)resourceManager.get(baseImageDescriptor);
        if (mismatchEntry.isMovingLink()) {
            return DeltaCompositeIcon.createModifyImage(baseImage);
        } else {
            return DeltaCompositeIcon.createDeleteImage(baseImage);
        }
    }

    private Image getImageForDescriptor(Object element, ImageDescriptor descriptor) {
        if (descriptor == null) {
            return getFallbackImage(element);
        } else {
            return (Image)resourceManager.get(descriptor);
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
