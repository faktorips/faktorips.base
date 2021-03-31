/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.value;

import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Represents a value that holds a specific valueObject T
 * 
 * @author frank
 * @since 3.9
 */
public interface IValue<T> {

    /**
     * Returns the saved content
     */
    T getContent();

    /**
     * Returns the String representation of the content.
     */
    String getContentAsString();

    /**
     * Writes the value to the xml document and returns the actual node. For reading from the xml
     * document there is {@code ValueFactory#createValue}
     * 
     * @param doc the Document
     */
    Node toXml(Document doc);

    /**
     * Validate the value, append messages to the MessagesList.
     * 
     * @param datatype the attributes datatype
     * @param datatypeName the name of the attribute's datatype, used for error messages in case the
     *            datatype is {@code null}
     * @param ipsproject the actual IpsProject
     * @param list Append new Messages to the MessageList
     * @param objectProperty ObjectProperty for Binding
     */
    void validate(ValueDatatype datatype,
            String datatypeName,
            IIpsProject ipsproject,
            MessageList list,
            ObjectProperty... objectProperty);

    /**
     * Add a {@link PropertyChangeListener} for the value.
     * 
     * @param listener the listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a {@link PropertyChangeListener} for the value.
     * 
     * @param listener the listener to delete
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns the content in the Locale that comes with the parameter. For non international values
     * the result will be always the same value.
     * 
     * @param locale the Locale
     */
    String getLocalizedContent(Locale locale);

    /**
     * Returns the content in the default project language. For non international values the result
     * will be always the same value.
     * 
     * @param ipsProject the IPSProject
     * @see #getLocalizedContent(Locale)
     */
    String getDefaultLocalizedContent(IIpsProject ipsProject);

    /**
     * Returns the first non-empty content, regardless of its locale. Returns an empty string if
     * none could be found.
     * 
     * @see #getLocalizedContent(Locale)
     */
    String getLocalizedContent();

    int compare(IValue<?> other, ValueDatatype valueDatatype);
}
