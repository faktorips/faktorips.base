/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.pctype.validationrule.ValidationRuleIdentification;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

public class MessagesImportPMO extends PresentationModelObject {

    public static final String PROPERTY_FILE_NAME = "filename"; //$NON-NLS-1$

    public static final String PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT = "ipsPackageFragmentRoot"; //$NON-NLS-1$

    public static final String PROPERTY_SUPPORTED_LANGUAGE = "supportedLanguage"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT = "format"; //$NON-NLS-1$

    public static final String PROPERTY_COLUMN_DELIMITER = "columnDelimiter"; //$NON-NLS-1$

    public static final String PROPERTY_IDENTIFIER_COLUMN_INDEX = "identifierColumnIndex"; //$NON-NLS-1$

    public static final String PROPERTY_TEXT_COLUMN_INDEX = "textColumnIndex"; //$NON-NLS-1$

    public static final String PROPERTY_RULE_IDENTIFIER = "ruleIdentifier"; //$NON-NLS-1$

    public static final String PROPERTY_ENABLE_WARNINGS_FOR_MISSING_MESSAGES = "enableWarningsForMissingMessages"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT_SETTINGS_ENABLED = "formatSettingsEnabled"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "MESSAGES_IMPORT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_EMPTY_FILE = MSGCODE_PREFIX + "noFile"; //$NON-NLS-1$

    public static final String MSG_DIRECTORY_FILE = MSGCODE_PREFIX + "noDirectory"; //$NON-NLS-1$

    public static final String MSG_NO_EXIST_FILE = MSGCODE_PREFIX + "noExistFile"; //$NON-NLS-1$

    public static final String MSG_INVALID_TARGET = MSGCODE_PREFIX + "invalidTarget"; //$NON-NLS-1$

    public static final String MSG_NO_LOCALE = MSGCODE_PREFIX + "noLocale"; //$NON-NLS-1$

    public static final String MSG_NO_COLUMN_DELIMITER = MSGCODE_PREFIX + "noColumnDelimiter"; //$NON-NLS-1$

    public static final String MSG_NO_ID_COLUMN_INDEX = MSGCODE_PREFIX + "noIDColumnIndex"; //$NON-NLS-1$

    public static final String MSG_NO_TEXT_COLUMN_INDEX = MSGCODE_PREFIX + "noTextColumnIndex"; //$NON-NLS-1$

    private String filename = IpsStringUtils.EMPTY;

    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private ISupportedLanguage supportedLanguage;

    private ImportFormat format = ImportFormat.CSV;

    private Character columnDelimiter = ';';

    private String idColumnIndex = "1"; //$NON-NLS-1$

    private String textColumnIndex = "2"; //$NON-NLS-1$

    private boolean enableWarningsForMissingMessages = false;

    private ValidationRuleIdentification ruleIdentifier = ValidationRuleIdentification.MESSAGE_CODE;

    private void updateFormat() {
        Path path = new Path(filename);
        String fileExtension = path.getFileExtension();
        setFormat(ImportFormat.getFormat(fileExtension));
    }

    /**
     * @return Returns the fileName.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param fileName The fileName to set.
     */
    public void setFilename(String fileName) {
        String oldValue = filename;
        filename = fileName;
        updateFormat();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FILE_NAME, oldValue, fileName));
    }

    /**
     * @return Returns the format of the imported file.
     */
    public ImportFormat getFormat() {
        return format;
    }

    /**
     * @param format The format of the imported file to set.
     */
    public void setFormat(ImportFormat format) {
        ImportFormat oldValue = this.format;
        this.format = format;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_FORMAT, oldValue, format));
    }

    public boolean isFormatSettingsEnabled() {
        return isCsvFileFormat();
    }

    /**
     * Returns <code>true</code> if the file format is set to {@link ImportFormat#CSV}, otherwise
     * <code>false</code>.
     */
    public boolean isCsvFileFormat() {
        return ImportFormat.CSV == format;
    }

    /**
     * @return Returns the ipsPackageFragmentRoot.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return ipsPackageFragmentRoot;
    }

    /**
     * @param ipsPackageFragmentRoot The ipsPackageFragmentRoot to set.
     */
    public void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot ipsPackageFragmentRoot) {
        IIpsPackageFragmentRoot oldValue = this.ipsPackageFragmentRoot;
        this.ipsPackageFragmentRoot = ipsPackageFragmentRoot;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT, oldValue,
                ipsPackageFragmentRoot));

        updateSupportedLanguage();
    }

    private void updateSupportedLanguage() {
        ISupportedLanguage language = getCalculateSupportedLanguage();
        setSupportedLanguage(language);
    }

    private ISupportedLanguage getCalculateSupportedLanguage() {
        if (ipsPackageFragmentRoot != null) {
            return ipsPackageFragmentRoot.getIpsProject().getReadOnlyProperties().getDefaultLanguage();
        } else {
            return null;
        }
    }

    /**
     * @return Returns the {@link ISupportedLanguage}.
     */
    public ISupportedLanguage getSupportedLanguage() {
        return supportedLanguage;
    }

    /**
     * @param supportedLanguage The {@link ISupportedLanguage} to set.
     */
    public void setSupportedLanguage(ISupportedLanguage supportedLanguage) {
        ISupportedLanguage oldValue = this.supportedLanguage;
        this.supportedLanguage = supportedLanguage;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SUPPORTED_LANGUAGE, oldValue, supportedLanguage));
    }

    /**
     * @return Returns the formatDelimiter.
     */
    public Character getColumnDelimiter() {
        return columnDelimiter;
    }

    //
    // public char getColumnDelimiterChar() {
    // return columnDelimiter.charAt(0);
    // }

    public void setColumnDelimiter(Character columnDelimiter) {
        Character oldValue = this.columnDelimiter;
        this.columnDelimiter = columnDelimiter;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_COLUMN_DELIMITER, oldValue, columnDelimiter));
    }

    /**
     * @return Returns the formatIdentifier.
     */
    public String getIdentifierColumnIndex() {
        return idColumnIndex;
    }

    /**
     * @param idColumnIndex The (one based) index of the column to be used as id.
     */
    public void setIdentifierColumnIndex(String idColumnIndex) {
        String oldValue = this.idColumnIndex;
        this.idColumnIndex = idColumnIndex;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IDENTIFIER_COLUMN_INDEX, oldValue, idColumnIndex));
    }

    /**
     * @return Returns the formatColumn.
     */
    public String getTextColumnIndex() {
        return textColumnIndex;
    }

    /**
     * @param textColumnIndex The (one based) index of the column to be used as text.
     */
    public void setTextColumnIndex(String textColumnIndex) {
        String oldValue = this.textColumnIndex;
        this.textColumnIndex = textColumnIndex;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEXT_COLUMN_INDEX, oldValue, textColumnIndex));
    }

    /**
     * @return Returns the identification.
     */
    public ValidationRuleIdentification getRuleIdentifier() {
        return ruleIdentifier;
    }

    /**
     * @param ruleIdentifier The kind of identifier to be used.
     */
    public void setRuleIdentifier(ValidationRuleIdentification ruleIdentifier) {
        ValidationRuleIdentification oldValue = this.ruleIdentifier;
        this.ruleIdentifier = ruleIdentifier;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_RULE_IDENTIFIER, oldValue, ruleIdentifier));
    }

    /**
     * @return Returns if the warnings concerning missing messages should be displayed after the
     *             import.
     */
    public boolean isEnableWarningsForMissingMessages() {
        return enableWarningsForMissingMessages;
    }

    /**
     * @param enableWarningsForMissingMessages Sets if warnings concerning missing messages should
     *            be displayed after the import.
     */
    public void setEnableWarningsForMissingMessages(boolean enableWarningsForMissingMessages) {
        boolean oldValue = this.enableWarningsForMissingMessages;
        this.enableWarningsForMissingMessages = enableWarningsForMissingMessages;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_ENABLE_WARNINGS_FOR_MISSING_MESSAGES, oldValue,
                enableWarningsForMissingMessages));
    }

    /**
     * @return Returns the availableLocales.
     */
    public Set<ISupportedLanguage> getAvailableLocales() {
        if (getIpsPackageFragmentRoot() == null) {
            return new HashSet<>();
        } else {
            IIpsProject ipsProject = getIpsPackageFragmentRoot().getIpsProject();
            return ipsProject.getReadOnlyProperties().getSupportedLanguages();
        }
    }

    public MessageList validate() {
        MessageList messageList = new MessageList();

        validateTargetname(messageList);
        validateFilename(messageList);
        validateColumnDelimiter(messageList);
        validateIdColumIndex(messageList);
        validateTextColumnIndex(messageList);
        validateLocale(messageList);

        return messageList;
    }

    private void validateTargetname(MessageList messageList) {
        if (ipsPackageFragmentRoot == null) {
            messageList.newError(MSG_INVALID_TARGET, Messages.MessagesImportPMO_EmptyTargetname);
        }
    }

    public void validateFilename(MessageList messageList) {
        if (filename.length() == 0) {
            messageList.newError(MSG_EMPTY_FILE, Messages.MessagesImportPMO_EmptyFilename);
        }
        File file = new File(filename);
        if (file.isDirectory()) {
            messageList.newError(MSG_DIRECTORY_FILE, Messages.MessagesImportPMO_FilenameIsDirectory);
        }
        if (!(new File(filename).exists())) {
            messageList.newError(MSG_NO_EXIST_FILE, Messages.MessagesImportPMO_FileDoesNotExist);
        }
    }

    private void validateColumnDelimiter(MessageList messageList) {
        if (isCsvFileFormat() && isInvalidDelimiter()) {
            messageList.newError(MSG_NO_COLUMN_DELIMITER, Messages.MessagesImportPMO_noColumnDelimiter);
        }
    }

    private boolean isInvalidDelimiter() {
        return columnDelimiter == null;
    }

    private void validateIdColumIndex(MessageList messageList) {
        if (isCsvFileFormat() && isInvalidIndex(idColumnIndex)) {
            messageList.newError(MSG_NO_ID_COLUMN_INDEX, Messages.MessagesImportPMO_Msg_noIdColumnIndex);
        }
    }

    /**
     * An index is invalid if it is <code>null</code>, whitespace or any other non-numeric string.
     * An numeric index is invalid if it is less or equal to 0.
     */
    private boolean isInvalidIndex(String index) {
        return IpsStringUtils.isEmpty(index) || !StringUtils.isNumeric(index) || Integer.parseInt(index) <= 0;
    }

    private void validateTextColumnIndex(MessageList messageList) {
        if (isCsvFileFormat() && isInvalidIndex(textColumnIndex)) {
            messageList.newError(MSG_NO_TEXT_COLUMN_INDEX, Messages.MessagesImportPMO_Msg_NoTextColumnIndex);
        }
    }

    private void validateLocale(MessageList messageList) {
        if (supportedLanguage == null) {
            messageList.newError(MSG_NO_LOCALE, Messages.MessagesImportPMO_EmptyLocale);
        }
    }

    enum ImportFormat {

        CSV("csv"), //$NON-NLS-1$

        PROPERTIES("properties"); //$NON-NLS-1$

        private static final String FILE_WILDCARD = "*."; //$NON-NLS-1$

        private final String extension;

        ImportFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }

        public String getFilenamePattern() {
            return FILE_WILDCARD + extension;
        }

        /**
         * Reads the given file extension string and returns the matching format. If the given file
         * extension does not match a valid format this method returns {@link #CSV} as default. This
         * method never returns <code>null</code>.
         * 
         * @param fileExtension The file extension text without leading '.'
         * @return The matching format or {@link #CSV} as default.
         */
        public static ImportFormat getFormat(String fileExtension) {
            if (PROPERTIES.extension.equals(fileExtension)) {
                return PROPERTIES;
            } else {
                return CSV;
            }
        }

    }

}
