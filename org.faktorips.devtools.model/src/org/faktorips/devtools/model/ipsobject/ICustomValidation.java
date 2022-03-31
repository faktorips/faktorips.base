/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;

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
     * @return The extended class or interface of the Faktor-IPS model, e.g. IAttribute or
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
     * @throws IpsException If an error occurs while validating the given object.
     */
    MessageList validate(T objectToValidate, IIpsProject ipsProject) throws IpsException;

}
