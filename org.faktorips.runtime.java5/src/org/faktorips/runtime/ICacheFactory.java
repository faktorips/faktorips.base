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

    public enum Type {
        PRODUCT_CMPT_CHACHE,
        PRODUCT_CMPT_GENERATION_CHACHE,
        TABLE_BY_CLASSNAME_CACHE,
        TABLE_BY_QUALIFIED_NAME_CACHE,
        ENUM_CONTENT_BY_CLASS,
        ENUM_XML_ADAPTER_BY_QUALIFIED_NAME;
    }

    /**
     * Creates a new cache of the given type.
     * 
     * @see Type#PRODUCT_CMPT_CHACHE
     * @see Type#PRODUCT_CMPT_GENERATION_CHACHE
     * @see Type#TABLE_BY_CLASSNAME_CACHE
     * @see Type#TABLE_BY_QUALIFIED_NAME_CACHE
     * @see Type#ENUM_CONTENT_BY_CLASS
     */
    public ICache<?> createCache(Type type);
}
