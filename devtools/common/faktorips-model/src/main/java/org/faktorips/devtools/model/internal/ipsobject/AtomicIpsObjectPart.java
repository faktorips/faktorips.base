/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.w3c.dom.Element;

/**
 * A part that does not contain any other parts (aside from descriptions and labels).
 * 
 * @author Jan Ortmann
 */
public abstract class AtomicIpsObjectPart extends IpsObjectPart {

    protected AtomicIpsObjectPart(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    protected AtomicIpsObjectPart() {
        super();
    }

    @Override
    public final IIpsElement[] getChildrenThis() {
        return new IIpsElement[0];
    }

    @Override
    protected final void reinitPartCollectionsThis() {
        // Nothing to do
    }

    @Override
    protected final boolean addPartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected final boolean removePartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected final IIpsObjectPart newPartThis(Element xmlTag, String id) {
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

}
