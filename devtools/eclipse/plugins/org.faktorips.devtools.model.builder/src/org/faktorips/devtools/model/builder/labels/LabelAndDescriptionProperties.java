/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.labels;

import java.util.Collection;
import java.util.Objects;

import org.faktorips.devtools.model.builder.propertybuilder.AbstractLocalizedProperties;
import org.faktorips.devtools.model.builder.propertybuilder.PropertyKey;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.type.DocumentationKind;

public class LabelAndDescriptionProperties extends AbstractLocalizedProperties {

    public LabelAndDescriptionProperties(boolean defaultLang, String lineSeparator) {
        super(defaultLang, lineSeparator);
    }

    @Override
    protected MessageKey createPropertyEntry(String key) {
        return MessageKey.create(key);
    }

    public void put(IIpsObjectPartContainer ipsObjectPart, DocumentationKind messageType, String messageText) {
        MessageKey messageKey = new MessageKey(ipsObjectPart, messageType);
        put(messageKey, messageText);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<MessageKey> getKeysForIpsObject(QualifiedNameType qualifiedNameType) {
        return (Collection<MessageKey>)super.getKeysForIpsObject(qualifiedNameType);
    }

    public static class MessageKey implements PropertyKey {

        /**
         * The separator to concatenate the key. We use the minus character because this character
         * is not allowed in names.
         */
        private static final String QNAME_SEPARATOR = "-"; //$NON-NLS-1$

        private final String key;
        private final String ipsObjectQname;
        private final IpsObjectType type;

        public MessageKey(IIpsObjectPartContainer ipsObjectPart, DocumentationKind messageType) {
            IIpsObject ipsObject = ipsObjectPart.getIpsObject();
            ipsObjectQname = ipsObject.getQualifiedName();
            type = ipsObject.getIpsObjectType();
            String ipsObjectPartName = ipsObjectPart instanceof IpsObject ? IpsStringUtils.EMPTY
                    : ipsObjectPart.getName();
            key = messageType.getKey(ipsObjectQname, type.getId(), ipsObjectPartName);
        }

        public MessageKey(String key, String ipsObjectQName, IpsObjectType type) {
            this.key = key;
            ipsObjectQname = ipsObjectQName;
            this.type = type;
        }

        public static MessageKey create(String key) {
            String[] split = key.split(QNAME_SEPARATOR);
            String name = split[0];
            String typeName = split[1];
            IpsObjectType typeForName = IpsObjectType.getTypeForName(typeName);
            if (typeForName != null) {
                return new MessageKey(key, name, typeForName);
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
        public QualifiedNameType getIpsObjectQNameType() {
            return new QualifiedNameType(ipsObjectQname, type);
        }

        public String getIpsObjectQname() {
            return ipsObjectQname;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            MessageKey other = (MessageKey)obj;
            return Objects.equals(key, other.key);
        }

        @Override
        public String toString() {
            return key;
        }

    }

}
