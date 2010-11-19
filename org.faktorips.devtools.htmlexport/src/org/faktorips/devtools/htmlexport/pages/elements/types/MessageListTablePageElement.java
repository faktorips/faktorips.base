/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.helper.path.LinkableIpsElementUtil;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

public class MessageListTablePageElement extends AbstractStandardTablePageElement {

    protected final MessageList messageList;
    protected final DocumentorConfiguration config;

    public MessageListTablePageElement(MessageList messageList, DocumentorConfiguration config) {
        super();
        this.messageList = messageList;
        this.config = config;
    }

    @Override
    protected void addDataRows() {
        for (Message message : messageList) {
            addMessageRow(message);
        }
    }

    /**
     * adds a row for the given message
     * 
     */
    protected void addMessageRow(Message message) {
        int severity = message.getSeverity();
        addSubElement(new TableRowPageElement(new PageElement[] {
                createInvalidObjectPropertiesPageElement(message),
                new TextPageElement(message.getText()),
                new TextPageElement(severity == Message.ERROR ? Messages.MessageListTablePageElement_error
                        : severity == Message.WARNING ? Messages.MessageListTablePageElement_warning
                                : severity == Message.INFO ? Messages.MessageListTablePageElement_info
                                        : Messages.MessageListTablePageElement_severity + severity) }));
    }

    protected PageElement createInvalidObjectPropertiesPageElement(Message message) {
        if (message.getInvalidObjectProperties().length == 0) {
            return new TextPageElement(""); //$NON-NLS-1$
        }

        if (message.getInvalidObjectProperties().length == 1) {
            return createInvalidObjectPropertiesItem(message.getInvalidObjectProperties()[0]);
        }

        ListPageElement objectPropertiesList = new ListPageElement();

        ObjectProperty[] invalidObjectProperties = message.getInvalidObjectProperties();
        for (ObjectProperty objectProperty : invalidObjectProperties) {
            objectPropertiesList.addPageElements(createInvalidObjectPropertiesItem(objectProperty));
        }

        return objectPropertiesList;
    }

    protected PageElement createInvalidObjectPropertiesItem(ObjectProperty objectProperty) {
        IIpsSrcFile srcFile = LinkableIpsElementUtil.getLinkableSrcFile(objectProperty.getObject());
        if (srcFile != null) {
            return PageElementUtils.createLinkPageElement(config, srcFile, "content", //$NON-NLS-1$
                    srcFile.getIpsObjectName(), true);
        }
        return new TextPageElement(objectProperty.getObject().toString());
    }

    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<String>();

        headline.add(Messages.MessageListTablePageElement_headlineProperties);
        headline.add(Messages.MessageListTablePageElement_headlineMessage);
        headline.add(Messages.MessageListTablePageElement_headlineSeverity);

        return headline;
    }

    @Override
    public boolean isEmpty() {
        return messageList.isEmpty();
    }

}