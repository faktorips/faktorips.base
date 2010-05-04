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
 * Default cache factory. Uses SoftReferenceCaches for each object type.
 * 
 * @author Jan Ortmann
 */
public class DefaultCacheFactory implements ICacheFactory {
    private int initialCapacityForProductCmpts = 500;
    private int initialCapacityForProductCmptGenerations = 5000;
    private int initialCapacityForTablesByClassname = 100;
    private int initialCapacityForTablesByQname = 100;
    private int initialCapacityForEnumContentByClassName = 100;

    public DefaultCacheFactory() {
        // do nothing
    }

    public DefaultCacheFactory(int initialCapacityForProductCmpts, int initialCapacityForProductCmptGenerations,
            int initialCapacityForTablesByClassname, int initialCapacityForTablesByQname,
            int initialCapacityForEnumContentByClassName) {
        super();
        this.initialCapacityForProductCmpts = initialCapacityForProductCmpts;
        this.initialCapacityForProductCmptGenerations = initialCapacityForProductCmptGenerations;
        this.initialCapacityForTablesByClassname = initialCapacityForTablesByClassname;
        this.initialCapacityForTablesByQname = initialCapacityForTablesByQname;
        this.initialCapacityForEnumContentByClassName = initialCapacityForEnumContentByClassName;
    }

    /**
     * {@inheritDoc}
     */
    public <T> ICache<T> createCache(Type type, Class<T> typeClass) {
        switch (type) {
            case PRODUCT_CMPT_CHACHE:
                return new SoftReferenceCache<T>(initialCapacityForProductCmpts);
            case PRODUCT_CMPT_GENERATION_CHACHE:
                return new SoftReferenceCache<T>(initialCapacityForProductCmptGenerations);
            case TABLE_BY_CLASSNAME_CACHE:
                return new SoftReferenceCache<T>(initialCapacityForTablesByClassname);
            case TABLE_BY_QUALIFIED_NAME_CACHE:
                return new SoftReferenceCache<T>(initialCapacityForTablesByQname);
            case ENUM_CONTENT_BY_CLASS:
                return new SoftReferenceCache<T>(initialCapacityForEnumContentByClassName);
        }
        throw new IllegalArgumentException("Unknown cache type " + type);
    }
}
