/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    CardinalityRange getCardinality();

    /**
     * Returns the {@link IProductComponentLinkSource} this link originates from. This may be a
     * {@link IProductComponentGeneration} or a {@link IProductComponent} (since 3.8).
     * 
     * @since The return value of this method changed in version 3.8 from
     *            {@link IProductComponentGeneration} to {@link IProductComponentLinkSource} because
     *            the link source may be a product component or a product component generation.
     */
    IProductComponentLinkSource getSource();

    /**
     * Returns the target product component.
     */
    T getTarget();

    /**
     * Returns the target product component's id.
     */
    String getTargetId();

    /**
     * Returns the name of the association this link belongs to.
     */
    String getAssociationName();

}
