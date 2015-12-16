/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.value;

import java.util.Locale;
import java.util.Observer;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Represents a value that holds a specific valueObject T
 * 
 * @author frank
 * @since 3.9
 */
public interface IValue<T> extends Comparable<IValue<T>> {

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
     * document there is {@link ValueFactory#createValue}
     * 
     * @param doc the Document
     */
    Node toXml(Document doc);

    /**
     * Validate the value, append messages to the MessagesList.
     * 
     * @param datatype the attributes datatype
     * @param ipsproject the actual IpsProject
     * @param list Append new Messages to the MessageList
     * @param objectProperty ObjectProperty for Binding
     */
    void validate(ValueDatatype datatype, IIpsProject ipsproject, MessageList list, ObjectProperty... objectProperty);

    /**
     * Add an observer for the value.
     * 
     * @param observer the observer to add
     */
    void addObserver(Observer observer);

    /**
     * Removes an observer for the value.
     * 
     * @param observer the observer to delete
     */
    void deleteObserver(Observer observer);

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
}
