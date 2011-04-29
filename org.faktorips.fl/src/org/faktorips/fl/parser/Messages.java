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

package org.faktorips.fl.parser;

import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Thorsten Guenther
 */
public class Messages extends LocalizedStringsSet {

    public final static Messages INSTANCE = new Messages();

    private Messages() {
        super("org.faktorips.fl.parser.messages", Messages.class.getClassLoader()); //$NON-NLS-1$
    }
}
