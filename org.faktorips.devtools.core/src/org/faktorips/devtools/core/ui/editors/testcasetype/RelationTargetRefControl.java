/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;

/**
 * Control to select a specific target of relation.
 * 
 * @author Joerg Ortmann
 */
public class RelationTargetRefControl extends PcTypeRefControl {
    private IPolicyCmptType policyCmptTypeTarget;
    
    public RelationTargetRefControl(IIpsProject project, Composite parent, UIToolkit toolkit,
            IPolicyCmptType policyCmptTypeTarget) {
        super(project, parent, toolkit);
        
        this.policyCmptTypeTarget = policyCmptTypeTarget;
    }

    public void setPolicyCmptTypeTarget(IPolicyCmptType policyCmptTypeTarget) {
        this.policyCmptTypeTarget = policyCmptTypeTarget;
    }

    /** 
     * {@inheritDoc}
     */
    protected IIpsObject[] getIpsObjects() throws CoreException {
        if (policyCmptTypeTarget == null)
            return new IIpsObject[0];
        
        // find all policy component of the given type (incl. subclasses) (the result could be candidates for the
        // target policy cmpt type of the relation)
        // when defining the test case subclasses of the current policy cmpt target could be assigned 
        // as target for the relations target
        // Remark: this is a operation in the ui, therefore it is acceptable if the operation takes a long
        // time, because the user has triggered this operation to chose a policy cmpt from this selection
        // of policy cmpt types
        ITypeHierarchy subTypeHierarchy = policyCmptTypeTarget.getSubtypeHierarchy();
        IPolicyCmptType[] subTypes = subTypeHierarchy.getAllSubtypes(policyCmptTypeTarget);
        if (subTypes == null)
            subTypes = new IPolicyCmptType[0];
        IIpsObject[] policyCmptTypes = new IIpsObject[subTypes.length + 1];
        System.arraycopy(subTypes, 0, policyCmptTypes, 0, subTypes.length);
        policyCmptTypes[subTypes.length] = policyCmptTypeTarget;
        return policyCmptTypes;
    }
}
