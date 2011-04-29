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

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        delta = (IGenerationToTypeDelta)newInput;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        List<DeltaType> elements = new ArrayList<DeltaType>();
        for (DeltaType element : DeltaType.values()) {
            if (delta.getEntries(element).length > 0) {
                elements.add(element);
            }
        }
        return elements.toArray();
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof DeltaType;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (!(parentElement instanceof DeltaType)) {
            return new Object[0];
        }
        return delta.getEntries((DeltaType)parentElement);
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDeltaType();
        }
        return delta;
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

}
