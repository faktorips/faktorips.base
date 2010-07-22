/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.eclipse.core.runtime.IAdaptable;
import org.faktorips.devtools.core.internal.model.adapter.IIpsSrcFileWrapper;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * A reference to unspecified objects (see subi nterfaces / -classes for further details) for used
 * in a <code>IProductCmptStructure</code>.
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptStructureReference extends IAdaptable, IIpsSrcFileWrapper {

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

}
