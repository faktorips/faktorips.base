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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public class TextButtonField extends DefaultEditField {
    
    TextButtonControl control;
    
    protected boolean immediatelyNotifyListener = false;

    public TextButtonField(TextButtonControl control) {
        super();
        this.control = control;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getControl()
     */
    public Control getControl() {
        return control;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setMessages(org.faktorips.util.message.MessageList)
     */
    public void setMessages(MessageList list) {
        MessageCueController.setMessageCue(control.getTextControl(), list);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getValue()
     */
    public Object getValue() {
    	return super.prepareObjectForGet(control.getTextControl().getText());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setValue(java.lang.Object)
     */
    public void setValue(Object newValue) {
        setText((String)super.prepareObjectForSet(newValue));
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getText()
     */
    public String getText() {
        return control.getTextControl().getText();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setText(java.lang.String)
     */
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            control.getTextControl().setText(newText);
        }
        finally {
            immediatelyNotifyListener = false;
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#insertText(java.lang.String)
     */
    public void insertText(String text) {
        control.getTextControl().insert(text);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#selectAll()
     */
    public void selectAll() {
        control.getTextControl().selectAll();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.fields.DefaultEditField#addListenerToControl()
     */
    protected void addListenerToControl() {
    	ModifyListener ml = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                boolean immediatelyNotify = immediatelyNotifyListener | control.isImmediatelyNotifyListener();
                notifyChangeListeners(new FieldValueChangedEvent(TextButtonField.this), immediatelyNotify);
            }
            
        };
        
        control.getTextControl().addModifyListener(ml);
        control.getTextControl().addDisposeListener(new MyDisposeListener(ml));
    }

    /**
     * Dispose listener to remove modify listener from control when control is disposed.
     * 
     * @author Thorsten Guenther
     */
    private class MyDisposeListener implements DisposeListener {
    	/**
    	 * Listener which has to be removed on dispose.
    	 */
    	private ModifyListener ml;
    	
    	/**
    	 * Create a new Listener.
    	 * 
    	 * @param ml The modify listener to remove on dispose
    	 */
    	MyDisposeListener(ModifyListener ml) {
    		this.ml = ml;
    	}
    	
		/**
		 * {@inheritDoc}
		 */
		public void widgetDisposed(DisposeEvent e) {
    		control.getTextControl().removeModifyListener(ml);
		}
    	
    }
    
}
