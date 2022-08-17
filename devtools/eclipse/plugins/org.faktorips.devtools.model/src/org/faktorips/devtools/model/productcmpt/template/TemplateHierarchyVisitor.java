/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt.template;

import org.faktorips.devtools.model.HierarchyVisitor;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public abstract class TemplateHierarchyVisitor<T extends ITemplatedValueContainer> extends HierarchyVisitor<T> {

    public TemplateHierarchyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T findSupertype(ITemplatedValueContainer currentType, IIpsProject ipsProject) {
        return (T)currentType.findTemplate(ipsProject);
    }

}
