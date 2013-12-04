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

package org.faktorips.runtime.internal;

import java.util.Locale;

import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.values.IInternationalString;

public class PropertyReadingInternationalString implements IInternationalString {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 5041960764276425054L;

    private final MessagesHelper messageHelper;

    private final String key;

    public PropertyReadingInternationalString(String key, MessagesHelper messagesHelper) {
        this.key = key;
        messageHelper = messagesHelper;
    }

    public String get(Locale locale) {
        return messageHelper.getMessage(key, locale);
    }

}
