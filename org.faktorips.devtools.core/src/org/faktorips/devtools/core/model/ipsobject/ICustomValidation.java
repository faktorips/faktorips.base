/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

/**
 * Interface for custom validations. A custom validation is a validation that is not part of the
 * Faktor-IPS meta model but is added for an individual purpose / project.
 * 
 * @author Jan Ortmann
 */
public interface ICustomValidation<T extends IIpsObjectPartContainer> {

    /**
     * Returns the design time model class (or the published interface) that is extended with this
     * custom validation. If an instances of the extended class is validated, it is passed to
     * {@link #validate(IIpsObjectPartContainer, IIpsProject)} method to execute the custom
     * validation.
     * 
     *@return The extended class or interface of the Faktor-IPS model, e.g. IAttribute or
     *         IPolicyCmptType.
     */
    Class<T> getExtendedClass();

    /**
     * Validates the given <code>objectToValidate</code> and returns the result as list of messages.
     * Is is allowed to return <code>null</code> to signal that no error (warning or info) was
     * found.
     * 
     * @param objectToValidate The object to validate.
     * @param ipsProject The ipsproject which is used to resolve referenced objects.
     * 
     * @return The result of the validation as list of messages.
     * 
     * @throws CoreException If an error occurs while validating the given object.
     */
    MessageList validate(T objectToValidate, IIpsProject ipsProject) throws CoreException;

}
