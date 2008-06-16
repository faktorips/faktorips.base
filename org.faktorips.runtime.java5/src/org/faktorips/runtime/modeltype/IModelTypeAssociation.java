/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.modeltype;


/**
 * 
 * @author Daniel Hohenberger
 */
public interface IModelTypeAssociation extends IModelElement {
    
    /**
     * @return the target model type object of this association.
     * @throws ClassNotFoundException if the target class could not be loaded.
     */
    public IModelType getTarget() throws ClassNotFoundException;
    
    /**
     * @return the minimum cardinality for this association. <code>0</code> if no minimum is set.
     */
    public int getMinCardinality();

    /**
     * @return the maximum cardinality for this association. <code>Integer.MAX_VALUE</code> if no maximum is set.
     */
    public int getMaxCardinality();

    /**
     * @return the plural form of this model type's name.
     */
    public String getNamePlural();

    /**
     * @return the type of this association.
     */
    public AssociationType getAssociationType();

    /**
     * Enum defining the possible association types.
     */
    public enum AssociationType {
        Association, Composition, CompositionToMaster;
    }
    
    /**
     * @return if this association is product relevant.
     */
    public boolean isProductRelevant();

}
