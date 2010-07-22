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
package org.faktorips.devtools.core.internal.model.businessfct;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.businessfct.BusinessFunction;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.w3c.dom.Element;

public class BusinessFunctionImpl extends IpsObject implements BusinessFunction {

    public BusinessFunctionImpl(IIpsSrcFile file) {
        super(file);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.BUSINESS_FUNCTION;
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        // Nothing to do.
    }

    @Override
    protected void reinitPartCollections() {
        // Nothing to do.
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        // Nothing to do.
    }

    /**
     * BusinessFunctions don't have any part, so this method should never be called.
     * 
     * @throws RuntimeException If the method is called.
     * 
     * @see org.faktorips.devtools.core.internal.model.ipsobject.IpsObject#newPart(Element, String)
     */
    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        throw new RuntimeException("newPart() not supported."); //$NON-NLS-1$
    }

    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        throw new IllegalArgumentException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

}
