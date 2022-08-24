/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;

/**
 * Abstract class to be implemented by individual validation tasks.
 * <p>
 * This class is intended to be used together with {@link IpsValidation}.
 * <p>
 * <strong>Important:</strong> This class is experimental and it has not yet been agreed about
 * whether this shall be the way to implement validations in Faktor-IPS. Therefore this class should
 * not be used at the moment, see FIPS-483.
 * 
 * @since 3.4
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsValidationTask {

    /**
     * Executes this validation task and returns the outcome in the form of a {@link Message}.
     * <p>
     * Returns null if the validation was successful and there were no infos or warnings.
     * 
     * @param ipsProject The context IPS project that shall be used for finder-methods that are used
     *            within the implementation
     * 
     * @throws IpsException If any error occurs during execution
     */
    public abstract Message execute(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns whether the overall validation this task is a part of shall continue even if this
     * task returns a message of severity {@link Message#ERROR}.
     * <p>
     * The default implementation returns false.
     */
    public boolean isContinueOnError() {
        return false;
    }

}
