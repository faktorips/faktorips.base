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

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.faktorips.devtools.core.internal.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * A reference to unspecified objects (see subi nterfaces / -classes for further details) for used
 * in a <code>IProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptStructureReference extends IIpsSrcFileWrapper {

    /**
     * @return The <code>IProductCmptStructure</code> this reference belongs to.
     */
    public IProductCmptTreeStructure getStructure();

    /**
     * @return The <code>IIpsObject</code> referenced by this object or <code>null</code> if the
     *         referenced IPS object doesn't exists.
     */
    public IIpsObject getWrappedIpsObject();

    /**
     * Returns the parent structure object of <code>null</code> if this element is the root.
     */
    public IProductCmptStructureReference getParent();

    /**
     * @return The {@link IIpsObjectPart} referenced by this object.
     */
    public abstract IIpsObjectPart getWrapped();

    /**
     * Getting the direct children of this {@link IProductCmptStructureReference}.
     * 
     * @return The children of this reference
     */
    public abstract IProductCmptStructureReference[] getChildren();
}
