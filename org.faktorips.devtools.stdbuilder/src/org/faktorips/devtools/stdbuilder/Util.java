/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

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
