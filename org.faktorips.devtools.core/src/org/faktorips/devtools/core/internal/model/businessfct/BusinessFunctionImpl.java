/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.businessfct;

import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.businessfct.BusinessFunction;
import org.w3c.dom.Element;


/**
 *
 */
public class BusinessFunctionImpl extends IpsObject implements
        BusinessFunction {

    public BusinessFunctionImpl(IIpsSrcFile file) {
        super(file);
    }

    /** 
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.BUSINESS_FUNCTION;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element newElement) {
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
    }

    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
    }

    /**
     * Overridden IMethod. 
     * 
     * BusinessFunctions don't have any part, so this method should never be called.
     * 
     * @throws RuntimeException if the method is called.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObject#newPart(java.lang.String, int)
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        throw new RuntimeException("newPart() not supported."); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        throw new IllegalArgumentException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
}
