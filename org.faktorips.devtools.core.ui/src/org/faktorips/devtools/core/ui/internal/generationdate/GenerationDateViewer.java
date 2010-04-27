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

package org.faktorips.devtools.core.ui.internal.generationdate;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * A simple combo box viewer to view a list of generation dates
 * 
 * @author dirmeier
 */
public class GenerationDateViewer extends ComboViewer {

    public GenerationDateViewer(Composite parent) {
        super(parent, SWT.READ_ONLY);
    }

    public GenerationDate getSelectedDate() {
        if (getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)getSelection();
            if (structuredSelection.getFirstElement() instanceof GenerationDate) {
                return (GenerationDate)structuredSelection.getFirstElement();
            }
        }
        return null;
    }

    public void setSelection(int index) {
        Object o = getElementAt(index);
        setSelection(o);
    }

    public void setSelection(Object o) {
        setSelection(new StructuredSelection(o), true);
    }

}
