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
import org.faktorips.util.message.MessageList;

public class MessagesImportPMO extends PresentationModelObject {

    public static final String PROPERTY_FILE_NAME = "fileName"; //$NON-NLS-1$

    public static final String PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT = "ipsPackageFragmentRoot"; //$NON-NLS-1$

    public static final String PROPERTY_SUPPORTED_LANGUAGE = "supportedLanguage"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT = "format"; //$NON-NLS-1$

    public static final String PROPERTY_COLUMN_DELIMITER = "columnDelimiter"; //$NON-NLS-1$

    public static final String PROPERTY_IDENTIFIER_COLUMN_INDEX = "identifierColumnIndex"; //$NON-NLS-1$

    public static final String PROPERTY_TEXT_COLUMN_INDEX = "textColumnIndex"; //$NON-NLS-1$

    public static final String PROPERTY_RULE_IDENTIFIER = "ruleIdentifier"; //$NON-NLS-1$

    public static final String PROPERTY_ENABLE_WARNINGS = "enableWarnings"; //$NON-NLS-1$

    public static final String PROPERTY_FORMAT_SETTINGS_VISIBLE = "formatSettingsVisible"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "MESSAGES_IMPORT_WIZARD-"; //$NON-NLS-1$

    public static final String MSG_EMPTY_FILE = MSGCODE_PREFIX + "noFile"; //$NON-NLS-1$

    public static final String MSG_DIRECTORY_FILE = MSGCODE_PREFIX + "noDirectory"; //$NON-NLS-1$

    public static final String MSG_NO_EXIST_FILE = MSGCODE_PREFIX + "noExistFile"; //$NON-NLS-1$

    public static final String MSG_INVALID_TARGET = MSGCODE_PREFIX + "invalidTarget"; //$NON-NLS-1$

    public static final String MSG_NO_LOCALE = MSGCODE_PREFIX + "noLocale"; //$NON-NLS-1$

    public static final String FORMAT_PROPERTIES_FILE = "formatPropertyFile"; //$NON-NLS-1$

    public static final String FORMAT_CSV_FILE = "formatCsvFile"; //$NON-NLS-1$

    public static final String MSG_NO_COLUMN_DELIMITER = MSGCODE_PREFIX + "noColumnDelimiter"; //$NON-NLS-1$

    public static final String MSG_NO_ID_COLUMN_INDEX = MSGCODE_PREFIX + "noIDColumnIndex"; //$NON-NLS-1$

    public static final String MSG_NO_TEXT_COLUMN_INDEX = MSGCODE_PREFIX + "noTextColumnIndex"; //$NON-NLS-1$

    private String fileName = StringUtils.EMPTY;

    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private ISupportedLanguage supportedLanguage;

    private String format = FORMAT_CSV_FILE;

    private Character columnDelimiter = ';';

    private String idColumnIndex = "1"; //$NON-NLS-1$

    private String textColumnIndex = "2"; //$NON-NLS-1$

    private boolean enableWarnings = false;

    private ValidationRuleIdentification ruleIdentifier = ValidationRuleIdentification.QUALIFIED_RULE_NAME;

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

    public void setColumnDelimiter(Character columnDelimiter) {
        Character oldValue = this.columnDelimiter;
        this.columnDelimiter = columnDelimiter;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_COLUMN_DELIMITER, oldValue, columnDelimiter));
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
     * @param textColumnIndex The (one based) index of the column to be used as text.
     */
    public void setTextColumnIndex(String textColumnIndex) {
        String oldValue = this.textColumnIndex;
        this.textColumnIndex = textColumnIndex;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_TEXT_COLUMN_INDEX, oldValue, textColumnIndex));
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
     * @return Returns the ipsPackageFragmentRoot.
     */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return ipsPackageFragmentRoot;
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
     * @return Returns the {@link ISupportedLanguage}.
     */
    public ISupportedLanguage getSupportedLanguage() {
        return supportedLanguage;
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

    /**
     * @return Returns the formatIdentifier.
     */
    public String getIdentifierColumnIndex() {
        return idColumnIndex;
    }

    /**
     * @return Returns the formatColumn.
     */
    public String getTextColumnIndex() {
        return textColumnIndex;
    }

    /**
     * @return Returns the identification.
     */
    public ValidationRuleIdentification getRuleIdentifier() {
        return ruleIdentifier;
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
        String filename = getFileName();
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
        if (isCsvFileFormat() && isDelimiterInvalid()) {
            messageList.newError(MSG_NO_COLUMN_DELIMITER, Messages.MessagesImportPMO_noColumnDelimiter);
        }
    }

    /**
     * Returns <code>true</code> if the file format is set to {@link #FORMAT_CSV_FILE}, otherwise
     * <code>false</code>.
     */
    public boolean isCsvFileFormat() {
        return FORMAT_CSV_FILE.equals(format);
    }

    private boolean isDelimiterInvalid() {
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
        return StringUtils.isEmpty(index) || !StringUtils.isNumeric(index) || Integer.parseInt(index) <= 0;
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

    public boolean isFormatSettingsVisible() {
        return isCsvFileFormat();
    }

}
