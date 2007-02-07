/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.util.ArgumentCheck;

/**
 * Field for mapping a <code>GregorianCalendar</code> to an <code>Text</code>
 * and vice versa.
 * 
 * @author Thorsten Guenther
 */
public class GregorianCalendarField extends DefaultEditField {

    private Text text;
    
    public GregorianCalendarField(Text text) {
        super();
        ArgumentCheck.notNull(text);
        this.text = text;
    }
    
    /**
     * {@inheritDoc}
     */ 
    public Control getControl() {
        return text;
    }

    /**
     * {@inheritDoc}
     */ 
    public Object parseContent() {
    	String text = getText();
    	
        text = (String)super.prepareObjectForGet(text);
        if (text == null) {
        	return null;
        }
        
        try {
			Date date = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).parse(text);
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(date);
			return gc;
		} catch (ParseException e) {
            // the text is not parseable, return null as value,
            // because only valid GregorianCalendar values could be stored in the object,
            // note: the value which is visible in the ui control differs from the value which will
            // be set in the object
            return null;
		}
    }

    /**
     * {@inheritDoc}
     */ 
    public void setValue(Object newValue) {
        ArgumentCheck.isInstanceOf(newValue, GregorianCalendar.class);
        newValue = super.prepareObjectForSet(newValue);
        
        if (newValue instanceof GregorianCalendar) {
        	newValue = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(((GregorianCalendar)newValue).getTime());
        }
        text.setText(newValue.toString());
    }

    /**
     * {@inheritDoc}
     */ 
    public String getText() {
        return text.getText();
    }

    /**
     * {@inheritDoc}
     */ 
    public void setText(String newText) {
        text.setText(newText);
    }

    /**
     * {@inheritDoc}
     */ 
    public void insertText(String s) {
        text.insert(s);
    }

    /**
     * {@inheritDoc}
     */ 
    public void selectAll() {
        text.selectAll();
    }

    /**
     * {@inheritDoc}
     */ 
    protected void addListenerToControl() {
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(GregorianCalendarField.this));
            }
            
        });
        
    }
}
