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

package org.faktorips.devtools.core.model.value;

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
     * @param objectProperty ObjectProperty for Binding
     * @param list Append new Messages to the MessageList
     */
    void validate(ValueDatatype datatype, IIpsProject ipsproject, ObjectProperty objectProperty, MessageList list);

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
}
