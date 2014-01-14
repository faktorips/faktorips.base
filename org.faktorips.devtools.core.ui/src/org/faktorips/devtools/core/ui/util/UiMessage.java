/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    private final Message message;

    public UiMessage(Message message) {
        this.message = message;
    }

    @Override
    public int getMessageType() {
        return convertSeverityToMessageType(message.getSeverity());
    }

    @Override
    public String getMessage() {
        return message.getText();
    }

    @Override
    public String getPrefix() {
        Object contextObject = getFirstInvalidObject();
        if (contextObject instanceof IValueHolder<?>) {
            IValueHolder<?> holder = (IValueHolder<?>)contextObject;
            contextObject = holder.getParent();
        }

        String objectName = StringUtils.EMPTY;
        if (contextObject instanceof IIpsObject) {
            return StringUtils.EMPTY;
        }
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

    private Object getFirstInvalidObject() {
        ObjectProperty[] invalidObjectProperties = message.getInvalidObjectProperties();
        if (invalidObjectProperties.length == 0 || invalidObjectProperties[0] == null) {
            return StringUtils.EMPTY;
        }

        ObjectProperty invalidObjectProperty = invalidObjectProperties[0];
        Object contextObject = invalidObjectProperty.getObject();
        return contextObject;
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
    public Object getKey() {
        return message.getCode();
    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public Control getControl() {
        return null;
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
}