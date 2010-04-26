/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

/**
 * Base interface for all model objects.
 * 
 * @author Jan Ortmann
 */
public interface IModelObject {

    /**
     * Validates the model object and returns a list of messages. If no message is generated the
     * method returns an empty list. Note that also messages like warnings or informations can be
     * returned for valid objects.
     * 
     * @param context provides additional external information that might be necessary to execute
     *            the validation. E.g. the business context, the locale to provide locale specific
     *            message texts, user information. The parameter must not be <code>null</code>.
     * 
     * @throws NullPointerException if context is <code>null</code>.
     */
    public MessageList validate(IValidationContext context);

}
