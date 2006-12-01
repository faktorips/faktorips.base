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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public abstract class DefaultEditField implements EditField {
    
    private boolean notifyChangeListeners = true;
    private List changeListeners;
    private boolean supportNull = true;
    
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#isTextContentParsable()
     */
    public boolean isTextContentParsable() {
        try {
            getValue();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setMessages(org.faktorips.util.message.MessageList)
     */
    public void setMessages(MessageList list) {
        MessageCueController.setMessageCue(getControl(), list);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#addChangeListener(org.faktorips.devtools.core.ui.controls.ValueChangeListener)
     */
    public boolean addChangeListener(ValueChangeListener listener) {
        if (changeListeners==null) {
            changeListeners = new ArrayList(1);
        }
        boolean added = changeListeners.add(listener);
        if (added && changeListeners.size()==1) {
            addListenerToControl();
        }
        return added;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#removeChangeListener(org.faktorips.devtools.core.ui.controls.ValueChangeListener)
     */
    public boolean removeChangeListener(ValueChangeListener listener) {
        if (changeListeners==null) {
            return false;
        }
        return changeListeners.remove(listener);
    }
    
    /**
     * If the first change listener is added to the edit field, the edit field
     * itself has to listen to changes in it's control.
     */
    protected abstract void addListenerToControl();
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setValue(java.lang.Object, boolean)
     */
    public void setValue(Object newValue, boolean triggerValueChanged) {
        notifyChangeListeners = triggerValueChanged;
        try {
            setValue(newValue);
        } catch (Exception e){
            IpsPlugin.log(e);
        } finally {
            notifyChangeListeners = true;
        }
    }
    
    /**
     * Sends the given event to all registered listeners. 
     */
    protected void notifyChangeListeners(FieldValueChangedEvent e) {
        if (!notifyChangeListeners) {
            return;
        }

        // notify change listeners in delayed manner
        IpsPlugin.getDefault().getEditFieldChangeBroadcaster().broadcastDelayed(e,
                (ValueChangeListener[])changeListeners.toArray(new ValueChangeListener[changeListeners.size()]));
    }
 
    /**
     * Returns the null-representation-string defined by the user (see IpsPreferences)
     * if the given object is null, the unmodified object otherwise.
     */
    Object prepareObjectForSet(Object object) {
    	if (object == null && supportNull) {
    		return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
    	}
    	return object;
    }

    /**
     * Returns null if the given value is the null-representation-string, 
     * the unmodified value otherwise.
     */
    Object prepareObjectForGet(Object value) {
    	if (supportNull && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(value)) {
    		return null;
    	}
    	return value;
    }
    
    /**
     * <code>true</code> to activate null-handling (which means that a null-object
     * is transformed to the user defined null-representations-string and vice versa) or
     * <code>false</code> to deactivate null-handling.
     */
    public void setSupportsNull(boolean supportsNull) {
    	this.supportNull = supportsNull;
    }
}
