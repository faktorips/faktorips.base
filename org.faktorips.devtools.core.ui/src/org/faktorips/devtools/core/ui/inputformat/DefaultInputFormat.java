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

package org.faktorips.devtools.core.ui.inputformat;

import java.util.Locale;

import org.eclipse.swt.events.VerifyEvent;

/**
 * Fallback inputformat. Simply passes through the given string in {@link #parse(String)} and
 * {@link #format(String)}.
 */
public class DefaultInputFormat extends AbstractInputFormat<String> {

    @Override
    protected String parseInternal(String stringToBeparsed) {
        return stringToBeparsed;
    }

    @Override
    protected String formatInternal(String value) {
        return value;
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // Nothing to do
    }

    @Override
    protected void initFormat(Locale locale) {
        // Nothing to initialize
    }

}
