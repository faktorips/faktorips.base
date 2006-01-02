package org.faktorips.devtools.core.ui.controller.fields;

import org.faktorips.devtools.core.ui.controller.EditField;


/**
 * An event that signals that the value in the edit field has been changed.
 * 
 * @author Jan Ortmann
 */
public class FieldValueChangedEvent {
    
    // the edit control that has changed
    public EditField field;

    public FieldValueChangedEvent(EditField field) {
        this.field = field;
    }

}
