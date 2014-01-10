/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.filter;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * {@link IIpsElementFilter}, which accepts all {@link IIpsElement}s within the given
 * {@link IIpsProject}
 * 
 * @author dicker
 */
public class IpsObjectInProjectFilter implements IIpsElementFilter {

    private final IIpsProject ipsProject;

    public IpsObjectInProjectFilter(IIpsProject ipsProject) {
        super();
        this.ipsProject = ipsProject;
    }

    @Override
    public boolean accept(IIpsElement element) {
        return ipsProject.equals(element.getIpsProject());
    }

}
