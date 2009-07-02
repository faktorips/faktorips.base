/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.testcase.IpsTestRunner;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Default implementation of the project naming conventions.
 * 
 * @author Daniel Hohenberger
 */
public class DefaultIpsProjectNamingConventions implements IIpsProjectNamingConventions {

    private IIpsProject ipsProject;

    private Map<IpsObjectType, String> errorMsgTxtNameIsEmpty = new HashMap<IpsObjectType, String>(1);
    private Map<IpsObjectType, String> errorMsgTxtNameIsQualified = new HashMap<IpsObjectType, String>(1);

    public static final char[] INVALID_RESOURCE_CHARACTERS;

    static {
        // setup the invalid names; @see OS
        // valid names and characters taken from
        // http://msdn.microsoft.com/library/default.asp?url=/library/en-us/fileio/fs/naming_a_file.asp
        // and from http://www.faqs.org/faqs/unix-faq/faq/part2/section-2.html
        // remark: wo don't differ between linux and windows because both could be used in
        // together (e.g. client and server installation), thus we use resriction for windows and
        // linux
        INVALID_RESOURCE_CHARACTERS = new char[] { '\\', '/', ':', '*', '?', '"', '<', '>', '|', '/', '\0' };
    }

    public DefaultIpsProjectNamingConventions(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;

        // initialize special error texts depending on the object type
        errorMsgTxtNameIsEmpty.put(IpsObjectType.PRODUCT_CMPT_TYPE,
                Messages.DefaultIpsProjectNamingConventions_msgMissingNameForProductCmpt);
        errorMsgTxtNameIsQualified.put(IpsObjectType.PRODUCT_CMPT_TYPE,
                Messages.DefaultIpsProjectNamingConventions_msgNameNotValidForProductCmpt);
    }

    private String getNameIsEmptyErrorText(IpsObjectType type) {
        String text = (String)errorMsgTxtNameIsEmpty.get(type);
        if (text == null) {
            text = Messages.DefaultIpsProjectNamingConventions_msgMissingName;
        }
        return text;
    }

    private String getNameIsQualifiedErrorText(IpsObjectType type) {
        String text = (String)errorMsgTxtNameIsQualified.get(type);
        if (text == null) {
            text = Messages.DefaultIpsProjectNamingConventions_msgNameMustNotBeQualified;
        }
        return text;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validateQualifiedIpsObjectName(IpsObjectType type, String name) throws CoreException {
        return validateIpsObjectNameInternal(type, name, true);
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validateUnqualifiedIpsObjectName(IpsObjectType type, String name) throws CoreException {
        ArgumentCheck.notNull(type);
        return validateIpsObjectNameInternal(type, name, false);
    }

    private MessageList validateIpsObjectNameInternal(IpsObjectType type, String name, boolean qualifiedCheck)
            throws CoreException {
        MessageList result = new MessageList();

        // common check for all ips object types
        if (StringUtils.isEmpty(name)) {
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
        } else if (IpsObjectType.BUSINESS_FUNCTION.equals(type)) {
            MessageList ml = validateNameForBusinessFunction(name, qualifiedCheck);
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
        if (IpsObjectType.PRODUCT_CMPT.equals(type)) {
            MessageList ml = validateNameForProductCmpt(name, qualifiedCheck);
            result.add(ml);
            return result;
        } else if (IpsObjectType.TABLE_CONTENTS.equals(type)) {
            MessageList ml = validateNameForTableContents(name, qualifiedCheck);
            result.add(ml);
            return result;
        } else if (IpsObjectType.TEST_CASE_TYPE.equals(type)) {
            MessageList ml = validateNameForTestCaseType(name, qualifiedCheck);
            result.add(ml);
            return result;
        } else if (IpsObjectType.TEST_CASE.equals(type)) {
            MessageList ml = validateNameForTestCase(name, qualifiedCheck);
            result.add(ml);
            return result;
        }
        return result;
    }

    private MessageList validateNameForPolicyCmptType(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    private MessageList validateNameForTestCase(String name, boolean qualifiedCheck) {
        MessageList ml = validateValidOsName(name, qualifiedCheck);
        if (!ml.containsErrorMsg()) {
            ml = IpsTestRunner.validateTestCaseName(name);
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

    private MessageList validateNameForBusinessFunction(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    private MessageList validateNameForEnumType(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck);
    }

    /*
     * Validate if the name is a valid os name
     */
    private MessageList validateValidOsName(String name, boolean qualifiedCheck) {
        MessageList ml = new MessageList();
        String unqualifiedName = qualifiedCheck ? StringUtil.unqualifiedName(name) : name;
        char[] chars = INVALID_RESOURCE_CHARACTERS;
        for (int i = 0; i < chars.length; i++) {
            if (unqualifiedName.indexOf(chars[i]) != -1) {
                ml.add(new Message(INVALID_NAME, NLS.bind(Messages.DefaultIpsProjectNamingConventions_msgNameNotValid,
                        unqualifiedName), Message.ERROR));
            }
        }
        return ml;
    }

    /*
     * Validate if the name is a valid java type identifier
     */
    private MessageList validateJavaTypeName(String name, boolean qualifiedCheck) {
        return validateJavaTypeName(name, qualifiedCheck, Messages.DefaultIpsProjectNamingConventions_msgNameNotValid,
                Messages.DefaultIpsProjectNamingConventions_msgNameIdDiscouraged);
    }

    /*
     * Special validation for product cmpt, the validation will be delegated to the
     * IProductCmptNamingStrategy
     */
    private MessageList validateNameForProductCmpt(String name, boolean qualifiedCheck) throws CoreException {
        IProductCmptNamingStrategy pns = ipsProject.getProductCmptNamingStrategy();
        // the validate will be delegated to the product cmpt naming strategy, only if the given
        // name is unqualified
        if (!qualifiedCheck) {
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
            IStatus status = JavaConventions.validateJavaTypeName(name, sourceLevel, complianceLevel);
            if (status.getSeverity() == IStatus.ERROR) {
                ml.add(new Message(INVALID_NAME, NLS.bind(msgNameNotValidError, name, status.getMessage()),
                        Message.ERROR));
                return ml;
            }
            if (status.getSeverity() == IStatus.WARNING) {
                ml.add(new Message(DISCOURAGED_NAME, NLS.bind(msgNameNotValidWarning, name, status.getMessage()),
                        Message.WARNING));
                return ml;
            }
        }
        return ml;
    }

    /**
     * A valid IPS package fragment name is either the empty String for the default package fragment
     * or a valid package package fragment name according to
     * <code>JavaConventions.validatePackageName</code>.
     * 
     * {@inheritDoc}
     */
    public MessageList validateIpsPackageName(String name) {
        MessageList ml = new MessageList();
        if (name.equals("")) { //$NON-NLS-1$
            return ml;
        }
        ml.add(validateJavaPackageName(name, Messages.DefaultIpsProjectNamingConventions_error,
                Messages.DefaultIpsProjectNamingConventions_warning));
        return ml;
    }

    private MessageList validateJavaPackageName(String name, String msgNameNotValidError, String msgNameNotValidWarning) {
        MessageList ml = new MessageList();
        String sourceLevel = getCompilerSourceLevel(ipsProject);
        String complianceLevel = getCompilerComplianceLevel(ipsProject);
        IStatus status = JavaConventions.validatePackageName(name, sourceLevel, complianceLevel);
        if (status.getSeverity() == IStatus.ERROR) {
            ml.add(new Message(INVALID_NAME, NLS.bind(msgNameNotValidError, name, status.getMessage()), Message.ERROR));
            return ml;
        }
        if (status.getSeverity() == IStatus.WARNING) {
            ml.add(new Message(DISCOURAGED_NAME, NLS.bind(msgNameNotValidWarning, name, status.getMessage()),
                    Message.WARNING));
            return ml;
        }
        return ml;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validateIpsPackageRootName(String name) throws CoreException {
        return new MessageList();
    }

    /**
     * {@inheritDoc}
     */
    public Message validateIfValidJavaIdentifier(String name,
            String text,
            Object validatedObject,
            IIpsProject ipsProject) throws CoreException {

        String sourceLevel = getCompilerSourceLevel(ipsProject);
        String complianceLevel = getCompilerComplianceLevel(ipsProject);
        IStatus status = JavaConventions.validateIdentifier(name, sourceLevel, complianceLevel);
        if (!status.isOK()) {
            return new Message(INVALID_NAME, text, Message.ERROR, validatedObject);
        }
        return null;
    }
    
    private String getCompilerComplianceLevel(IIpsProject ipsProject) {
        return ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
    }
    
    private String getCompilerSourceLevel(IIpsProject ipsProject) {
        return ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
    }
    
}
