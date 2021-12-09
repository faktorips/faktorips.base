/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

/**
 * Marks an object as being validatable.
 * 
 * @author Jan Ortmann
 */
public interface Validatable {

    /**
     * Returns <code>true</code> if this object does not contain any errors, otherwise
     * <code>false</code>. If the method returns <code>false</code> the method
     * <code>validate()</code> returns at least one error message.
     * 
     * @param ipsProject The context IPS project. The validation might be called from a different
     *            IPS project than the actual instance of this validatable belongs to. In this case
     *            it is necessary to use the IPS project of the caller for finder-methods that are
     *            used within the implementation of this method.
     * @return Flag indicating whether this object is valid.
     * 
     * @throws CoreRuntimeException If an exception occurs while validating the object.
     * 
     * @see #validate(IIpsProject)
     * 
     * @since 3.0.0
     */
    public boolean isValid(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the resulting severity of the validation. The returned severity is equal to the
     * severity of the message list returned by the validate(IIpsProject) method.
     * 
     * @param ipsProject The context IPS project. The validation might be called from a different
     *            IPS project than the actual instance of this validatable belongs to. In this case
     *            it is necessary to use the IPS project of the caller for finder-methods that are
     *            used within the implementation of this method.
     * 
     * @return the resulting validation severity.
     * 
     * @throws CoreRuntimeException If an exception occurs while obtaining the resulting validation
     *             severity.
     * 
     * @see #validate(IIpsProject)
     * 
     * @since 3.0.0
     */
    public Severity getValidationResultSeverity(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * <p>
     * Validates the object and all of it's parts.
     * <p>
     * Note that validations will be cached. The validation cache can be cleared trough the
     * IpsModel.
     * 
     * @param ipsProject The context IPS project. The validation might be called from a different
     *            IPS project than the actual instance of this validatable belongs to. In this case
     *            it is necessary to use the IPS project of the caller for finder-methods that are
     *            used within the implementation of this method.
     * 
     * @return A ValidationMessageList containing a list of messages describing errors, warnings and
     *         information. If no messages are created, an empty list is returned.
     * 
     * @throws CoreRuntimeException If an exception occurs while validating the object.
     * 
     * @see org.faktorips.devtools.model.IIpsModel#clearValidationCache()
     */
    public MessageList validate(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the project this validatable object belongs to. This project is used to validate the
     * object if no project is specified for validation.
     * 
     * @return The project this object belongs to.
     */
    public IIpsProject getIpsProject();

}
