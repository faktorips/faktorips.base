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
 * <p>
 * The generic type T specifies the target type of this link.
 * 
 * @author Daniel Hohenberger
 */
public interface IProductComponentLink<T extends IProductComponent> extends IRuntimeObject, IClRepositoryObject {

    /**
     * @return this link's min and max cardinality as a <code>IntegerRange</code>.
     */
    public CardinalityRange getCardinality();

    /**
     * Returns the {@link IProductComponentLinkSource} this link originates from. This may be a
     * {@link IProductComponentGeneration} or a {@link IProductComponent} (since 3.8).
     * 
     * @since The return value of this method changed in version 3.8 from
     *        {@link IProductComponentGeneration} to {@link IProductComponentLinkSource} because the
     *        link source may be a product component or a product component generation.
     */
    public IProductComponentLinkSource getSource();

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
