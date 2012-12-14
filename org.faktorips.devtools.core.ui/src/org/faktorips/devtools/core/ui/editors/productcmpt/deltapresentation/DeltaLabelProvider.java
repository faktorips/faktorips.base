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
 * @author Jan Ortmann
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
            descriptor = getBaseImage(((DeltaTypeWrapper)element).type);
        } else if (element instanceof IDeltaEntryForProperty) {
            IDeltaEntryForProperty entry = (IDeltaEntryForProperty)element;
            Image baseImage = getBaseImage(entry.getPropertyType());
            if (entry.getDeltaType() == DeltaType.MISSING_PROPERTY_VALUE) {
                descriptor = DeltaCompositeIcon.createAddImage(baseImage);
            } else if (entry.getDeltaType() == DeltaType.VALUE_WITHOUT_PROPERTY) {
                descriptor = DeltaCompositeIcon.createDeleteImage(baseImage);
            } else {
                descriptor = DeltaCompositeIcon.createModifyImage(baseImage);
            }
        } else if (element instanceof IDeltaEntry) {
            descriptor = getImageDescriptorForDeltaEntry((IDeltaEntry)element);
        }
        if (descriptor == null) {
            if (element instanceof ProductCmptGenerationToTypeDeltaWrapper) {
                return workbenchLabelProvider.getImage(((ProductCmptGenerationToTypeDeltaWrapper)element).getDelta());
            }
            return workbenchLabelProvider.getImage(element);
        }
        return (Image)resourceManager.get(descriptor);
    }

    private ImageDescriptor getImageDescriptorForDeltaEntry(IDeltaEntry deltaEntry) {
        ImageDescriptor baseImageDescriptor = getBaseImage(deltaEntry.getDeltaType());
        if (deltaEntry.getDeltaType() == DeltaType.LINK_CHANGING_OVER_TIME_MISMATCH) {
            LinkChangingOverTimeMismatchEntry mismatchEntry = (LinkChangingOverTimeMismatchEntry)deltaEntry;
            Image baseImage = (Image)resourceManager.get(baseImageDescriptor);
            if (mismatchEntry.isMovingLink()) {
                return DeltaCompositeIcon.createModifyImage(baseImage);
            } else {
                return DeltaCompositeIcon.createDeleteImage(baseImage);
            }
        }
        return baseImageDescriptor;
    }

    private Image getBaseImage(ProductCmptPropertyType propertyType) {
        if (propertyType == ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE) {
            return getImageForName("ProductAttribute.gif"); //$NON-NLS-1$
        } else if (propertyType == ProductCmptPropertyType.TABLE_STRUCTURE_USAGE) {
            return getImageForName("TableContentsUsage.gif"); //$NON-NLS-1$
        } else if (propertyType == ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION) {
            return getImageForName("Formula.gif"); //$NON-NLS-1$
        } else if (propertyType == ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE) {
            return getImageForName("PolicyAttribute.gif"); //$NON-NLS-1$
        } else if (propertyType == ProductCmptPropertyType.VALIDATION_RULE) {
            return getImageForName("ValidationRuleDef.gif"); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    private Image getImageForName(String fileName) {
        return (Image)resourceManager.get(IpsUIPlugin.getImageHandling().createImageDescriptor(fileName));
    }

    private ImageDescriptor getBaseImage(DeltaType deltaType) {
        // TODO SW 17.5.11 use part adapters for base images
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
        } else {
            return null;
        }
    }

    @Override
    public String getText(Object element) {
        if (element instanceof DeltaTypeWrapper) {
            return ((DeltaTypeWrapper)element).type.getDescription();
        }
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDescription();
        }
        if (element instanceof ProductCmptGenerationToTypeDeltaWrapper) {
            return ((ProductCmptGenerationToTypeDeltaWrapper)element).getDates();
        }
        return workbenchLabelProvider.getText(element);
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
        workbenchLabelProvider.dispose();
    }

}
