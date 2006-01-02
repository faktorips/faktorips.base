package org.faktorips.devtools.core.ui.controls;

import org.faktorips.util.message.MessageList;

/**
 * Provides a method for validating an table element.
 * @author Andy Roesch
 */
public interface TableElementValidator {

 /**
 * Validates the given element 
 * @author Andy Roesch
 */
    public MessageList validate (String element); 
}
