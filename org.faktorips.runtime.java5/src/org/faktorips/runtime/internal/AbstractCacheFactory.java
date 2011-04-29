/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.List;

import org.faktorips.runtime.GenerationId;
import org.faktorips.runtime.ICacheFactory;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.caching.IComputable;

public abstract class AbstractCacheFactory implements ICacheFactory {

    public AbstractCacheFactory() {
        super();
    }

    public IComputable<Class<?>, List<?>> createEnumCache(IComputable<Class<?>, List<?>> computable) {
        return createCache(computable);
    }

    public IComputable<String, IProductComponent> createProductCmptCache(IComputable<String, IProductComponent> computable) {
        return createCache(computable);
    }

    public IComputable<GenerationId, IProductComponentGeneration> createProductCmptGenerationCache(IComputable<GenerationId, IProductComponentGeneration> computable) {
        return createCache(computable);
    }

    public IComputable<String, ITable> createTableCache(IComputable<String, ITable> computable) {
        return createCache(computable);
    }

}
