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

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.type.Messages;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class <b>Type</b> which are also used in the
 * creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Peter Erzberger
 */

public class TypeValidations {

    /**
     * Validates if there exists already a policy component type or product component type in the
     * ips object path. The method checks when a product component type is validated if a policy
     * component type with the same name exists within the ips objects path and vice versa.
     * 
     * @param otherIpsObjectType the IpsObjectType of the other type e.g. if a product component
     *            type is validated the IpsObjectType has to be policy component type
     * @param qualifiedName the qualified name of the type that is to validate
     * @param ipsProject the ips project
     * @param thisType the model object of the type that is to validate
     * @return a message if the validation fails otherwise <code>null</code>
     * 
     * @throws CoreException exceptions that a raised a delegated by this method
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
                    Messages.Type_msgOtherTypeWithSameQNameInDependentProject, new Object[] {
                            otherIpsObjectType.getId(), file.getIpsProject() }), Message.WARNING,
                    thisType != null ? new ObjectProperty[] { new ObjectProperty(thisType, null) }
                            : new ObjectProperty[0]);

        }
        return null;
    }

}
