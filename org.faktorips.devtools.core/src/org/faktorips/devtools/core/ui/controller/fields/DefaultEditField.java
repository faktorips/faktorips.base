package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public abstract class DefaultEditField implements EditField {
    
    private boolean notifyChangeListeners = true;
    private List changeListeners;

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
        for (Iterator it=changeListeners.iterator(); it.hasNext(); ) {
            ValueChangeListener listener = (ValueChangeListener)it.next();
            listener.valueChanged(e);
        }
    }
    
}
