/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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
