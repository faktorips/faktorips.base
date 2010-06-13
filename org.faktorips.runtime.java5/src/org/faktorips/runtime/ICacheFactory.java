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

package org.faktorips.runtime;

/**
 * A factory for creating caches used by the repository.
 * 
 * @author Jan Ortmann
 */
public interface ICacheFactory {

    /**
     * Creates a new cache of the given type.
     */
    public <T> ICache<T> createCache(Class<T> typeClass);

    public ICache<IProductComponent> createProductCmptCache();

    public ICache<IProductComponentGeneration> createProductCmptGenerationCache();

    public ICache<ITable> createTableCache();

}
