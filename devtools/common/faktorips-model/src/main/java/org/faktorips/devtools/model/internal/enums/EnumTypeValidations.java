/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.enums.EnumTypeHierarchyVisitor;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.util.ArgumentCheck;

/**
 * A class that contains validations of the model class <code>IEnumType</code> that are also used in
 * the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeValidations {

    private EnumTypeValidations() {
        // Prohibit initialization.
    }

    /**
     * Validates whether the given super <code>IEnumType</code> exists in the IPS object path of the
     * given IPS project and that super <code>IEnumType</code> is abstract.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumType The <code>IEnumType</code> that might be invalid or <code>null</code> if that
     *            information cannot be supported.
     * @param superEnumTypeQualifiedName The qualified name of the super <code>IEnumType</code>.
     * @param ipsProject The IPS object path of this IPS project will be searched.
     * 
     * @throws IllegalArgumentException If <code>superEnumTypeQualifiedName</code> is an empty
     *             string.
     * @throws NullPointerException If <code>validationMessageList</code>,
     *             <code>superEnumTypeQualifiedName</code> or <code>ipsProject</code> is
     *             <code>null</code>.
     */
    public static void validateSuperEnumType(MessageList validationMessageList,
            IEnumType enumType,
            String superEnumTypeQualifiedName,
            IIpsProject ipsProject) {

        ArgumentCheck.notNull(new Object[] { validationMessageList, superEnumTypeQualifiedName, ipsProject });
        ArgumentCheck.isTrue(IpsStringUtils.isNotEmpty(superEnumTypeQualifiedName));

        // Super EnumType exists?
        IEnumType superEnumType = ipsProject.findEnumType(superEnumTypeQualifiedName);
        if (superEnumType == null) {
            String text = MessageFormat.format(Messages.EnumType_SupertypeDoesNotExist, superEnumTypeQualifiedName);
            Message message = new Message(
                    IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST,
                    text,
                    Message.ERROR,
                    enumType != null
                            ? new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) }
                            : new ObjectProperty[0]);
            validationMessageList.add(message);
            return;
        }

        // Super EnumType abstract?
        if (!(superEnumType.isAbstract())) {
            String text = MessageFormat.format(Messages.EnumType_SupertypeIsNotAbstract,
                    superEnumType.getQualifiedName());
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT, text, Message.ERROR,
                    enumType, IEnumType.PROPERTY_SUPERTYPE);
            validationMessageList.add(message);
        }
    }

    /**
     * Checks is the super type hierarchy of this enumeration type is valid. Therefore this method
     * checks if a cycle exists in the type hierarchy, if there is an enumeration type in the
     * hierarchy for which the super type cannot be found and if there exists a super type that is
     * not abstract. Last is a constraint specific to Faktor-IPS enumerations.
     * 
     * @param msgList The message list where messages are added to in cases of failing validations.
     * @param enumType The enumeration type that is validated.
     * @param ipsProject The <code>IpsProject</code> used as starting point for searches. Note: Not
     *            the <code>IpsProject</code> of the provided enumeration type is used within this
     *            method.
     * 
     * @throws IpsException If an exception occurs during processing.
     */
    public static void validateSuperTypeHierarchy(MessageList msgList, IEnumType enumType, IIpsProject ipsProject) {
        ArgumentCheck.notNull(new Object[] { msgList, ipsProject });

        IEnumType superEnumType = enumType.findSuperEnumType(ipsProject);
        if (superEnumType == null) {
            return;
        }
        SupertypeCollector collector = new SupertypeCollector(ipsProject);
        collector.start(superEnumType);
        if (collector.cycleDetected()) {
            String msg = MessageFormat.format(Messages.EnumType_cycleDetected, enumType.getQualifiedName());
            msgList.add(new Message(IEnumType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg, Message.ERROR, enumType,
                    IEnumType.PROPERTY_SUPERTYPE));
        } else {
            for (IEnumType iEnumType : collector.superTypes) {
                MessageList superResult = iEnumType.validate(ipsProject);
                if (!(superResult.isEmpty())) {
                    if (superResult.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST) != null
                            || superResult
                                    .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT) != null) {
                        String text = MessageFormat.format(Messages.EnumType_inconsistentHierarchy,
                                enumType.getQualifiedName());
                        msgList.add(new Message(IEnumType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY, text, Message.ERROR,
                                enumType, IEnumType.PROPERTY_SUPERTYPE));
                    }
                }
            }
        }
    }

    /**
     * Validates the qualified enumeration content name that has to be specified for an enumeration
     * type which delegates the its values to an enumeration content.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param msgList The message list where messages are added to in cases of failing validations
     * @param enumType The enumeration type that needs to be validated
     * @param enumTypeExtensible flag indicating whether the enumeration type is extensible by an
     *            enumeration content
     * @param enumContentName The qualified name of the enumeration content of the enumeration type
     *            that is validated
     * 
     * @throws NullPointerException If <code>msgList</code> or <code>enumContentName</code> is
     *             <code>null</code> .
     */
    public static void validateEnumContentName(MessageList msgList,
            IEnumType enumType,
            boolean enumTypeIsAbstract,
            boolean enumTypeExtensible,
            String enumContentName) {

        ArgumentCheck.notNull(new Object[] { msgList, enumContentName });

        // Name should not be empty if the EnumType defers it's values and is not abstract.
        if (enumTypeExtensible && !enumTypeIsAbstract) {
            if (enumContentName.length() == 0) {
                String text = Messages.EnumType_EnumContentNameEmpty;
                Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY, text, Message.ERROR,
                        enumType != null ? new ObjectProperty[] { new ObjectProperty(enumType,
                                IEnumType.PROPERTY_ENUM_CONTENT_NAME) } : new ObjectProperty[0]);
                msgList.add(message);
            }
        }
    }

    private static class SupertypeCollector extends EnumTypeHierarchyVisitor {

        private List<IEnumType> superTypes = new ArrayList<>();

        public SupertypeCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IEnumType currentType) {
            superTypes.add(currentType);
            return true;
        }

    }

}
