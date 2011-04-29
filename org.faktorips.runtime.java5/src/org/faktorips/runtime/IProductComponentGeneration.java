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

package org.faktorips.runtime;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A product component generation represents the state of a product component valid for a period of
 * time. The period's begins is defined by the generation's valid from date. The period ends at the
 * next generation's valid from date. A product component's generation periods are none overlapping.
 * For a given point in time exactly one (or none) generation is found.
 * 
 * @author Jan Ortmann
 */
public interface IProductComponentGeneration extends IRuntimeObject {

    /**
     * Returns the repository this product component generation belongs to. This method never
     * returns <code>null</code>.
     */
    public IRuntimeRepository getRepository();

    /**
     * Returns the product component this generation belongs to. This method never returns
     * <code>null</code>.
     */
    IProductComponent getProductComponent();

    /**
     * Returns the previous generation if available if not <code>null</code> will be returned.
     */
    public IProductComponentGeneration getPreviousGeneration();

    /**
     * Returns the next generation if available if not <code>null</code> will be returned.
     */
    public IProductComponentGeneration getNextGeneration();

    /**
     * Returns the point in time this generation is valid from in the given time zone. This method
     * never returns <code>null</code>.
     * 
     * @throws NullPointerException if zone is <code>null</code>.
     */
    Date getValidFrom(TimeZone zone);

    /**
     * Returns the <code>IProductComponentLink</code> for the association with the given role name
     * to the given product component or <code>null</code> if no such association exists.
     */
    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target);

    /**
     * Returns a <code>List</code> of all the <code>IProductComponentLink</code>s from this product
     * component generation to other product components.
     */
    public List<IProductComponentLink<? extends IProductComponent>> getLinks();

}
