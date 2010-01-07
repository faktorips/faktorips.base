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

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.editors.deltapresentation.DeltaCompositeIcon;

/**
 * 
 * @author Jan Ortmann
 */
public class DeltaLabelProvider extends LabelProvider {

    private ResourceManager resourceManager;

    public DeltaLabelProvider() {
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage(Object element) {
        ImageDescriptor descriptor = ImageDescriptor.getMissingImageDescriptor();
        if (element instanceof DeltaType) {
            descriptor = getBaseImage(((DeltaType)element));
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
            descriptor = getBaseImage(((IDeltaEntry)element).getDeltaType());
        }
        return (Image)resourceManager.get(descriptor);
    }

    private Image getBaseImage(ProdDefPropertyType propertyType) {
        if (propertyType == ProdDefPropertyType.VALUE) {
            return (Image)resourceManager.get(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "ProductAttribute.gif"));
        } else if (propertyType == ProdDefPropertyType.TABLE_CONTENT_USAGE) {
            return (Image)resourceManager.get(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "TableContentsUsage.gif"));
        } else if (propertyType == ProdDefPropertyType.FORMULA) {
            return (Image)resourceManager.get(IpsUIPlugin.getImageHandling().createImageDescriptor("Formula.gif"));
        } else if (propertyType == ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET) {
            return (Image)resourceManager.get(IpsUIPlugin.getImageHandling().createImageDescriptor(
                    "PolicyAttribute.gif"));
        } else {
            return null;
        }
    }

    private ImageDescriptor getBaseImage(DeltaType deltaType) {
        if (deltaType == DeltaType.MISSING_PROPERTY_VALUE) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeMissingPropertyValue.gif");
        } else if (deltaType == DeltaType.VALUE_WITHOUT_PROPERTY) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeValueWithoutProperty.gif");
        } else if (deltaType == DeltaType.PROPERTY_TYPE_MISMATCH) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypePropertyTypeMismatch.gif");
        } else if (deltaType == DeltaType.VALUE_SET_MISMATCH) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeValueSetMismatch.gif");
        } else if (deltaType == DeltaType.LINK_WITHOUT_ASSOCIATION) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeLinkWithoutAssociation.gif");
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(Object element) {
        if (element instanceof DeltaType) {
            return ((DeltaType)element).getDescription();
        }
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDescription();
        }
        return super.getText(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        resourceManager.dispose();
    }
}
