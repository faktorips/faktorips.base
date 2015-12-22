/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpt.template;

import org.faktorips.devtools.core.model.HierarchyVisitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;

public abstract class TemplateHierarchyVisitor extends HierarchyVisitor<IPropertyValueContainer> {

    public TemplateHierarchyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @Override
    protected IPropertyValueContainer findSupertype(IPropertyValueContainer currentType, IIpsProject ipsProject) {
        return currentType.findTemplate(ipsProject);
    }

}
