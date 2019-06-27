/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.stdbuilder.propertybuilder.AbstractLocalizedProperties;
import org.faktorips.devtools.stdbuilder.propertybuilder.PropertyKey;
import org.faktorips.runtime.model.type.DocumentationKind;

public class LabelAndDescriptionProperties extends AbstractLocalizedProperties {

    public LabelAndDescriptionProperties(boolean defaultLang) {
        super(defaultLang);
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
            String ipsObjectPartName = ipsObjectPart instanceof IpsObject ? StringUtils.EMPTY : ipsObjectPart.getName();
            key = messageType.getKey(ipsObjectQname, type.getId(), ipsObjectPartName);
        }

        public MessageKey(String key, String ipsObjectQName, IpsObjectType type) {
            this.key = key;
            this.ipsObjectQname = ipsObjectQName;
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
