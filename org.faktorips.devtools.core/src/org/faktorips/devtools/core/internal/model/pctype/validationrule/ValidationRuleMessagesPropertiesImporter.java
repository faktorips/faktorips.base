/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.util.IoUtil;
import org.faktorips.values.LocalizedString;

/**
 * This class implements an algorithm to import a properties file containing the validation messages
 * for {@link IValidationRule}s. The keys have to be the qualified rule names as provided by
 * {@link IValidationRule#getQualifiedRuleName()}.
 * 
 */
public class ValidationRuleMessagesPropertiesImporter extends ValidationRuleMessagesImportOperation {

    public static final int MSG_CODE_MISSING_MESSAGE = 1;

    public static final int MSG_CODE_ILLEGAL_MESSAGE = 2;

    private Properties properties;

    private List<String> importedMessageKeys;

    private MultiStatus missingMessages;

    private MultiStatus illegalMessages;

    public ValidationRuleMessagesPropertiesImporter(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        super(contents, root, locale);
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * This method imports the messages in the give file into the objects found in the specified
     * {@link IIpsPackageFragmentRoot}. The messages are set for the specified locale.
     */
    @Override
    protected IStatus importContent() {
        try {
            loadProperties();
            return importProperties();
        } catch (CoreException e) {
            return e.getStatus();
        } catch (IOException e) {
            return new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                    Messages.ValidationRuleMessagesPropertiesImporter_error_loadingPropertyFile, e);
        } finally {
            IoUtil.close(getContents());
        }

    }

    void loadProperties() throws IOException {
        setProperties(new Properties());
        properties.load(getContents());
    }

    IStatus importProperties() throws CoreException {
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
        importedMessageKeys = new ArrayList<String>();
        missingMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_MISSING_MESSAGE,
                Messages.ValidationRuleMessagesPropertiesImporter_status_missingMessage, null);
        illegalMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, MSG_CODE_ILLEGAL_MESSAGE,
                Messages.ValidationRuleMessagesPropertiesImporter_status_illegalMessage, null);
    }

    private List<String> importValidationMessages(List<IIpsSrcFile> allIpsSrcFiled) throws CoreException {
        for (IIpsSrcFile ipsSrcFile : allIpsSrcFiled) {
            if (!ipsSrcFile.isMutable()) {
                continue;
            }
            boolean dirtyState = ipsSrcFile.isDirty();
            IPolicyCmptType pcType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
            importValidationMessages(pcType);
            getMonitor().worked(1);
            if (!dirtyState && ipsSrcFile.isDirty()) {
                ipsSrcFile.save(false, new SubProgressMonitor(getMonitor(), 1));
            }
        }
        return importedMessageKeys;
    }

    private void importValidationMessages(IPolicyCmptType pcType) {
        List<IValidationRule> validationRules = pcType.getValidationRules();
        for (IValidationRule validationRule : validationRules) {
            String messageKey = validationRule.getQualifiedRuleName();
            String message = properties.getProperty(messageKey);
            if (updateValidationMessage(validationRule, message)) {
                importedMessageKeys.add(messageKey);
            } else {
                missingMessages.add(new Status(IStatus.WARNING, IpsPlugin.PLUGIN_ID, NLS.bind(
                        Messages.ValidationRuleMessagesPropertiesImporter_warning_ruleNotFound, new String[] {
                                validationRule.getName(), pcType.getQualifiedName(), messageKey })));
            }
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
        if (importedMessageKeys.size() < properties.size()) {
            for (Object key : properties.keySet()) {
                if (!importedMessageKeys.contains(key)) {
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
        if (!missingMessages.isOK()) {
            result.add(missingMessages);
        }
        if (result.isOK()) {
            return new Status(IStatus.OK, IpsPlugin.PLUGIN_ID, StringUtils.EMPTY);
        } else {
            return result;
        }
    }
}
