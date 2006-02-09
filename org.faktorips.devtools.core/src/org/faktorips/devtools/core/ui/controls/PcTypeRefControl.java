package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control to edit policy component type references.  
 */
public class PcTypeRefControl extends IpsObjectRefControl {

    public PcTypeRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, "Type Selection", "Select a type (?=any character, *=any string");
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.IpsObjectRefControl#getPdObjects()
     */
    protected IIpsObject[] getPdObjects() throws CoreException {
        return getPdProject().findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE);
    }
    
    /**
     * Returns the policy component type identified by the qualified name found in this control's text value.
     * Returns <code>null</code> if the text value does not identify a policy component type.
     * 
     * @throws CoreException if an error occurs while searching for the type.
     */
    public IPolicyCmptType findPcType() throws CoreException {
        IIpsProject project = getPdProject();
        if (project==null) {
            return null;
        }
        return project.findPolicyCmptType(getText());
    }

}
