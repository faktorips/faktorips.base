/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

/**
 * This class represents a link between two product components.
 * 
 * @author Daniel Hohenberger
 */
public interface IProductComponentLink<T extends IProductComponent> extends IRuntimeObject, IClRepositoryObject {

    /**
     * @return this link's min and max cardinality as a <code>IntegerRange</code>.
     */
    public CardinalityRange getCardinality();

    /**
     * Returns the {@link IProductComponentGeneration} this link originates from (if applicable). If
     * this link originates from a product component an {@link UnsupportedOperationException} is
     * thrown.
     * 
     * @throws UnsupportedOperationException if this link's source is not a product component
     *             generation but a product component
     * @deprecated As of 3.8 links can originate both from product components and generations. No
     *             replacement available.
     */
    @Deprecated
    public IProductComponentGeneration getSource();

    /**
     * Returns the target product component.
     */
    public T getTarget();

    /**
     * Returns the target product component's id.
     */
    public String getTargetId();

    /**
     * Returns the name of the association this link belongs to.
     */
    public String getAssociationName();

}
