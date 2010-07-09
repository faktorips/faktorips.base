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

import java.util.List;

import org.faktorips.runtime.caching.IComputable;
import org.faktorips.runtime.caching.Memoizer;

/**
 * A factory for creating caches used by the repository.
 * 
 * @author Jan Ortmann
 */
public interface ICacheFactory {

    /**
     * Creates a new cache of the given type.
     */
    public <K, V> Memoizer<K, V> createCache(IComputable<K, V> computable);

    public Memoizer<String, IProductComponent> createProductCmptCache(IComputable<String, IProductComponent> computable);

    public Memoizer<GenerationId, IProductComponentGeneration> createProductCmptGenerationCache(IComputable<GenerationId, IProductComponentGeneration> computable);

    public Memoizer<String, ITable> createTableCache(IComputable<String, ITable> computable);

    public Memoizer<Class<?>, List<?>> createEnumCache(IComputable<Class<?>, List<?>> computable);

}
