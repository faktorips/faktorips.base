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

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.message.MessageList;


// TODO Roman: javadoc
public abstract class CompositeFactory implements ValueChangeListener {

    
    private ArrayList listeners = new ArrayList();

    
    public abstract Composite createPropertyComposite(Composite parent, UIToolkit toolkit);
    public abstract MessageList validate();

    public abstract void setTableFormat(ITableFormat tableFormat);
    

    public void addValueChangedListener(ValueChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }
        
    public void removeValueChangedListener(ValueChangeListener listener) {
        listeners.remove(listener);
    }

    public void valueChanged(FieldValueChangedEvent e) {
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext()) {
            ((ValueChangeListener) iterator.next()).valueChanged(e);
        }
    }
}
