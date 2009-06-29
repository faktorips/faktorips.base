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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntryForProperty;
import org.faktorips.devtools.core.ui.editors.deltapresentation.DeltaCompositeIcon;

/**
 * 
 * @author Jan Ortmann
 */
public class DeltaLabelProvider extends LabelProvider {

    private List images = new ArrayList();
    
    public DeltaLabelProvider() {
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        if (element instanceof DeltaType) {
            return ((DeltaType)element).getImage();
        }
        if (element instanceof IDeltaEntryForProperty) {
            IDeltaEntryForProperty entry = (IDeltaEntryForProperty)element;
            Image baseImage = entry.getPropertyType().getImage();
            if (entry.getDeltaType()==DeltaType.MISSING_PROPERTY_VALUE) {
                return rememerImageForDispose(DeltaCompositeIcon.createAddImage(baseImage));
            }
            if (entry.getDeltaType()==DeltaType.VALUE_WITHOUT_PROPERTY) {
                return rememerImageForDispose(DeltaCompositeIcon.createDeleteImage(baseImage));
            }
            return rememerImageForDispose(DeltaCompositeIcon.createModifyImage(baseImage));
        }
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDeltaType().getImage();
        }
        return super.getImage(element);
    }
    
    private Image rememerImageForDispose(Image image) {
        images.add(image);
        return image;
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

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        for (Iterator it = images.iterator(); it.hasNext();) {
            Image image = (Image)it.next();
            image.dispose();
        }
    }
}
