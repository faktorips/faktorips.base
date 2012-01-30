/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.ui.messagesimport;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class MessagesImportPMO extends PresentationModelObject {

    public final static String PROPERTY_FILE_NAME = "fileName"; //$NON-NLS-1$

    public final static String PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT = "ipsPackageFragmentRoot"; //$NON-NLS-1$

    public final static String PROPERTY_LOCALE = "locale"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "MESSAGES_IMPORT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_EMPTY_FILE = MSGCODE_PREFIX + "noFile"; //$NON-NLS-1$

    public static final String MSG_DIRECTORY_FILE = MSGCODE_PREFIX + "directory"; //$NON-NLS-1$

    public static final String MSG_NO_EXIST_FILE = MSGCODE_PREFIX + "noExistFile"; //$NON-NLS-1$

    public static final String MSG_INVALID_TARGET = MSGCODE_PREFIX + "invalidTarget"; //$NON-NLS-1$

    public static final String MSG_NO_LOCALE = MSGCODE_PREFIX + "noLocale"; //$NON-NLS-1$

    private String fileName = ""; //$NON-NLS-1$

    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private ISupportedLanguage locale;

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        String oldValue = this.fileName;
        this.fileName = fileName;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FILE_NAME, oldValue, fileName));
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param ipsPackageFragmentRoot The ipsPackageFragmentRoot to set.
     */
    public void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot ipsPackageFragmentRoot) {
        IIpsPackageFragmentRoot oldValue = this.ipsPackageFragmentRoot;
        this.ipsPackageFragmentRoot = ipsPackageFragmentRoot;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT, oldValue,
                ipsPackageFragmentRoot));
    }

    /**
     * @return Returns the ipsPackageFragmentRoot.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return ipsPackageFragmentRoot;
    }

    /**
     * @param locale The locale to set.
     */
    public void setLocale(ISupportedLanguage locale) {
        ISupportedLanguage oldValue = this.locale;
        this.locale = locale;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_LOCALE, oldValue, locale));
    }

    /**
     * @return Returns the locale.
     */
    public ISupportedLanguage getLocale() {
        return locale;
    }

    /**
     * @return Returns the availableLocales.
     */
    public Set<ISupportedLanguage> getAvailableLocales() {
        if (getIpsPackageFragmentRoot() == null) {
            return new HashSet<ISupportedLanguage>();
        } else {
            IIpsProject ipsProject = getIpsPackageFragmentRoot().getIpsProject();
            return ipsProject.getProperties().getSupportedLanguages();
        }
    }

    public MessageList validate() {
        MessageList result = new MessageList();
        Message messageTarget = validateTargetname();

        if (messageTarget != null) {
            result.add(messageTarget);

        } else {

            Message messageFile = validateFilename();

            if (messageFile != null) {
                result.add(messageFile);

            } else {
                Message messageLocale = validateLocale();

                if (messageLocale != null) {
                    result.add(messageLocale);
                }

            }
        }
        return result;
    }

    private Message validateLocale() {
        Message localeMessage = null;

        if (locale == null) {

            localeMessage = new Message(MSG_NO_LOCALE, Messages.MessagesImportPMO_EmptyLocale, Message.ERROR);
            return localeMessage;

        }

        return localeMessage;
    }

    public Message validateFilename() {
        Message fileMessage = null;

        String filename = getFileName();
        if (filename.length() == 0) {
            fileMessage = new Message(MSG_EMPTY_FILE, Messages.MessagesImportPMO_EmptyFilename, Message.ERROR);
            return fileMessage;
        }
        File file = new File(filename);
        if (file.isDirectory()) {
            fileMessage = new Message(MSG_DIRECTORY_FILE, Messages.MessagesImportPMO_FilenameIsDirectory, Message.ERROR);
            return fileMessage;
        }
        if (!(new File(filename).exists())) {
            fileMessage = new Message(MSG_NO_EXIST_FILE, Messages.MessagesImportPMO_FileDoesNotExist, Message.ERROR);
            return fileMessage;

        }
        return fileMessage;
    }

    private Message validateTargetname() {
        Message targetMessage = null;
        if (ipsPackageFragmentRoot == null) {
            targetMessage = new Message(MSG_INVALID_TARGET, Messages.MessagesImportPMO_EmptyTargetname, Message.ERROR);
            return targetMessage;
        }

        return targetMessage;
    }

}
