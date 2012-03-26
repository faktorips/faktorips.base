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

package org.faktorips.devtools.core.model.productcmpt;


/**
 * A factory create an {@link IValueHolder} for {@link IAttributeValue attribute values}. To
 * use this factory with {@link IAttributeValue} you have to register your holder type in the enum
 * {@link AttributeValueType} and provide a zero argument default constructor in your factory.
 * 
 * @author dirmeier
 */
public interface IAttributeValueHolderFactory<T> {

    /**
     * Creating a new value holder with the specified parent. The new value holder is not set as
     * value holder in the specified {@link IAttributeValue}.
     * 
     * @param parent The attribute value used as parent object
     * @return the newly created value holder.
     */
    public IValueHolder<T> createValueHolder(IAttributeValue parent);

    /**
     * Creating a new value holder with the specified parent and set the specified default value.
     * The new value holder is not set as value holder in the specified {@link IAttributeValue}. The
     * implementation must not perform a change event when setting the default value!
     * 
     * @param parent The attribute value used as parent object
     * @param defaultValue The value set as default value.
     * 
     * @return the newly created value holder.
     */
    public IValueHolder<T> createValueHolder(IAttributeValue parent, T defaultValue);

}
