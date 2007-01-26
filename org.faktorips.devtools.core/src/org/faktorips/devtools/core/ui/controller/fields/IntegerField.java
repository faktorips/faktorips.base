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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class IntegerField extends DefaultEditField {

    private Text text;
    
    public IntegerField(Text text) {
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
    public Object getValue() {
    	String text = getText();
        if (text != null && text.length() == 0) {
            throw new RuntimeException("Can't return an Integer, field is empty."); //$NON-NLS-1$
        }
        text = (String)super.prepareObjectForGet(text);
        if (text == null) {
        	return null;
        }
        return Integer.valueOf(text);
    }

    /** 
     * {@inheritDoc}
     */
    public void setValue(Object newValue) {
        ArgumentCheck.isInstanceOf(newValue, Integer.class);
        newValue = super.prepareObjectForSet(newValue);
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
                notifyChangeListeners(new FieldValueChangedEvent(IntegerField.this));
            }
            
        });
        
    }
}
