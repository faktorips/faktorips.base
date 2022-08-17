/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;

public class MessageListTablePageElement extends AbstractStandardTablePageElement {

    private static final String MESSAGE_LIST_TABLE_PAGE_ELEMENT_SEVERITY = "MessageListTablePageElement_severity"; //$NON-NLS-1$
    private static final String MESSAGE_LIST_TABLE_PAGE_ELEMENT_INFO = "MessageListTablePageElement_info"; //$NON-NLS-1$
    private static final String MESSAGE_LIST_TABLE_PAGE_ELEMENT_WARNING = "MessageListTablePageElement_warning"; //$NON-NLS-1$
    private static final String MESSAGE_LIST_TABLE_PAGE_ELEMENT_ERROR = "MessageListTablePageElement_error"; //$NON-NLS-1$

    private final MessageList messageList;

    public MessageListTablePageElement(MessageList messageList, DocumentationContext context) {
        super(context);
        this.messageList = messageList;
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
        Severity severity = message.getSeverity();
        addSubElement(new TableRowPageElement(new IPageElement[] {
                createInvalidObjectPropertiesPageElement(message),
                new TextPageElement(message.getText(), getContext()),
                new TextPageElement(severity == Message.ERROR ? getContext().getMessage(
                        MESSAGE_LIST_TABLE_PAGE_ELEMENT_ERROR)
                        : severity == Message.WARNING ? getContext().getMessage(
                                MESSAGE_LIST_TABLE_PAGE_ELEMENT_WARNING)
                                : severity == Message.INFO ? getContext().getMessage(
                                        MESSAGE_LIST_TABLE_PAGE_ELEMENT_INFO)
                                        : getContext().getMessage(
                                                MESSAGE_LIST_TABLE_PAGE_ELEMENT_SEVERITY)
                                                + severity,
                        getContext()) },
                getContext()));
    }

    protected IPageElement createInvalidObjectPropertiesPageElement(Message message) {
        if (message.getInvalidObjectProperties().size() == 0) {
            return new TextPageElement("", getContext()); //$NON-NLS-1$
        }

        if (message.getInvalidObjectProperties().size() == 1) {
            return createInvalidObjectPropertiesItem(message.getInvalidObjectProperties().get(0));
        }

        ListPageElement objectPropertiesList = new ListPageElement(getContext());

        List<ObjectProperty> invalidObjectProperties = message.getInvalidObjectProperties();
        for (ObjectProperty objectProperty : invalidObjectProperties) {
            objectPropertiesList.addPageElements(createInvalidObjectPropertiesItem(objectProperty));
        }

        return objectPropertiesList;
    }

    protected IPageElement createInvalidObjectPropertiesItem(ObjectProperty objectProperty) {
        IIpsSrcFile srcFile = getLinkableSrcFile(objectProperty.getObject(), getContext());
        if (srcFile != null) {
            return new PageElementUtils(getContext()).createLinkPageElement(getContext(), srcFile, TargetType.CONTENT,
                    srcFile.getIpsObjectName(), true);
        }
        return new TextPageElement(objectProperty.getObject().toString(), getContext());
    }

    private IIpsSrcFile getLinkableSrcFile(Object object, DocumentationContext context) {
        if (object instanceof String) {
            return null;
        }
        if (object instanceof IIpsSrcFile) {
            return (IIpsSrcFile)object;
        }
        if (object instanceof IDescription) {
            return getLinkableSrcFile(((IDescription)object).getParent(), getContext());
        }
        if (object instanceof IIpsObjectPartContainer) {
            return ((IIpsObjectPartContainer)object).getIpsSrcFile();
        }

        context.addStatus(new IpsStatus(IStatus.WARNING, "No IpsSrcFile for class " + object.getClass())); //$NON-NLS-1$
        return null;
    }

    @Override
    protected List<String> getHeadline() {
        List<String> headline = new ArrayList<>();

        headline.add(getContext().getMessage(HtmlExportMessages.MessageListTablePageElement_headlineProperties));
        headline.add(getContext().getMessage(HtmlExportMessages.MessageListTablePageElement_headlineMessage));
        headline.add(getContext().getMessage(HtmlExportMessages.MessageListTablePageElement_headlineSeverity));

        return headline;
    }

    @Override
    public boolean isEmpty() {
        return messageList.isEmpty();
    }

}
