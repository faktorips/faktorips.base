/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.HierarchyVisitor;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A specialization of <code>HierarchyVisitor</code> for <code>IEnumType</code>.
 * 
 * @author Peter Kuntz
 */
public abstract class EnumTypeHierachyVisitor extends HierarchyVisitor<IEnumType> {

    /**
     * Creates a new <code>EnumTypeHierachyVisitor</code>.
     * 
     * @param ipsProject The ips project which ips object path is used to search for enum types.
     */
    public EnumTypeHierachyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IEnumType findSupertype(IEnumType currentType, IIpsProject ipsProject) throws CoreException {
        return currentType.findSuperEnumType(ipsProject);
    }

}
