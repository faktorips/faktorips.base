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

package org.faktorips.runtime.productprovider;

/**
 * This is the abstract implementation for product data providers. For a description of product data
 * providers @see {@link IProductDataProvider}
 * 
 * @author dirmeier
 */
public abstract class AbstractProductDataProvider implements IProductDataProvider {

    protected static final String MODIFIED_EXCEPTION_MESSAGE = "Data has changed: ";

    public AbstractProductDataProvider() {
        super();
    }

    public boolean isExpired(long timestamp) {
        return getModificationStamp() != timestamp;
    }

}