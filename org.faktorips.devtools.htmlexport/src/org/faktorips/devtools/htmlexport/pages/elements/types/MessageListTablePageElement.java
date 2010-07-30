/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Represents a table with the {@link Message}s of a {@link MessageList} as rows and the attributes
 * of the {@link Message} as columns
 * 
 * @author dicker
 * 
 */
public class MessageListTablePageElement extends AbstractStandardTablePageElement {

    protected MessageList messageList;

    /**
     * Creates a {@link MessageListTablePageElement} for the specified {@link MessageList}
     * 
     * @param messageList
     */
    public MessageListTablePageElement(MessageList messageList) {
        super();
        this.messageList = messageList;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#
     * addDataRows()
     */
    @Override
    protected void addDataRows() {
        for (Message message : messageList) {
            addMessageRow(message);
        }
    }

    /**
     * adds a row for the given message
     * 
     * @param message
     */
    protected void addMessageRow(Message message) {
        int severity = message.getSeverity();
        addSubElement(new TableRowPageElement(new PageElement[] {
                new TextPageElement(message.getCode()),
                new TextPageElement(message.getText()),
                new TextPageElement(severity == Message.ERROR ? Messages.MessageListTablePageElement_error
                        : severity == Message.WARNING ? Messages.MessageListTablePageElement_warning
                                : severity == Message.INFO ? Messages.MessageListTablePageElement_info
                                        : Messages.MessageListTablePageElement_severity + severity),
                new TextPageElement(Arrays.toString(message.getInvalidObjectProperties())) }));
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#
     * getHeadline()
     */
    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        headline.add(Messages.MessageListTablePageElement_headlineCode);
        headline.add(Messages.MessageListTablePageElement_headlineMessage);
        headline.add(Messages.MessageListTablePageElement_headlineSeverity);
        headline.add(Messages.MessageListTablePageElement_headlineProperties);

        return headline;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return messageList.isEmpty();
    }

}
