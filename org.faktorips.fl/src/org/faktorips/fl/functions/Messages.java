package org.faktorips.fl.functions;

import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class Messages extends LocalizedStringsSet {

    public final static Messages INSTANCE = new Messages();
    
    private Messages() {
        super("org.faktorips.fl.functions.Messages");
    }

}
