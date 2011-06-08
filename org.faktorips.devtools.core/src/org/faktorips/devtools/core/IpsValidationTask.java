/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;

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
     * @throws CoreException If any error occurs during execution
     */
    public abstract Message execute(IIpsProject ipsProject) throws CoreException;

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
