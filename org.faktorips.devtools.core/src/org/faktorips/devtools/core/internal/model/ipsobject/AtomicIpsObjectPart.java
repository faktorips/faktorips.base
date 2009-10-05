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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.w3c.dom.Element;

/**
 * A part that does not contain any other parts.
 * 
 * @author Jan Ortmann
 */
public abstract class AtomicIpsObjectPart extends IpsObjectPart {

    public AtomicIpsObjectPart(IIpsObject parent, int id) {
        super(parent, id);
    }

    public AtomicIpsObjectPart(IIpsObjectPart parent, int id) {
        super(parent, id);
    }

    public AtomicIpsObjectPart() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    public final IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     */
    public final IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknwon type " + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     */
    protected final IIpsObjectPart newPart(Element xmlTag, int id) {
        return null;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     */
    protected final void addPart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     */
    protected final void removePart(IIpsObjectPart part) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    protected final void reinitPartCollections() {
        // nothing to do
    }

}
