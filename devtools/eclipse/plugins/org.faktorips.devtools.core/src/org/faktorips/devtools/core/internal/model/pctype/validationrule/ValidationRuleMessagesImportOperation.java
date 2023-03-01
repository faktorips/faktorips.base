/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.LocalizedString;

public abstract class ValidationRuleMessagesImportOperation implements ICoreRunnable {

    public static final int MSG_CODE_MISSING_MESSAGE = 1;

    public static final int MSG_CODE_ILLEGAL_MESSAGE = 2;

    public static final int MSG_CODE_MULTIPLE_USED_MESSAGECODES = 3;

    private final InputStream contents;

    private final IIpsPackageFragmentRoot root;

    private final Locale locale;

    private IProgressMonitor monitor = new NullProgressMonitor();

    private IStatus resultStatus = new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, IpsStringUtils.EMPTY);

    private ValidationRuleIdentification identification = ValidationRuleIdentification.QUALIFIED_RULE_NAME;

    /**
     * The messageKey-messageText (key-value) pairs to import. Raw data e.g. from csv or properties
     * files.
     */
    private Map<String, String> contentMap;

    /**
     * Is used to create more detailed import warnings.
     * <p>
     * Maps a messageKey to the corresponding {@link IValidationRule} that has its message text
     * updated. If a message key is imported multiple times, the most recently processed
     * {@link IValidationRule} is stored.
     * <p>
     * Contains only entries for changed validation rules. Thus there are no entries for messageKeys
     * no corresponding validation rule was found for.
     */
    private Map<String, IValidationRule> importedMessageKeys;

    private MultiStatus missingMessages;

    private MultiStatus illegalMessages;

    private MultiStatus multipleUsedMessageCodes;

    private boolean enableWarningsForMissingMessages;

    public ValidationRuleMessagesImportOperation(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        this.contents = contents;
        this.root = root;
        this.locale = locale;
    }

    /**
     * @return Returns the resultStatus.
     */
    public IStatus getResultStatus() {
        return resultStatus;
    }

    public InputStream getContents() {
        return contents;
    }

    public IIpsPackageFragmentRoot getPackageFragmentRoot() {
        return root;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public void run(IProgressMonitor progressMonitor) {
        if (progressMonitor != null) {
            setMonitor(progressMonitor);
        }
        resultStatus = loadContent();
        if (resultStatus.getSeverity() != IStatus.ERROR) {
            resultStatus = importContentMap();
        }

    }

    protected abstract IStatus loadContent();

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public void setMethodOfIdentification(ValidationRuleIdentification identification) {
        this.identification = identification;
    }

    public ValidationRuleIdentification getMethodOfIdentification() {
        return identification;
    }

    void setKeyValueMap(Map<String, String> keyValueMap) {
        contentMap = keyValueMap;
    }

    protected IStatus importContentMap() {
        List<IIpsSrcFile> allPolicyCmptFiled = getPackageFragmentRoot().findAllIpsSrcFiles(
                IpsObjectType.POLICY_CMPT_TYPE);
        try {
            getMonitor().beginTask(Messages.ValidationRuleMessagesPropertiesImporter_status_importingMessages,
                    allPolicyCmptFiled.size() * 2 + 1);
            initResultFields();
            importValidationMessages(allPolicyCmptFiled);
            checkForIllegalMessages();
            return makeResultStatus();
        } finally {
            getMonitor().done();
        }
    }

    private void initResultFields() {
        importedMessageKeys = new HashMap<>();
        missingMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_MISSING_MESSAGE,
                Messages.ValidationRuleMessagesPropertiesImporter_status_missingMessage, null);
        illegalMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_ILLEGAL_MESSAGE,
                Messages.ValidationRuleMessagesPropertiesImporter_status_illegalMessage, null);
        multipleUsedMessageCodes = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_MULTIPLE_USED_MESSAGECODES,
                Messages.ValidationRuleCsvImporter_status_multipleUsedMessageCodes, null);
    }

    private void importValidationMessages(List<IIpsSrcFile> allIpsSrcFiled) {
        for (IIpsSrcFile ipsSrcFile : allIpsSrcFiled) {
            if (!ipsSrcFile.isMutable()) {
                continue;
            }
            boolean dirtyState = ipsSrcFile.isDirty();
            IPolicyCmptType pcType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
            importValidationMessages(pcType);
            getMonitor().worked(1);
            if (!dirtyState && ipsSrcFile.isDirty()) {
                ipsSrcFile.save(SubMonitor.convert(getMonitor(), 1));
            }
        }
    }

    private void importValidationMessages(IPolicyCmptType pcType) {
        List<IValidationRule> validationRules = pcType.getValidationRules();
        for (IValidationRule validationRule : validationRules) {
            String messageKey = getMethodOfIdentification().getIdentifier(validationRule);
            checkForMultipleUsedMessageCodes(messageKey, validationRule);
            String message = contentMap.get(messageKey);
            if (updateValidationMessage(validationRule, message)) {
                importedMessageKeys.put(messageKey, validationRule);
            } else {
                missingMessages.add(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID, NLS.bind(
                        Messages.ValidationRuleMessagesPropertiesImporter_warning_ruleNotFound, new String[] {
                                validationRule.getName(), pcType.getQualifiedName(), messageKey })));
            }
        }
    }

    private void checkForMultipleUsedMessageCodes(String messageKey, IValidationRule validationRule) {
        if (importedMessageKeys.containsKey(messageKey)) {
            multipleUsedMessageCodes.add(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID, NLS.bind(
                    Messages.ValidationRuleCsvImporter_warning_multipleUsedMessageCodes, new String[] { messageKey,
                            validationRule.getQualifiedRuleName(),
                            importedMessageKeys.get(messageKey).getQualifiedRuleName() })));
        }
    }

    private boolean updateValidationMessage(IValidationRule validationRule, String message) {
        if (message == null) {
            return false;
        } else {
            validationRule.getMessageText().add(new LocalizedString(getLocale(), message));
            return true;
        }
    }

    private void checkForIllegalMessages() {
        if (importedMessageKeys.size() < contentMap.size()) {
            for (Object key : contentMap.keySet()) {
                if (!importedMessageKeys.containsKey(key)) {
                    illegalMessages.add(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID, NLS.bind(
                            Messages.ValidationRuleMessagesPropertiesImporter_warning_invalidMessageKey, key)));
                }
            }
        }
        getMonitor().worked(1);
    }

    private IStatus makeResultStatus() {
        MultiStatus result = new MultiStatus(IpsPlugin.PLUGIN_ID, 0,
                Messages.ValidationRuleMessagesPropertiesImporter_status_problemsDuringImport, null);
        if (!illegalMessages.isOK()) {
            result.add(illegalMessages);
        }
        if (!missingMessages.isOK() && isEnableWarningsForMissingMessages()) {
            result.add(missingMessages);
        }
        if (!multipleUsedMessageCodes.isOK()) {
            result.add(multipleUsedMessageCodes);
        }
        if (result.isOK()) {
            return new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, IpsStringUtils.EMPTY);
        } else {
            return result;
        }
    }

    private boolean isEnableWarningsForMissingMessages() {
        return enableWarningsForMissingMessages;
    }

    /**
     * @param enableWarningsForMissingMessages Sets if warnings concerning missing messages should
     *            be displayed after the import.
     */
    public void setEnableWarningsForMissingMessages(boolean enableWarningsForMissingMessages) {
        this.enableWarningsForMissingMessages = enableWarningsForMissingMessages;
    }

}
