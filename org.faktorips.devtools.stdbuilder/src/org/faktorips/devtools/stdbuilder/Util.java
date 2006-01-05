package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.model.pctype.Modifier;

/**
 * Utility class for this ips artefact builder plugin.
 * 
 * @author Peter Erzberger
 */
public class Util {

    /**
     * Returns the <code>java.lang.reflect.Modifier</code> constant for the faktorips plugin
     * modifier enum value.
     */
    public final static int getJavaModifier(Modifier modifier) {

        if (Modifier.PRIVATE.equals(modifier)) {
            return java.lang.reflect.Modifier.PRIVATE;
        }

        if (Modifier.PUBLIC.equals(modifier)) {
            return java.lang.reflect.Modifier.PUBLIC;
        }

        if (Modifier.PUBLISHED.equals(modifier)) {
            return java.lang.reflect.Modifier.PUBLIC;
        }

        throw new IllegalArgumentException("No match was found for the provided modifier: "
                + modifier);
    }
}
