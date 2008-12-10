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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Stefan Widmaier
 */
public class TextCellEditor extends TableCellEditor {

    private Text textControl;
    
    public TextCellEditor(TableViewer tableViewer, int columnIndex, Text textControl){
        super(tableViewer, columnIndex, textControl);
        this.textControl= textControl;
    }
    
    /**
     * {@inheritDoc}
     */
    protected Object doGetValue() {
        return textControl.getText();
    }

    /**
     * {@inheritDoc}
     */
    protected void doSetFocus() {
        textControl.selectAll();
        textControl.setFocus();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void doSetValue(Object value) {
        if(value instanceof String){
            textControl.setText((String)value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMappedValue() {
        return false;
    }
}
