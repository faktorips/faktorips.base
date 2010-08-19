/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

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

}
