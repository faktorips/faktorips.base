/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.type.Messages;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations for the model class {@link IType} which are also used in the
 * creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Peter Erzberger
 */

public class TypeValidations {

    private TypeValidations() {
        // Utility class not to be instantiated.
    }

    /**
     * Validates if there exists already a policy component type or product component type with the
     * same name in the IPS object path. The method checks when a product component type is
     * validated if a policy component type with the same name exists within the IPS object path and
     * vice versa.
     * <p>
     * Returns a message if the validation fails, otherwise <code>null</code>.
     * 
     * @param otherIpsObjectType The {@link IpsObjectType} of the other type. If for example a
     *            product component type is validated, this has to be
     *            {@link IpsObjectType#POLICY_CMPT_TYPE}.
     * @param qualifiedName The qualified name of the type that is to validate.
     * @param ipsProject The IPS project.
     * @param thisType The model object of the type that is to validate.
     * 
     * @throws CoreException Any raised exceptions are delegated by this method.
     */
    public static Message validateOtherTypeWithSameNameTypeInIpsObjectPath(IpsObjectType otherIpsObjectType,
            String qualifiedName,
            IIpsProject ipsProject,
            IType thisType) throws CoreException {

        IIpsSrcFile file = ipsProject.findIpsSrcFile(otherIpsObjectType, qualifiedName);
        if (file != null) {
            if (ipsProject.equals(file.getIpsProject())) {
                return new Message(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_EXISTS, NLS.bind(
                        Messages.Type_msgOtherTypeWithSameQNameInSameProject, otherIpsObjectType.getDisplayName()),
                        Message.ERROR, thisType != null ? new ObjectProperty[] { new ObjectProperty(thisType, null) }
                                : new ObjectProperty[0]);
            }
            return new Message(IType.MSGCODE_OTHER_TYPE_WITH_SAME_NAME_IN_DEPENDENT_PROJECT_EXISTS, NLS.bind(
                    Messages.Type_msgOtherTypeWithSameQNameInDependentProject,
                    new Object[] { otherIpsObjectType.getId(), file.getIpsProject() }), Message.WARNING,
                    thisType != null ? new ObjectProperty[] { new ObjectProperty(thisType, null) }
                            : new ObjectProperty[0]);

        }
        return null;
    }

    /**
     * Validates the type hierarchy of the given type. Add
     * 
     * @param type The type of which you want to validate the hierarchy
     * @param ipsProject the project that is used as reference to find other objects
     */
    public static Message validateTypeHierachy(IType type, IIpsProject ipsProject) {
        if (StringUtils.isEmpty(type.getSupertype())) {
            return null;
        }
        IType superType = type.findSupertype(ipsProject);
        if (superType == null) {
            String text = NLS.bind(Messages.Type_msg_supertypeNotFound, type.getSupertype());
            return new Message(IType.MSGCODE_SUPERTYPE_NOT_FOUND, text, Message.ERROR, type, IType.PROPERTY_SUPERTYPE);
        }

        SupertypesValidator validator = new SupertypesValidator(ipsProject);

        validator.start(superType);

        if (!validator.result) {
            String text = Messages.Type_msg_TypeHierarchyInconsistent;
            return new Message(IType.MSGCODE_INCONSISTENT_TYPE_HIERARCHY, text, Message.ERROR, type,
                    IType.PROPERTY_SUPERTYPE);
        }

        if (validator.cycleDetected()) {
            String msg = Messages.Type_msg_cycleInTypeHierarchy;
            return new Message(IType.MSGCODE_CYCLE_IN_TYPE_HIERARCHY, msg.toString(), Message.ERROR, type,
                    IType.PROPERTY_SUPERTYPE);
        }
        return null;
    }

    private static class SupertypesValidator extends TypeHierarchyVisitor<IType> {

        private boolean result;

        public SupertypesValidator(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) {
            if (StringUtils.isEmpty(currentType.getSupertype())) {
                // there should be no more super type
                result = true;
                return false;
            }
            result = isNull(currentType);
            return result;
        }

        private boolean isNull(IType currentType) {
            return currentType.findSupertype(getIpsProject()) != null;
        }

    }

}
