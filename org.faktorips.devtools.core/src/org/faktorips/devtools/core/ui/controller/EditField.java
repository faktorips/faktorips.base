package org.faktorips.devtools.core.ui.controller;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.util.message.MessageList;


/**
 *
 */
public interface EditField {
    
    /**
     * Returns the control this is a helper for.
     */
    public Control getControl();
    
    /**
     * Returns the value shown in the control. 
     */
    public Object getValue();
    
    /**
     * Sets the value shown in the control. 
     */
    public void setValue(Object newValue);
    
    /**
     * Sets the value shown in the control. 
     */
    public void setValue(Object newValue, boolean triggerValueChanged);
    
    /**
     * Returns the controls content as string or text value.
     */
    public abstract String getText();
    
    /**
     * Sets the controls content as string or text value.
     */
    public abstract void setText(String newText);
    
    /**
     * Inserts the text in the control.
     */
    public abstract void insertText(String text);
    
    /**
     * Selects all the text in the receiver.
     */ 
    public abstract void selectAll();
    
    /**
     * Returns true if the control's content can be returned as an instance
     * of the class, this is an edit contrl for. 
     */
    public boolean isTextContentParsable();
    
    /**
     * Sets the messages for the control.
     */
    public void setMessages(MessageList list);

    /**
     * Adds the value change listener.
     */
    public boolean addChangeListener(ValueChangeListener listener);
    
    /**
     * Removes the value change listener.
     */
    public boolean removeChangeListener(ValueChangeListener listener);

}
