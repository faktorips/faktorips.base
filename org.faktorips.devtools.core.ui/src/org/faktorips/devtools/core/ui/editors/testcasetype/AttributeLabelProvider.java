/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

/**
 * Attribute label provider with optional policy cmpt type name beside the attributes name.
 * 
 * @author Joerg Ortmann
 */
public class AttributeLabelProvider extends DefaultLabelProvider {

    private boolean showPolicyCmptTypeName;

    @Override
    public String getText(Object element) {
        IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)element;

        if (!showPolicyCmptTypeName) {
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
