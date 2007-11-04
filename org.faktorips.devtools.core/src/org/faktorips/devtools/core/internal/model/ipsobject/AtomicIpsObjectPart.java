/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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
    protected final void reAddPart(IIpsObjectPart part) {
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
