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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 *
 */
public class ProductCmptRefControl extends IpsObjectRefControl {

    private String qualifiedTypeName;
    private boolean includeCmptsForSubtypes = true;
    
    
    public ProductCmptRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.ProductCmptRefControl_title, Messages.ProductCmptRefControl_description);
    }
    
    /**
     * @param qPcTypeNmae The policy component type for which product components should be selectable.
     * @param includeCmptsForSubtypes <code>true</code> if also product components for subtypes should be selectable.
     */
    public void setPolicyCmptType(String qTypeName, boolean includeCmptsForSubtypes) {
        this.qualifiedTypeName = qTypeName;
        this.includeCmptsForSubtypes = includeCmptsForSubtypes;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.IpsObjectRefControl#getPdObjects()
     */
    protected IIpsObject[] getPdObjects() throws CoreException {
        return getPdProject().findProductCmpts(qualifiedTypeName, includeCmptsForSubtypes);
    }

}
