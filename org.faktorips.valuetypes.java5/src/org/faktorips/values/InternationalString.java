/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.values;

import java.io.Serializable;
import java.util.Locale;

/**
 * An {@link InternationalString} could be used for string properties that could be translated in
 * different languages.
 */
public interface InternationalString extends Serializable {

    /**
     * Getting the value string for the specified locale. Returns <code>null</code> if no string
     * could be found for the specified locale.
     * 
     * @param locale the locale of the text you want to get
     * @return return the text for the specified locale or <code>null</code> if no such text exists
     */
    public String get(Locale locale);

}