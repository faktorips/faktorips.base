/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.ConfigWithoutValidationRuleEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.MissingValidationRuleConfigEntry;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
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

    @Override
    public Image getImage(Object element) {
        ImageDescriptor descriptor = ImageDescriptor.getMissingImageDescriptor();
        if (element instanceof DeltaType) {
            descriptor = getBaseImage(((DeltaType)element));
        } else if (element instanceof IDeltaEntryForProperty) {
            IDeltaEntryForProperty entry = (IDeltaEntryForProperty)element;
            ImageDescriptor baseImage = getBaseImage(entry.getPropertyType());
            if (entry.getDeltaType() == DeltaType.MISSING_PROPERTY_VALUE) {
                descriptor = DeltaCompositeIcon.createAddImage(baseImage, resourceManager);
            } else if (entry.getDeltaType() == DeltaType.VALUE_WITHOUT_PROPERTY) {
                descriptor = DeltaCompositeIcon.createDeleteImage(baseImage, resourceManager);
            } else {
                descriptor = DeltaCompositeIcon.createModifyImage(baseImage, resourceManager);
            }
        } else if (element instanceof IDeltaEntry) {
            IDeltaEntry entry = (IDeltaEntry)element;
            descriptor = getBaseImage(entry);
            if (entry.getDeltaType() == DeltaType.MISSING_VALIDATION_RULE_CONFIG) {
                descriptor = DeltaCompositeIcon.createAddImage(descriptor, resourceManager);
            } else if (entry.getDeltaType() == DeltaType.CONFIG_WITHOUT_VALIDATION_RULE) {
                descriptor = DeltaCompositeIcon.createDeleteImage(descriptor, resourceManager);
            }
        }
        return (Image)resourceManager.get(descriptor);
    }

    private ImageDescriptor getBaseImage(ProdDefPropertyType propertyType) {
        if (propertyType == ProdDefPropertyType.VALUE) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("ProductAttribute.gif"); //$NON-NLS-1$
        } else if (propertyType == ProdDefPropertyType.TABLE_CONTENT_USAGE) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("TableContentsUsage.gif"); //$NON-NLS-1$
        } else if (propertyType == ProdDefPropertyType.FORMULA) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("Formula.gif"); //$NON-NLS-1$
        } else if (propertyType == ProdDefPropertyType.DEFAULT_VALUE_AND_VALUESET) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("PolicyAttribute.gif"); //$NON-NLS-1$
        } else {
            return null;
        }
    }

    /**
     * Returns the image descriptor for an {@link IDeltaEntry} or child element in the delta-tree.
     * Returns the image of the delta type ({@link #getBaseImage(DeltaType)}) per default.
     */
    private ImageDescriptor getBaseImage(IDeltaEntry deltaEntry) {
        if (deltaEntry.getDeltaType() == DeltaType.CONFIG_WITHOUT_VALIDATION_RULE) {
            IValidationRuleConfig config = ((ConfigWithoutValidationRuleEntry)deltaEntry).getValidationRuleConfig();
            return IpsUIPlugin.getImageHandling().getImageDescriptor(config);
        } else if (deltaEntry.getDeltaType() == DeltaType.MISSING_VALIDATION_RULE_CONFIG) {
            IValidationRule rule = ((MissingValidationRuleConfigEntry)deltaEntry).getValidationRule();
            return IpsUIPlugin.getImageHandling().getImageDescriptor(rule);
        } else {
            return getBaseImage(deltaEntry.getDeltaType());
        }
    }

    /**
     * Returns the image descriptor for a {@link DeltaType} or root element in the delta-tree.
     */
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
        } else if (deltaType == DeltaType.MISSING_VALIDATION_RULE_CONFIG) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeValidationRuleMismatch.gif"); //$NON-NLS-1$
        } else if (deltaType == DeltaType.CONFIG_WITHOUT_VALIDATION_RULE) {
            return IpsUIPlugin.getImageHandling().createImageDescriptor("DeltaTypeValidationRuleMismatch.gif"); //$NON-NLS-1$
        } else {
            return null;
        }
    }

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

    @Override
    public void dispose() {
        resourceManager.dispose();
    }

}
