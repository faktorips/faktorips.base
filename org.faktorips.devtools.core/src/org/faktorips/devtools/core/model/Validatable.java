/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.model;


import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

/**
 * Marks an object as being validatable.
 */
public interface Validatable {

    /**
     * Returns <code>true</code> if this object does not contain any errors, otherwise
     * <code>false</code>. If the method returns <code>false</code> the method
     * <code>validate()</code> returns at least one error message.
     * 
     * @see #validate(IIpsProject)
     * @throws CoreException if an exception occurs while validating the object.
     */
    public boolean isValid() throws CoreException;

    /**
     * Returns the resulting severity of the validation. The returned severity is equal to the
     * severity of the message list returned by the validate() method.
     * 
     * @see #validate(IIpsProject)
     * @throws CoreException
     */
    public int getValidationResultSeverity() throws CoreException;

    /**
     * Validates the object and all of it's parts.
     * 
     * @param ipsProject the context IIpsProject. The validation might be called from a different
     *            IIpsProject than the actual instance of this validatable belongs to. In this case
     *            it is necessary to use the IIpsProject of the caller for finder-methods that are
     *            used within the implementation of this method.
     * @return ValidationMessageList containing a list of messages describing errors, warnings and
     *         information. If no messages are created, an empty list is returned.
     * @throws CoreException if an exception occurs while validating the object.
     */
    public MessageList validate(IIpsProject ipsProject) throws CoreException;
}
