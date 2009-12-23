package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;


public abstract class AbstractTextGenerator implements IGenerator {

    public final byte[] generate() {
        try {
            return generateText().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return generateText().getBytes();
        }
    }

    public abstract String generateText(); 

}
