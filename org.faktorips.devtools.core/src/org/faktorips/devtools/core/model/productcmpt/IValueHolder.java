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

import org.faktorips.devtools.core.internal.model.productcmpt.AbstractValueHolder;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.XmlSupport;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * This is the public interface for all value holders as they are used by {@link IAttributeValue}.
 * It is recommended to use the {@link AbstractValueHolder} class for implementing this interface.
 * <p>
 * A value holder is a child of an {@link IIpsObjectPart} for example an {@link IAttributeValue}.
 * Any changes within the value holder have to be propagated to this part.
 * 
 * @author dirmeier
 * @since 3.7
 */
public interface IValueHolder<T> extends XmlSupport, Validatable, Comparable<IValueHolder<T>> {

    /**
     * Returning the {@link IIpsObjectPart} of which this value holder is a child of. Every value
     * holder need to have a parent {@link IIpsObjectPart}. If anything within the value holder
     * changes, the change event is propagated to this part.
     * 
     * @return The parent {@link IIpsObjectPart}
     */
    public IIpsObjectPart getParent();

    /**
     * Returning a string representation of the value.
     * 
     * @return a string representation of this part.
     */
    public String getStringValue();

    /**
     * Setting a String representation of the value.
     * <p>
     * This method was introduced as deprecated method to handle existing implementations for
     * example for default values that are stored as String instead of value holders. You should not
     * use this method because the it is not specified how the implementation handles the given
     * string value. Especially it is not specified that {@link #getStringValue()} would return the
     * same string as previously set by this method.#
     * 
     * @param value The value you want to set
     * @deprecated Use {@link #setValue(Object)}
     */
    @Deprecated
    public void setStringValue(String value);

    /**
     * Returns the value of this value holder. The type of the value depends on the generic type T.
     * 
     * @return The current value stored in this value holder.
     */
    public T getValue();

    /**
     * Setting a new value for this value holder. The type of the value must match the generic type
     * T.
     * <p>
     * Setting a new value have to perform a change event on the parent.
     * 
     * @param value The value that should be set as current value in this holder.
     */
    public void setValue(T value);

}