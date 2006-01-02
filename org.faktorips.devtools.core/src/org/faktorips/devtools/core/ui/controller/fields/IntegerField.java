package org.faktorips.devtools.core.ui.controller.fields;

import org.apache.commons.lang.StringUtils;
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
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getControl()
     */
    public Control getControl() {
        return text;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getValue()
     */
    public Object getValue() {
        if (StringUtils.isEmpty(getText())) {
            throw new RuntimeException("Can't return an Integer, field is empty.");
        }
        return Integer.valueOf(getText());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setValue(java.lang.Object)
     */
    public void setValue(Object newValue) {
        ArgumentCheck.isInstanceOf(newValue, Integer.class);
        text.setText(newValue.toString());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getText()
     */
    public String getText() {
        return text.getText();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setText(java.lang.String)
     */
    public void setText(String newText) {
        text.setText(newText);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#insertText(java.lang.String)
     */
    public void insertText(String s) {
        text.insert(s);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#selectAll()
     */
    public void selectAll() {
        text.selectAll();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.fields.DefaultEditField#addListenerToControl()
     */
    protected void addListenerToControl() {
        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(IntegerField.this));
            }
            
        });
        
    }
}
