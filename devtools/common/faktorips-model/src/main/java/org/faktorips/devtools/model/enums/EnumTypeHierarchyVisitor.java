/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.enums;

import org.faktorips.devtools.model.HierarchyVisitor;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A specialization of <code>HierarchyVisitor</code> for <code>IEnumType</code>.
 * 
 * @author Peter Kuntz
 */
public abstract class EnumTypeHierarchyVisitor extends HierarchyVisitor<IEnumType> {

    /**
     * Creates a new <code>EnumTypeHierachyVisitor</code>.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search for
     *            <code>IEnumType</code>s.
     */
    public EnumTypeHierarchyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @Override
    protected IEnumType findSupertype(IEnumType currentType, IIpsProject ipsProject) {
        return currentType.findSuperEnumType(ipsProject);
    }

}
