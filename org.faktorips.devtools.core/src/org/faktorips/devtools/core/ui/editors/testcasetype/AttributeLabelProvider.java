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

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * Attribute label provider with optional policy cmpt type name beside the attributes name.
 * 
 * @author Joerg Ortmann
 */
public class AttributeLabelProvider extends DefaultLabelProvider {
    private boolean showPolicyCmptTypeName;
    
    /**
     * {@inheritDoc}
     */
    public String getText(Object element) {
        IAttribute attribute = (IAttribute) element;
        
        if (!showPolicyCmptTypeName){
            return super.getText(element);
        }
        IIpsElement policyCmptType = attribute.getParent();
        return super.getText(element) + " - " + policyCmptType.getName(); //$NON-NLS-1$
    }

    /**
     * @param showPolicyCmptTypeName The showPolicyCmptTypeName to set.
     */
    public void setShowPolicyCmptTypeName(boolean showPolicyCmptTypeName) {
        this.showPolicyCmptTypeName = showPolicyCmptTypeName;
    }
}
