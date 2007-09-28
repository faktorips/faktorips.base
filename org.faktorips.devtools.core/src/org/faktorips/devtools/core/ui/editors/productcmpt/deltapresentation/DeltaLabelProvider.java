/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.product.DeltaType;
import org.faktorips.devtools.core.model.product.IDeltaEntry;

/**
 * 
 * @author Jan Ortmann
 */
public class DeltaLabelProvider extends LabelProvider {

    public DeltaLabelProvider() {
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        if (element instanceof DeltaType) {
            return ((DeltaType)element).getImage();
        }
        if (element instanceof IDeltaEntry) {
            IDeltaEntry entry = (IDeltaEntry)element;
            Image baseImage = entry.getPropertyType().getImage();
            if (entry.getDeltaType()==DeltaType.MISSING_PROPERTY_VALUE) {
                return DeltaCompositeIcon.createAddImage(baseImage);
            }
            if (entry.getDeltaType()==DeltaType.VALUE_WITHOUT_PROPERTY) {
                return DeltaCompositeIcon.createDeleteImage(baseImage);
            }
            return DeltaCompositeIcon.createModifyImage(baseImage);
        }
        return super.getImage(element);
    }

    /**
     * {@inheritDoc}
     */
    public String getText(Object element) {
        if (element instanceof DeltaType) {
            return ((DeltaType)element).getDescription();
        }
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDescription();
        }
        return super.getText(element);
    }

    
}
