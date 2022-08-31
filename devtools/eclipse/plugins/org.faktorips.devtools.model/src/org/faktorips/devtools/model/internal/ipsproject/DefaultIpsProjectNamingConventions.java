/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.mapping.SeverityMapping;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaConventions;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * Default implementation of the project naming conventions.
 * 
 * @author Daniel Hohenberger
 */
public class DefaultIpsProjectNamingConventions implements IIpsProjectNamingConventions {

    /*
     * setup the invalid names; @see OS valid names and characters taken from http://msdn.microsoft
     * .com/library/default.asp?url=/library/en-us/fileio/fs/naming_a_file.asp and from
     * http://www.faqs.org/faqs/unix-faq/faq/part2/section-2.html remark: we don't differ between
     * Linux and Windows because both could be used together (e.g. client and server installation),
     * thus we use restrictions for Windows and Linux
     */
    public static final char[] INVALID_RESOURCE_CHARACTERS = { '\\', '/', ':', '*', '?', '"', '<', '>', '|',
            '/', '\0' };

    /**
     * Characters which are used within the test runner protocol and therefore forbidden to use
     * inside a test case name
     */
    public static final String FORBIDDEN_CHARACTERS_IN_TESTCASENAME = "\\[\\]{},:"; //$NON-NLS-1$
    private static final Pattern FORBIDDEN_CHARACTERS_IN_TESTCASENAME_PATTERN = Pattern
            .compile("[" + FORBIDDEN_CHARACTERS_IN_TESTCASENAME + "]"); //$NON-NLS-1$ //$NON-NLS-2$

    private IIpsProject ipsProject;

    private Map<IpsObjectType, String> errorMsgTxtNameIsEmpty = new HashMap<>(1);
    private Map<IpsObjectType, String> errorMsgTxtNameIsQualified = new HashMap<>(1);

    public DefaultIpsProjectNamingConventions(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;

        // initialize special error texts depending on the object type
        errorMsgTxtNameIsEmpty.put(IpsObjectType.PRODUCT_CMPT_TYPE,
                Messages.DefaultIpsProjectNamingConventions_msgMissingNameForProductCmpt);
        errorMsgTxtNameIsQualified.put(IpsObjectType.PRODUCT_CMPT_TYPE,
                Messages.DefaultIpsProjectNamingConventions_msgNameNotValidForProductCmpt);
    }

    private String getNameIsEmptyErrorText(IpsObjectType type) {
        String text = errorMsgTxtNameIsEmpty.get(type);
        if (text == null) {
            text = Messages.DefaultIpsProjectNamingConventions_msgMissingName;
        }
        return text;
    }

    private String getNameIsQualifiedErrorText(IpsObjectType type) {
        String text = errorMsgTxtNameIsQualified.get(type);
        if (text == null) {
            text = Messages.DefaultIpsProjectNamingConventions_msgNameMustNotBeQualified;
        }
        return text;
    }

    @Override
    public MessageList validateQualifiedIpsObjectName(IpsObjectType type, String name) {
        return validateIpsObjectNameInternal(type, name, true);
    }

    @Override
    public MessageList validateUnqualifiedIpsObjectName(IpsObjectType type, String name) {
        ArgumentCheck.notNull(type);
        return validateIpsObjectNameInternal(type, name, false);
    }

    private MessageList validateIpsObjectNameInternal(IpsObjectType type, String name, boolean qualifiedCheck) {
        MessageList result = new MessageList();

        // common check for all IPS object types
        if (IpsStringUtils.isEmpty(name)) {
            String text = getNameIsEmptyErrorText(type);
            result.add(new Message(NAME_IS_MISSING, text, Message.ERROR));
            return result;
        }
        if (!qualifiedCheck) {
            if (name.indexOf('.') != -1) {
                String text = getNameIsQualifiedErrorText(type);
                result.add(new Message(NAME_IS_QUALIFIED, text, Message.ERROR));
                return result;
            }
        }

        if (IpsObjectType.ENUM_TYPE.equals(type)) {
            MessageList ml = validateNameForEnumType(name, qualifiedCheck);
            result.add(ml);
            return result;
        } else if (IpsObjectType.POLICY_CMPT_TYPE.equals(type)) {
            MessageList ml = validateNameForPolicyCmptType(name, qualifiedCheck);
            result.add(ml);
            return result;
        } else if (IpsObjectType.PRODUCT_CMPT_TYPE.equals(type)) {
            MessageList ml = validateNameForProductCmptType(name, qualifiedCheck);
            result.add(ml);
            return result;
        } else if (IpsObjectType.TABLE_STRUCTURE.equals(type)) {
            MessageList ml = validateNameForTableStructure(name, qualifiedCheck);
            result.add(ml);
            return result;
        }
        if (IpsObjectType.PRODUCT_CMPT.equals(type) || IpsObjectType.PRODUCT_TEMPLATE.equals(type)) {
            MessageList ml = validateNameForProductCmpt(name, qualifiedCheck);
            result.add(ml);
        } else if (IpsObjectType.TABLE_CONTENTS.equals(type)) {
            MessageList ml = validateNameForTableContents(name, qualifiedCheck);
            result.add(ml);
        } else if (IpsObjectType.TEST_CASE_TYPE.equals(type)) {
            MessageList ml = validateNameForTestCaseType(name, qualifiedCheck);
            result.add(ml);
        } else if (IpsObjectType.TEST_CASE.equals(type)) {
            MessageList ml = validateNameForTestCase(name, qualifiedCheck);
            result.add(ml);
        }
        return result;
    }

    private MessageList validateNameForPolicyCmptType(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    private MessageList validateNameForTestCase(String name, boolean qualifiedCheck) {
        MessageList ml = validateValidOsName(name, qualifiedCheck);
        if (!ml.containsErrorMsg()) {
            ml = validateTestCaseName(name);
        }
        return ml;
    }

    /**
     * Validate if the test case name is a valid name.
     */
    private static MessageList validateTestCaseName(String testCaseName) {
        MessageList ml = new MessageList();
        boolean matches = FORBIDDEN_CHARACTERS_IN_TESTCASENAME_PATTERN.matcher(testCaseName).find();
        if (matches) {
            ml.add(new Message(INVALID_NAME,
                    MessageFormat.format(Messages.IpsTestRunner_validationErrorInvalidName, testCaseName,
                            FORBIDDEN_CHARACTERS_IN_TESTCASENAME.replace("\\", "")), //$NON-NLS-1$ //$NON-NLS-2$
                    Message.ERROR));
        }
        return ml;
    }

    private MessageList validateNameForTestCaseType(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    private MessageList validateNameForTableContents(String name, boolean qualifiedCheck) {
        return validateValidOsName(name, qualifiedCheck);
    }

    private MessageList validateNameForTableStructure(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    private MessageList validateNameForProductCmptType(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    private MessageList validateNameForEnumType(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    /**
     * Validate if the name is a valid os name
     */
    private MessageList validateValidOsName(String name, boolean qualifiedCheck) {
        MessageList ml = new MessageList();
        String unqualifiedName = qualifiedCheck ? StringUtil.unqualifiedName(name) : name;
        char[] chars = INVALID_RESOURCE_CHARACTERS;
        for (char c : chars) {
            if (unqualifiedName.indexOf(c) != -1) {
                ml.add(new Message(INVALID_NAME,
                        MessageFormat.format(Messages.DefaultIpsProjectNamingConventions_msgNameNotValid,
                                unqualifiedName),
                        Message.ERROR));
            }
        }
        return ml;
    }

    /**
     * Validate if the name is a valid java type identifier
     */
    @Override
    public MessageList validateJavaTypeName(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck, Messages.DefaultIpsProjectNamingConventions_msgNameNotValid,
                Messages.DefaultIpsProjectNamingConventions_msgNameIdDiscouraged);
    }

    /**
     * Special validation for product cmpt, the validation will be delegated to the
     * IProductCmptNamingStrategy
     */
    private MessageList validateNameForProductCmpt(String name, boolean qualifiedCheck) {
        IProductCmptNamingStrategy pns = ipsProject.getProductCmptNamingStrategy();
        // the validate will be delegated to the product cmpt naming strategy, only if the given
        // name is unqualified
        if (!qualifiedCheck && pns != null) {
            return pns.validate(name);
        }
        return null;
    }

    private MessageList validateJavaTypeName(String name,
            boolean qualifiedCheck,
            String msgNameNotValidError,
            String msgNameNotValidWarning) {

        MessageList ml = new MessageList();
        if (!qualifiedCheck) {
            String sourceLevel = getCompilerSourceLevel(ipsProject);
            String complianceLevel = getCompilerComplianceLevel(ipsProject);
            Severity validationResult = Severity.NONE;
            String validationMessage = IpsStringUtils.EMPTY;
            if (Abstractions.isEclipseRunning()) {
                IStatus status = JavaConventions.validateJavaTypeName(name, sourceLevel, complianceLevel);
                validationResult = SeverityMapping.toIps(status.getSeverity());
                validationMessage = status.getMessage();
            } else {
                MessageList validatePackageName = PlainJavaConventions.validateTypeName(name);
                validationResult = validatePackageName.getSeverity();
                validationMessage = validatePackageName.getText();
            }
            switch (validationResult) {
                case ERROR:
                    ml.add(new Message(INVALID_NAME,
                            MessageFormat.format(msgNameNotValidError, name, validationMessage),
                            Message.ERROR));
                    break;
                case WARNING:
                    ml.add(new Message(DISCOURAGED_NAME,
                            MessageFormat.format(msgNameNotValidWarning, name, validationMessage),
                            Message.WARNING));
                    break;
                default:
                    break;
            }
        }
        return ml;
    }

    /**
     * A valid IPS package fragment name is either the empty String for the default package fragment
     * or a valid package package fragment name according to
     * <code>JavaConventions.validatePackageName</code>.
     */
    @Override
    public MessageList validateIpsPackageName(String name) {
        MessageList ml = new MessageList();
        if (IpsStringUtils.isBlank(name)) {
            return ml;
        }
        ml.add(validateJavaPackageName(name, Messages.DefaultIpsProjectNamingConventions_error,
                Messages.DefaultIpsProjectNamingConventions_warning));
        return ml;
    }

    private MessageList validateJavaPackageName(String name,
            String msgNameNotValidError,
            String msgNameNotValidWarning) {
        MessageList ml = new MessageList();
        String sourceLevel = getCompilerSourceLevel(ipsProject);
        String complianceLevel = getCompilerComplianceLevel(ipsProject);
        Severity validationResult = Severity.NONE;
        String validationMessage = IpsStringUtils.EMPTY;
        if (Abstractions.isEclipseRunning()) {
            IStatus status = JavaConventions.validatePackageName(name, sourceLevel, complianceLevel);
            validationResult = SeverityMapping.toIps(status.getSeverity());
            validationMessage = status.getMessage();
        } else {
            MessageList validatePackageName = PlainJavaConventions.validatePackageName(name);
            validationResult = validatePackageName.getSeverity();
            validationMessage = validatePackageName.getText();
        }
        switch (validationResult) {
            case ERROR:
                ml.add(new Message(INVALID_NAME, MessageFormat.format(msgNameNotValidError, name, validationMessage),
                        Message.ERROR));
                break;
            case WARNING:
                ml.add(new Message(DISCOURAGED_NAME,
                        MessageFormat.format(msgNameNotValidWarning, name, validationMessage),
                        Message.WARNING));
                break;
            default:
                break;
        }
        return ml;
    }

    @Override
    public MessageList validateIpsPackageRootName(String name) {
        return new MessageList();
    }

    @Override
    public Message validateIfValidJavaIdentifier(String name,
            String text,
            Object validatedObject,
            IIpsProject ipsProject) {

        String sourceLevel = getCompilerSourceLevel(ipsProject);
        String complianceLevel = getCompilerComplianceLevel(ipsProject);
        IStatus status = JavaConventions.validateIdentifier(name, sourceLevel, complianceLevel);
        if (!status.isOK()) {
            return new Message(INVALID_NAME, text, Message.ERROR, validatedObject);
        }
        return null;
    }

    private String getCompilerComplianceLevel(IIpsProject ipsProject) {
        return ipsProject.getJavaProject().getSourceVersion().toString();
    }

    private String getCompilerSourceLevel(IIpsProject ipsProject) {
        return ipsProject.getJavaProject().getSourceVersion().toString();
    }

}
