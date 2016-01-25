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
import org.faktorips.devtools.core.model.productcmpt.ITemplatedPropertyContainer;

public abstract class TemplateHierarchyVisitor<T extends ITemplatedPropertyContainer> extends HierarchyVisitor<T> {

    public TemplateHierarchyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T findSupertype(ITemplatedPropertyContainer currentType, IIpsProject ipsProject) {
        return (T)currentType.findTemplate(ipsProject);
    }

}
