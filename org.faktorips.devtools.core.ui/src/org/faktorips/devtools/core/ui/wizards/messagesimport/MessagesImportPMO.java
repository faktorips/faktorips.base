/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.messagesimport;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.pctype.validationrule.ValidationRuleIdentification;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class MessagesImportPMO extends PresentationModelObject {

    public static final String PROPERTY_FILE_NAME = "fileName"; //$NON-NLS-1$

    public static final String PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT = "ipsPackageFragmentRoot"; //$NON-NLS-1$

    public static final String PROPERTY_LOCALE = "locale"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT = "format"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT_DELIMITER = "formatDelimiter"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT_IDENTIFIER = "formatIdentifier"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT_COLUMN = "formatColumn"; //$NON-NLS-1$

    public static final String PROPERTY_IDENTIFICATION = "identification"; //$NON-NLS-1$

    public static final String PROPERTY_ENABLE_WARNINGS = "enableWarnings"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT_SETTINGS_VISIBLE = "formatSettingsVisible"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "MESSAGES_IMPORT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_EMPTY_FILE = MSGCODE_PREFIX + "noFile"; //$NON-NLS-1$

    public static final String MSG_DIRECTORY_FILE = MSGCODE_PREFIX + "directory"; //$NON-NLS-1$

    public static final String MSG_NO_EXIST_FILE = MSGCODE_PREFIX + "noExistFile"; //$NON-NLS-1$

    public static final String MSG_INVALID_TARGET = MSGCODE_PREFIX + "invalidTarget"; //$NON-NLS-1$

    public static final String MSG_NO_LOCALE = MSGCODE_PREFIX + "noLocale"; //$NON-NLS-1$

    public static final String FORMAT_PROPERTY_FILE = "formatPropertyFile"; //$NON-NLS-1$

    public static final String FORMAT_CSV_FILE = "formatCsvFile"; //$NON-NLS-1$

    private String fileName = StringUtils.EMPTY;

    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private ISupportedLanguage locale;

    private String format = FORMAT_CSV_FILE;

    private String formatDelimiter = StringUtils.EMPTY;

    private String formatIdentifier = StringUtils.EMPTY;

    private String formatColumn = StringUtils.EMPTY;

    private ValidationRuleIdentification identification = ValidationRuleIdentification.QUALIFIED_RULE_NAME;

    private boolean enableWarnings = true;

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        String oldValue = this.fileName;
        this.fileName = fileName;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FILE_NAME, oldValue, fileName));
    }

    /**
     * @param format The format of the imported file to set.
     */
    public void setFormat(String format) {
        String oldValue = this.format;
        this.format = format;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FORMAT, oldValue, format));
    }

    /**
     * @param formatDelimiter The formatDelimiter to set.
     */
    public void setFormatDelimiter(String formatDelimiter) {
        String oldValue = this.formatDelimiter;
        this.formatDelimiter = formatDelimiter;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FORMAT_DELIMITER, oldValue, formatDelimiter));
    }

    /**
     * @param formatIdentifier The formatIdentifier to set.
     */
    public void setFormatIdentifier(String formatIdentifier) {
        String oldValue = this.formatIdentifier;
        this.formatIdentifier = formatIdentifier;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FORMAT_IDENTIFIER, oldValue, formatIdentifier));
    }

    /**
     * @param formatColumn The formatColumn to set.
     */
    public void setFormatColumn(String formatColumn) {
        String oldValue = this.formatColumn;
        this.formatColumn = formatColumn;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FORMAT_COLUMN, oldValue, formatColumn));
    }

    /**
     * @param identification The identification to set.
     */
    public void setIdentification(ValidationRuleIdentification identification) {
        ValidationRuleIdentification oldValue = this.identification;
        this.identification = identification;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IDENTIFICATION, oldValue, identification));
    }

    /**
     * @param enableWarnings Sets if warnings should be displayed after the import.
     */
    public void setEnableWarnings(boolean enableWarnings) {
        boolean oldValue = this.enableWarnings;
        this.enableWarnings = enableWarnings;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_ENABLE_WARNINGS, oldValue, enableWarnings));
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return Returns the format of the imported file.
     */
    public String getFormat() {
        return format;
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
     * @return Returns the formatDelimiter.
     */
    public String getFormatDelimiter() {
        return formatDelimiter;
    }

    /**
     * @return Returns the formatIdentifier.
     */
    public String getFormatIdentifier() {
        return formatIdentifier;
    }

    /**
     * @return Returns the formatColumn.
     */
    public String getFormatColumn() {
        return formatColumn;
    }

    /**
     * @return Returns the identification.
     */
    public ValidationRuleIdentification getIdentification() {
        return identification;
    }

    /**
     * @return Returns if the warnings should be displayed after the import.
     */
    public boolean isEnableWarnings() {
        return enableWarnings;
    }

    /**
     * @return Returns the availableLocales.
     */
    public Set<ISupportedLanguage> getAvailableLocales() {
        if (getIpsPackageFragmentRoot() == null) {
            return new HashSet<ISupportedLanguage>();
        } else {
            IIpsProject ipsProject = getIpsPackageFragmentRoot().getIpsProject();
            return ipsProject.getReadOnlyProperties().getSupportedLanguages();
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

    /**
     * Returns <code>true</code> if the file format is set to {@link #FORMAT_CSV_FILE}, otherwise
     * <code>false</code>.
     */
    public boolean isCsvFileFormat() {
        return FORMAT_CSV_FILE.equals(format);
    }

    public boolean isFormatSettingsVisible() {
        return getFormat().equals(FORMAT_CSV_FILE);
    }

}
