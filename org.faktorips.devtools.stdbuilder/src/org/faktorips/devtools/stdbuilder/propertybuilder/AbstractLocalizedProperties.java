/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.propertybuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.stdbuilder.MessagesProperties;
import org.faktorips.util.MultiMap;

public abstract class AbstractLocalizedProperties {

    private final MultiMap<QualifiedNameType, PropertyKey> propertyKeysForIpsObject = MultiMap.createWithSetsAsValues();

    private final MessagesProperties messagesProperties = new MessagesProperties();

    private final boolean defaultLang;

    public AbstractLocalizedProperties(boolean defaultLang) {
        this.defaultLang = defaultLang;
    }

    public void store(OutputStream outputStream) {
        messagesProperties.store(outputStream);
    }

    public int size() {
        return messagesProperties.size();
    }

    public void clear() {
        messagesProperties.clear();
        propertyKeysForIpsObject.clear();
    }

    public boolean isModified() {
        return messagesProperties.isModified();
    }

    public void deleteAllMessagesFor(QualifiedNameType qualifiedNameType) {
        for (PropertyKey propertyKey : propertyKeysForIpsObject.get(qualifiedNameType)) {
            messagesProperties.remove(propertyKey.getKey());
        }
        propertyKeysForIpsObject.remove(qualifiedNameType);
    }

    public void load(InputStream stream) {
        messagesProperties.load(stream);
        initMessagesForIpsObjects();
    }

    void initMessagesForIpsObjects() {
        propertyKeysForIpsObject.clear();
        for (String key : messagesProperties.keySet()) {
            PropertyKey propertyKey = createPropertyEntry(key);
            if (propertyKey != null) {
                propertyKeysForIpsObject.put(propertyKey.getIpsObjectQNameType(), propertyKey);
            }
        }
    }

    protected abstract PropertyKey createPropertyEntry(String key);

    public void put(PropertyKey propertyKey, String messageText) {
        if (defaultLang || StringUtils.isNotBlank(messageText)) {
            propertyKeysForIpsObject.put(propertyKey.getIpsObjectQNameType(), propertyKey);
            messagesProperties.put(propertyKey.getKey(), messageText);
        } else {
            remove(propertyKey);
        }
    }

    public void remove(PropertyKey propertyKey) {
        propertyKeysForIpsObject.remove(propertyKey.getIpsObjectQNameType(), propertyKey);
        messagesProperties.remove(propertyKey.getKey());
    }

    protected Collection<? extends PropertyKey> getKeysForIpsObject(QualifiedNameType qualifiedNameType) {
        return propertyKeysForIpsObject.get(qualifiedNameType);
    }

    public String getMessage(String qualifiedMessageKey) {
        return messagesProperties.getMessage(qualifiedMessageKey);
    }

    @Override
    public String toString() {
        return messagesProperties.toString();
    }

}
