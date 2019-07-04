/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
