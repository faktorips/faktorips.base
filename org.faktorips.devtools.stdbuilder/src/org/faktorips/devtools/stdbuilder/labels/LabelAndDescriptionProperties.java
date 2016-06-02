/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.labels;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedProperties;
import org.faktorips.devtools.stdbuilder.propertybuilder.PropertyKey;

public class LabelAndDescriptionProperties extends AbstractLocalizedProperties {

    public LabelAndDescriptionProperties(boolean defaultLang) {
        super(defaultLang);
    }

    @Override
    protected MessageKey createPropertyEntry(String key) {
        return MessageKey.create(key);
    }

    public void put(IIpsObjectPartContainer ipsObjectPart, MessageType messageType, String messageText) {
        MessageKey messageKey = new MessageKey(ipsObjectPart, messageType);
        put(messageKey, messageText);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<MessageKey> getKeysForIpsObject(String ipsObjectQname) {
        return (Collection<MessageKey>)super.getKeysForIpsObject(ipsObjectQname);
    }

    public static enum MessageType {

        LABEL,

        DESCRIPTION;

    }

    public static class MessageKey implements PropertyKey {

        /**
         * The separator to concatenate the key. We use the minus character because this character
         * is not allowed in names.
         */
        private static final String QNAME_SEPARATOR = "-"; //$NON-NLS-1$

        private final String key;
        private final String ipsObjectQname;
        private final String ipsObjectPartType;
        private final String ipsObjectPartName;
        private final MessageType messageType;

        public MessageKey(IIpsObjectPartContainer ipsObjectPart, MessageType messageType) {
            ipsObjectQname = ipsObjectPart.getIpsObject().getQualifiedName();
            ipsObjectPartType = ipsObjectPart.getClass().getSimpleName();
            ipsObjectPartName = ipsObjectPart.getName();
            this.messageType = messageType;
            key = ipsObjectQname + QNAME_SEPARATOR + ipsObjectPartType + QNAME_SEPARATOR + ipsObjectPartName
                    + QNAME_SEPARATOR + messageType.name();
        }

        public MessageKey(String key, String ipsObjectQName, String ipsObjectPartType, String ipsObjectPartName,
                MessageType messageType) {
            this.key = key;
            ipsObjectQname = ipsObjectQName;
            this.ipsObjectPartType = ipsObjectPartType;
            this.ipsObjectPartName = ipsObjectPartName;
            this.messageType = messageType;
        }

        public static MessageKey create(String key) {
            String[] split = key.split(QNAME_SEPARATOR);
            if (split.length == 4) {
                String ipsObjectQname = split[0];
                String ipsObjectPartType = split[1];
                String ipsObjectPartName = split[2];
                MessageType type = MessageType.valueOf(split[3]);
                return new MessageKey(key, ipsObjectQname, ipsObjectPartType, ipsObjectPartName, type);
            } else {
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getIpsObjectQname() {
            return ipsObjectQname;
        }

        public String getIpsObjectPartType() {
            return ipsObjectPartType;
        }

        public String getIpsObjectPartName() {
            return ipsObjectPartName;
        }

        public MessageType getMessageType() {
            return messageType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MessageKey other = (MessageKey)obj;
            return ObjectUtils.equals(this.key, other.key);
        }

        @Override
        public String toString() {
            return key;
        }

    }

}
