/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IMessage;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.IpsStringUtils;

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
        prefix = IpsStringUtils.EMPTY;
    }

    /**
     * Converts from Faktor-IPS {@link Message} severity to {@link IMessageProvider} message type.
     */
    private int convertSeverityToMessageType(Severity severity) {
        return switch (severity) {
            case INFO -> IMessageProvider.INFORMATION;
            case WARNING -> IMessageProvider.WARNING;
            case ERROR -> IMessageProvider.ERROR;
            default -> IMessageProvider.NONE;
        };
    }

    private Object getFirstObject(List<ObjectProperty> objectProperties) {
        if (objectProperties.size() == 0 || objectProperties.get(0) == null) {
            return IpsStringUtils.EMPTY;
        }

        ObjectProperty invalidObjectProperty = objectProperties.get(0);
        return invalidObjectProperty.getObject();
    }

    private String getPrefix(Object contextObject) {
        if (contextObject instanceof IIpsObject) {
            return IpsStringUtils.EMPTY;
        }
        if (contextObject instanceof IValueHolder<?> holder) {
            return getObjectName(holder.getParent());

        }
        return getObjectName(contextObject);

    }

    private String getObjectName(Object contextObject) {
        String objectName = IpsStringUtils.EMPTY;
        if (contextObject instanceof ILabeledElement) {
            objectName = IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel((ILabeledElement)contextObject);
        } else if (contextObject instanceof IIpsObjectPartContainer) {
            objectName = getCaptionName((IIpsObjectPartContainer)contextObject);
        }
        if (IpsStringUtils.isEmpty(objectName) && contextObject instanceof IIpsElement) {
            objectName = ((IIpsElement)contextObject).getName();
        }
        if (IpsStringUtils.isEmpty(objectName)) {
            return IpsStringUtils.EMPTY;
        } else {
            return objectName + SEPERATOR;
        }
    }

    String getCaptionName(IIpsObjectPartContainer contextObject) {
        String caption = IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(contextObject);
        if (IpsStringUtils.isEmpty(caption)) {
            IIpsElement parent = contextObject.getParent();
            if (parent instanceof IIpsObjectPartContainer parentContainer) {
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
