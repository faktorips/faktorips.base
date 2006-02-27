package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controls.Checkbox;


/**
 *
 */
public class CheckboxField extends DefaultEditField {
    
    private Checkbox checkbox;
    
    public CheckboxField(Checkbox checkbox) {
        this.checkbox = checkbox;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getControl()
     */
    public Control getControl() {
        return checkbox;
    }
    
    public Checkbox getCheckbox() {
        return checkbox;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getValue()
     */
    public Object getValue() {
        return new Boolean(checkbox.isChecked());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setValue(java.lang.Object)
     */
    public void setValue(Object newValue) {
        checkbox.setChecked(((Boolean)newValue).booleanValue());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#getText()
     */
    public String getText() {
        return checkbox.isChecked()?"true":"false"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#setText(java.lang.String)
     */
    public void setText(String newText) {
        checkbox.setChecked(Boolean.valueOf(newText).booleanValue());
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#insertText(java.lang.String)
     */
    public void insertText(String text) {
        // nothing to do
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.EditField#selectAll()
     */
    public void selectAll() {
        // nothing to do
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controller.fields.DefaultEditField#addListenerToControl()
     */
    protected void addListenerToControl() {
        checkbox.getButton().addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(CheckboxField.this));
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
            
        });
    }

}
