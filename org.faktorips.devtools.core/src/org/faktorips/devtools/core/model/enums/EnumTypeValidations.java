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

package org.faktorips.devtools.core.model.enums;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeHierachyVisitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <tt>IEnumType</tt> that are also used in the
 * creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeValidations {

    /**
     * Validates whether the given super <tt>IEnumType</tt> exists in the IPS object path of the
     * given IPS project and that super <tt>IEnumType</tt> is abstract.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumType The <tt>IEnumType</tt> that might be invalid or <tt>null</tt> if that
     *            information cannot be supported.
     * @param superEnumTypeQualifiedName The qualified name of the super <tt>IEnumType</tt>.
     * @param ipsProject The IPS object path of this IPS project will be searched.
     * 
     * @throws CoreException If an error occurs while searching for the super <tt>IEnumType</tt>.
     * @throws IllegalArgumentException If <tt>superEnumTypeQualifiedName</tt> is an empty string.
     * @throws NullPointerException If <tt>validationMessageList</tt>,
     *             <tt>superEnumTypeQualifiedName</tt> or <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public static void validateSuperEnumType(MessageList validationMessageList,
            IEnumType enumType,
            String superEnumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, superEnumTypeQualifiedName, ipsProject });
        ArgumentCheck.isTrue(!(superEnumTypeQualifiedName.equals(""))); //$NON-NLS-1$

        // Super EnumType exists?
        IEnumType superEnumType = ipsProject.findEnumType(superEnumTypeQualifiedName);
        if (superEnumType == null) {
            String text = NLS.bind(Messages.EnumType_SupertypeDoesNotExist, superEnumTypeQualifiedName);
            Message message = new Message(
                    IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST,
                    text,
                    Message.ERROR,
                    enumType != null ? new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) }
                            : new ObjectProperty[0]);
            validationMessageList.add(message);
            return;
        }

        // Super EnumType abstract?
        if (!(superEnumType.isAbstract())) {
            String text = NLS.bind(Messages.EnumType_SupertypeIsNotAbstract, superEnumType.getQualifiedName());
            Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT, text, Message.ERROR,
                    new ObjectProperty[] { new ObjectProperty(enumType, IEnumType.PROPERTY_SUPERTYPE) });
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
     * @param ipsProject The <tt>IpsProject</tt> used as starting point for searches. Note: Not the
     *            <tt>IpsProject</tt> of the provided enumeration type is used within this method.
     * 
     * @throws CoreException If an exception occurs during processing.
     */
    public static void validateSuperTypeHierarchy(MessageList msgList, IEnumType enumType, IIpsProject ipsProject)
            throws CoreException {

        ArgumentCheck.notNull(new Object[] { msgList, ipsProject });

        IEnumType superEnumType = enumType.findSuperEnumType(ipsProject);
        if (superEnumType == null) {
            return;
        }
        SupertypeCollector collector = new SupertypeCollector(ipsProject);
        collector.start(superEnumType);
        if (collector.cycleDetected()) {
            String msg = NLS.bind(Messages.EnumType_cycleDetected, enumType.getQualifiedName());
            msgList.add(new Message(IEnumType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg, Message.ERROR, enumType,
                    IEnumType.PROPERTY_SUPERTYPE));
        } else {
            for (IEnumType iEnumType : collector.superTypes) {
                MessageList superResult = iEnumType.validate(ipsProject);
                if (!(superResult.isEmpty())) {
                    if (superResult.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST) != null
                            || superResult.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT) != null) {
                        String text = NLS.bind(Messages.EnumType_inconsistentHierarchy, enumType.getQualifiedName());
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
     * @param valuesDeferredToContent flag indicating whether the enumeration type is supposed to
     *            contain values by itself or if it defers the values to an enumeration content
     * @param enumContentName The qualified name of the enumeration content of the enumeration type
     *            that is validated
     * 
     * @throws NullPointerException If <tt>msgList</tt> or <tt>enumContentName</tt> is <tt>null</tt>
     *             .
     */
    public static void validateEnumContentName(MessageList msgList,
            IEnumType enumType,
            boolean enumTypeIsAbstract,
            boolean valuesDeferredToContent,
            String enumContentName) {

        ArgumentCheck.notNull(new Object[] { msgList, enumContentName });

        // Name should not be empty if the EnumType defers it's values and is not abstract.
        if (valuesDeferredToContent && !enumTypeIsAbstract) {
            if (enumContentName.length() == 0) {
                String text = Messages.EnumType_EnumContentNameEmpty;
                Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY, text, Message.ERROR,
                        enumType != null ? new ObjectProperty[] { new ObjectProperty(enumType,
                                IEnumType.PROPERTY_ENUM_CONTENT_NAME) } : new ObjectProperty[0]);
                msgList.add(message);
            }
        }
    }

    private EnumTypeValidations() {
        // Prohibit initialization.
    }

    private static class SupertypeCollector extends EnumTypeHierachyVisitor {

        private List<IEnumType> superTypes = new ArrayList<IEnumType>();

        public SupertypeCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IEnumType currentType) throws CoreException {
            superTypes.add(currentType);
            return true;
        }

    }

}
