/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;

/**
 * A ISearchOperator provides the core for the search
 * <p>
 * The method {@link #check(IProductPartsContainer)} returns whether the given
 * {@link IProductPartsContainer} is a hit of of the search.
 * 
 * @author dicker
 */
public interface ISearchOperator {

    /**
     * returns true, if the given {@link IProductPartsContainer} is a hit
     */
    public boolean check(IProductPartsContainer productPartsContainer);

}
