package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;


/**
 * A field for object references.
 */
public class PdObjectField extends TextButtonField {

    /**
     * @param control
     */
    public PdObjectField(TextButtonControl control) {
        super(control);
    }

    /**
     * Returns the datatype if possible, or null if the current value in the
     * control does not specifiy a datatype. 
     */
    public IIpsObject getPdObject(IIpsProject project, IpsObjectType type) throws CoreException {
        return project.findIpsObject(type, getText());
    }
    
}
