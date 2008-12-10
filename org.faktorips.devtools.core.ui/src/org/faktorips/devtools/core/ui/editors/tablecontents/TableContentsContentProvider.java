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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;

public class TableContentsContentProvider implements IStructuredContentProvider{

    public TableContentsContentProvider(){
    }
    
    public Object[] getElements(Object inputElement) {
        if(inputElement instanceof ITableContents){
            ITableContents table= (ITableContents) inputElement;
            return ((ITableContentsGeneration) table.getFirstGeneration()).getRows();
        }
        return new Object[0];
    }

    /**
     * Empty implementation.
     * {@inheritDoc}
     */
    public void dispose() {
    }

    /**
     * Empty implementation.
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }


}
