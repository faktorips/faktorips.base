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

    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.BUSINESS_FUNCTION;
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    @Override
    protected void propertiesToXml(Element newElement) {

    }

    @Override
    protected void reinitPartCollections() {
    }

    @Override
    protected void addPart(IIpsObjectPart part) {

    }

    /**
     * Overridden Method.
     * 
     * BusinessFunctions don't have any part, so this method should never be called.
     * 
     * @throws RuntimeException if the method is called.
     * 
     * @see org.faktorips.devtools.core.internal.model.ipsobject.IpsObject#newPart(java.lang.String,
     *      int)
     */
    @Override
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        throw new RuntimeException("newPart() not supported."); //$NON-NLS-1$
    }

    public IIpsObjectPart newPart(Class<?> partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        throw new IllegalArgumentException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

}
