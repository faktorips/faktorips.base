/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.HierarchyVisitor;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A specialization of <tt>HierarchyVisitor</tt> for <tt>IEnumType</tt>.
 * 
 * @author Peter Kuntz
 */
public abstract class EnumTypeHierachyVisitor extends HierarchyVisitor<IEnumType> {

    /**
     * Creates a new <tt>EnumTypeHierachyVisitor</tt>.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search for
     *            <tt>IEnumType</tt>s.
     */
    public EnumTypeHierachyVisitor(IIpsProject ipsProject) {
        super(ipsProject);
    }

    @Override
    protected IEnumType findSupertype(IEnumType currentType, IIpsProject ipsProject) throws CoreException {
        return currentType.findSuperEnumType(ipsProject);
    }

}
