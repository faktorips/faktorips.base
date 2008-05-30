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
    private int initialCapacityForTablesByQname= 100;
    
    public DefaultCacheFactory() {
    }
    
    public DefaultCacheFactory(int initialCapacityForProductCmpts, int initialCapacityForProductCmptGenerations,
            int initialCapacityForTablesByClassname, int initialCapacityForTablesByQname) {
        super();
        this.initialCapacityForProductCmpts = initialCapacityForProductCmpts;
        this.initialCapacityForProductCmptGenerations = initialCapacityForProductCmptGenerations;
        this.initialCapacityForTablesByClassname = initialCapacityForTablesByClassname;
        this.initialCapacityForTablesByQname = initialCapacityForTablesByQname;
    }

    /**
     * {@inheritDoc}
     */
    public ICache createCache(Type type) {
        switch (type) {
            case PRODUCT_CMPT_CHACHE:
                return new SoftReferenceCache(initialCapacityForProductCmpts);
            case PRODUCT_CMPT_GENERATION_CHACHE:
                return new SoftReferenceCache(initialCapacityForProductCmptGenerations);
            case TABLE_BY_CLASSNAME_CACHE:
                return new SoftReferenceCache(initialCapacityForTablesByClassname);
            case TABLE_BY_QUALIFIED_NAME_CACHE:
                return new SoftReferenceCache(initialCapacityForTablesByQname);
        }
        throw new IllegalArgumentException("Unknown cache type " + type);
    }
}
