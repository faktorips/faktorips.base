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

/**
 * This class represents a link between two product components.
 * 
 * @author Daniel Hohenberger
 */
public interface IProductComponentLink<E extends IProductComponent> extends IRuntimeObject, IClRepositoryObject {

    /**
     * @return this link's min and max cardinality as a <code>IntegerRange</code>.
     */
    public CardinalityRange getCardinality();

    /**
     * Returns the product component generation this link belongs to. This method never returns
     * <code>null</code>.
     */
    public IProductComponentGeneration getSource();

    /**
     * Returns the target product component.
     */
    public E getTarget();

    /**
     * Returns the target product component's id.
     */
    public String getTargetId();

    /**
     * Returns the name of the association this link belongs to.
     */
    public String getAssociationName();

}
