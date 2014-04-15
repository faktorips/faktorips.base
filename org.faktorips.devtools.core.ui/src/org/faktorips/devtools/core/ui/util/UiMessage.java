/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IMessage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

public final class UiMessage implements IMessage {

    static final String SEPERATOR = ": "; //$NON-NLS-1$

    private final int messageType;
    private final String messageText;
    private final String messageCode;
    private final String prefix;

    public UiMessage(Message message) {
        messageType = convertSeverityToMessageType(message.getSeverity());
        messageText = message.getText();
        messageCode = message.getCode();
        Object firstObject = getFirstObject(message.getInvalidObjectProperties());
        prefix = getPrefix(firstObject);
    }

    public UiMessage(String text) {
        messageType = IMessageProvider.INFORMATION;
        messageText = text;
        messageCode = text;
        prefix = StringUtils.EMPTY;
    }

    /**
     * Converts from Faktor-IPS {@link Message} severity to {@link IMessageProvider} message type.
     */
    private int convertSeverityToMessageType(int severity) {
        switch (severity) {
            case Message.INFO:
                return IMessageProvider.INFORMATION;

            case Message.WARNING:
                return IMessageProvider.WARNING;

            case Message.ERROR:
                return IMessageProvider.ERROR;

            default:
                return IMessageProvider.NONE;
        }
    }

    private Object getFirstObject(ObjectProperty[] objectProperties) {
        if (objectProperties.length == 0 || objectProperties[0] == null) {
            return StringUtils.EMPTY;
        }

        ObjectProperty invalidObjectProperty = objectProperties[0];
        Object contextObject = invalidObjectProperty.getObject();
        return contextObject;
    }

    private String getPrefix(Object contextObject) {
        if (contextObject instanceof IIpsObject) {
            return StringUtils.EMPTY;
        }
        if (contextObject instanceof IValueHolder<?>) {
            IValueHolder<?> holder = (IValueHolder<?>)contextObject;
            return getObjectName(holder.getParent());

        }
        return getObjectName(contextObject);

    }

    private String getObjectName(Object contextObject) {
        String objectName = StringUtils.EMPTY;
        if (contextObject instanceof ILabeledElement) {
            objectName = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel((ILabeledElement)contextObject);
        } else if (contextObject instanceof IIpsObjectPartContainer) {
            objectName = getCaptionName((IIpsObjectPartContainer)contextObject);
        }
        if (StringUtils.isEmpty(objectName) && contextObject instanceof IIpsElement) {
            objectName = ((IIpsElement)contextObject).getName();
        }
        if (StringUtils.isEmpty(objectName)) {
            return StringUtils.EMPTY;
        } else {
            return objectName + SEPERATOR;
        }
    }

    String getCaptionName(IIpsObjectPartContainer contextObject) {
        String caption = IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(contextObject);
        if (StringUtils.isEmpty(caption)) {
            IIpsElement parent = contextObject.getParent();
            if (parent instanceof IIpsObjectPartContainer) {
                IIpsObjectPartContainer parentContainer = (IIpsObjectPartContainer)parent;
                return getCaptionName(parentContainer);
            }
        }
        return caption;
    }

    @Override
    public int getMessageType() {
        return messageType;
    }

    @Override
    public String getMessage() {
        return messageText;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Object getKey() {
        return messageCode;
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public Control getControl() {
        return null;
    }
}