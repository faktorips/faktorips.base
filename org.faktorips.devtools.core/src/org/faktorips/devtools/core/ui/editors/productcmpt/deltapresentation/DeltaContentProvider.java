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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;

/**
 * 
 * @author Jan Ortmann
 */
public class DeltaContentProvider implements ITreeContentProvider {

    private IGenerationToTypeDelta delta;
    
    public DeltaContentProvider() {
    }


    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        delta = (IGenerationToTypeDelta)newInput;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        List elements = new ArrayList();
        for (int i = 0; i < DeltaType.ALL_TYPES.length; i++) {
            if (delta.getEntries(DeltaType.ALL_TYPES[i]).length>0) {
                elements.add(DeltaType.ALL_TYPES[i]);
            }
        }
        return elements.toArray();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
        return element instanceof DeltaType;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (! (parentElement instanceof DeltaType)) {
            return new Object[0];
        }
        return delta.getEntries((DeltaType)parentElement);
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDeltaType();
        }
        return delta;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {

    }

}
