/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;

/**
 * An implementation of this interface generates output
 * 
 * @author dicker
 * 
 */
public interface IGenerator {
    /**
     * returns generated data as byte[]
     * 
     */
    public byte[] generate() throws UnsupportedEncodingException;

    public final String CHARSET = "UTF-8"; //$NON-NLS-1$
}
