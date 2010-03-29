package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;



/**
 * AbstractTextGenerator generates Text without using an {@link ILayouter}
 * 
 * <br/>
 * Useful for structuring data like e.g. frame definitions
 * 
 * @author dicker
 *
 */
public abstract class AbstractTextGenerator implements IGenerator {

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.generators.IGenerator#generate()
     */
    public final byte[] generate() {
        try {
            return generateText().getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            return generateText().getBytes();
        }
    }

    
    /**
     * returns the generated String
     * @return 
     */
    public abstract String generateText(); 

}
