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
import java.util.Iterator;
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
 * A class that contains validations of the model class <code>IEnumType</code> that are also used in
 * the creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumTypeValidations {

    /**
     * Validates whether the given super enum type exists in the ips object path of the given ips
     * project and that super enum type is abstract.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param validationMessageList The message list to save validation messages into.
     * @param enumType The enum type that might be invalid or <code>null</code> if that information
     *            cannot be supported.
     * @param superEnumTypeQualifiedName The qualified name of the super enum type.
     * @param ipsProject The ips object path of this ips project will be searched.
     * 
     * @throws CoreException If an error occurs while searching for the super enum type.
     * @throws IllegalArgumentException If <code>superEnumTypeQualifiedName</code> is an empty
     *             string.
     * @throws NullPointerException If <code>validationMessageList</code>,
     *             <code>superEnumTypeQualifiedName</code> or <code>ipsProject</code> is
     *             <code>null</code>.
     */
    public static void validateSuperEnumType(MessageList validationMessageList,
            IEnumType enumType,
            String superEnumTypeQualifiedName,
            IIpsProject ipsProject) throws CoreException {

        ArgumentCheck.notNull(new Object[] { validationMessageList, superEnumTypeQualifiedName, ipsProject });
        ArgumentCheck.isTrue(!(superEnumTypeQualifiedName.equals(""))); //$NON-NLS-1$

        // Super enum type exists?
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

        // Super enum type abstract?
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
     * not abstract. Last is a constraint specific to faktor ips enumerations.
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
            for (Iterator<IEnumType> it = collector.superTypes.iterator(); it.hasNext();) {
                MessageList superResult = it.next().validate(ipsProject);
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
     * Validates the package specification for enum contents that might want to reference this enum
     * type.
     * <p>
     * Adds validation messages to the given message list.
     * 
     * @param msgList The message list where messages are added to in cases of failing validations.
     * @param enumType The enum type that might be invalid or <code>null</code> if that information
     *            cannot be supported.
     * @param valuesDeferredToContent Flag indicating whether the enum type to validate does contain
     *            values.
     * @param enumContentPackageFragment The enum content package fragment of the enum type to
     *            validate.
     * 
     * @throws NullPointerException If <tt>msgList</tt> or <tt>enumContentPackageFragment</tt> is
     *             <tt>null</tt>.
     */
    public static void validateEnumContentPackageFragment(MessageList msgList,
            IEnumType enumType,
            boolean valuesDeferredToContent,
            String enumContentPackageFragment) {

        ArgumentCheck.notNull(new Object[] { msgList, enumContentPackageFragment });

        // Package specification should not be empty if this enum type does not contain values.
        if (valuesDeferredToContent) {
            if (enumContentPackageFragment.equals("")) {
                String text = Messages.EnumType_EnumContentPackageFragmentEmpty;
                Message message = new Message(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_PACKAGE_FRAGMENT_EMPTY, text,
                        Message.INFO, enumType != null ? new ObjectProperty[] { new ObjectProperty(enumType,
                                IEnumType.PROPERTY_ENUM_CONTENT_PACKAGE_FRAGMENT) } : new ObjectProperty[0]);
                msgList.add(message);
            }
        }
    }

    /** Prohibits initialization. */
    private EnumTypeValidations() {

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
