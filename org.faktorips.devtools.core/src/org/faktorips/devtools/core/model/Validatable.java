package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.util.message.MessageList;


/**
 * Marks an object as being validatable.
 */
public interface Validatable {

    /**
     * Validates the object and all of it's parts. 
     * 
     * @return ValidationMessageList containing a list of messages 
     * describing errors, warnings and information. If no
     * messages are created, an empty list is returned.
     *  
     * @throws CoreException if an exception occurs while validating the object.
     */
    public MessageList validate() throws CoreException;
}
