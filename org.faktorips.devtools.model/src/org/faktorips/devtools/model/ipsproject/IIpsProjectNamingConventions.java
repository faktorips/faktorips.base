/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Naming conventions for the various IPS elements. This is a separate class as it is sometimes
 * necessary to check if a name is valid before an object is created for example in wizards to a
 * create a new object. Therefore we can't use a method of the specified class. The naming
 * conventions are defined per project, so that they can be configured in the project.
 * 
 * @author Jan Ortmann
 */
public interface IIpsProjectNamingConventions {

    /**
     * Message code for messages indicating that a name is discouraged.
     */
    public static final String DISCOURAGED_NAME = "DISCOURAGED_NAME"; //$NON-NLS-1$

    /**
     * Message code for messages indicating that a name is invalid.
     */
    public static final String INVALID_NAME = "INVALID_NAME"; //$NON-NLS-1$

    /**
     * Message code for messages indicating that a name is missing.
     */
    public static final String NAME_IS_MISSING = "NAME_IS_MISSING"; //$NON-NLS-1$

    /**
     * Message code for messages indicating that a given name should be unqualified but was
     * qualified.
     */
    public static final String NAME_IS_QUALIFIED = "NAME_IS_QUALIFIED"; //$NON-NLS-1$

    /**
     * Returns a message list containing errors if the given qualified name is missing or invalid
     * for the given IPS object type. The message list might also contain warnings if the name is
     * discouraged.
     * 
     * @param type The type of IPS object.
     * @param name The name to validate.
     * 
     * @see #DISCOURAGED_NAME
     * @see #INVALID_NAME
     * @see #NAME_IS_MISSING
     * 
     * @throws NullPointerException if type is <code>null</code>
     * @throws IpsException if an error occurs while validating the name.
     */
    public MessageList validateQualifiedIpsObjectName(IpsObjectType type, String name) throws IpsException;

    /**
     * Returns a message list containing errors if the given unqualified name is missing or invalid
     * for the given IPS object type. The message list might also contain warnings if the name is
     * discouraged.
     * 
     * @param type The type of IPS object.
     * @param name The name to validate.
     * 
     * @see #DISCOURAGED_NAME
     * @see #INVALID_NAME
     * @see #NAME_IS_MISSING
     * @see #NAME_IS_QUALIFIED
     * 
     * @throws NullPointerException if type is <code>null</code>
     * @throws IpsException if an error occurs while validating the name.
     */
    public MessageList validateUnqualifiedIpsObjectName(IpsObjectType type, String name) throws IpsException;

    /**
     * Validates if the given name is valid for IPS packages.
     * 
     * @throws IpsException if an error occurs while validating the name.
     */
    public MessageList validateIpsPackageName(String name) throws IpsException;

    /**
     * Validates if the given name is valid for IPS package roots.
     * 
     * @throws IpsException if an error occurs while validating the name.
     */
    public MessageList validateIpsPackageRootName(String name) throws IpsException;

    /**
     * Returns a Message object if the provided name doesn't comply to the java naming conventions.
     * The code of the message object is <code>INVALID_NAME</code>
     * 
     * @param name the name that is validated
     * @param text the message text
     * @param validatedObject the object that is provided to the created return message.
     * @param ipsProject is needed to retrieve the project specific compiler settings that are used
     *            for validation
     * 
     * @see #INVALID_NAME
     * 
     * @return <code>null</code> if the name is valid and a standard message if not
     * 
     * @throws IpsException if an error occurs while validating the name.
     */
    public Message validateIfValidJavaIdentifier(String name,
            String text,
            Object validatedObject,
            IIpsProject ipsProject) throws IpsException;

    /**
     * Validates if the given name is valid java type name.
     * 
     * @param qualifiedCheck true = qualified name; false = unqualified name
     */
    public MessageList validateJavaTypeName(String name, boolean qualifiedCheck);

}
