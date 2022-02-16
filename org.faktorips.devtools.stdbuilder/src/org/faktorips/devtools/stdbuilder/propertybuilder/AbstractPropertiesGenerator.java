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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;

public abstract class AbstractPropertiesGenerator {

    private final AFile messagesPropertiesFile;
    private final ISupportedLanguage supportedLanguage;
    private final AbstractLocalizedPropertiesBuilder builder;
    private final AbstractLocalizedProperties localizedProperties;

    public AbstractPropertiesGenerator(AFile messagesPropertiesFile, ISupportedLanguage supportedLanguage,
            AbstractLocalizedPropertiesBuilder propertiesBuilder, AbstractLocalizedProperties localizedProperties) {
        this.messagesPropertiesFile = messagesPropertiesFile;
        this.supportedLanguage = supportedLanguage;
        this.builder = propertiesBuilder;
        this.localizedProperties = localizedProperties;
        try {
            if (messagesPropertiesFile.exists()) {
                localizedProperties.load(messagesPropertiesFile.getContents());
            }
        } catch (IpsException e) {
            StdBuilderPlugin.log(e);
        }
    }

    void storeMessagesToFile(AFile propertyFile, AbstractLocalizedProperties messages) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        messages.store(outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        builder.writeToFile(propertyFile, inputStream, true, true);
    }

    /**
     * Saving the properties to the file adding the given comment. The file must already exists.
     * 
     * @return true if file was modified otherwise false
     * @throws IpsException in case of any exception during writing to file
     */
    public boolean saveIfModified() {
        if (getLocalizedProperties().isModified()) {
            AFile file = getMessagesPropertiesFile();
            if (!file.exists()) {
                file.create(new ByteArrayInputStream("".getBytes()), null); //$NON-NLS-1$
                file.setDerived(builder.buildsDerivedArtefacts()
                        && builder.getBuilderSet().isMarkNoneMergableResourcesAsDerived(), null);
            }
            storeMessagesToFile(file, getLocalizedProperties());
            return true;
        } else {
            return false;
        }
    }

    public void loadMessages() {
        if (messagesPropertiesFile.exists()) {
            getLocalizedProperties().load(messagesPropertiesFile.getContents());
        } else {
            getLocalizedProperties().clear();
        }
    }

    /**
     * @return Returns the messagesPropertiesFile.
     */
    public AFile getMessagesPropertiesFile() {
        return messagesPropertiesFile;
    }

    public void deleteAllMessagesFor(QualifiedNameType ipsObject) {
        getLocalizedProperties().deleteAllMessagesFor(ipsObject);
    }

    public ISupportedLanguage getSupportedLanguage() {
        return supportedLanguage;
    }

    protected Locale getLocale() {
        return getSupportedLanguage().getLocale();
    }

    public AbstractLocalizedProperties getLocalizedProperties() {
        return localizedProperties;
    }

    /**
     * Checks which parts are deleted from the IPS object with the specified name and remove these
     * deleted messages.
     * 
     * The existingLocalizedProperties only need to contain the messages for the current IPS object.
     * It could be easily created by creating a new {@link AbstractLocalizedProperties} object for
     * the current IPS object. The method compares the already existing messages for the object with
     * the current existing messages
     */
    protected void deleteMessagesForDeletedParts(QualifiedNameType qualifiedNameType,
            AbstractLocalizedProperties existingLocalizedProperties) {
        Collection<PropertyKey> keysForIpsObjectToDelete = new HashSet<>(getLocalizedProperties()
                .getKeysForIpsObject(qualifiedNameType));
        Collection<? extends PropertyKey> newKeysForIpsObject = existingLocalizedProperties
                .getKeysForIpsObject(qualifiedNameType);
        keysForIpsObjectToDelete.removeAll(newKeysForIpsObject);
        for (PropertyKey propertyKey : keysForIpsObjectToDelete) {
            getLocalizedProperties().remove(propertyKey);
        }
    }

}