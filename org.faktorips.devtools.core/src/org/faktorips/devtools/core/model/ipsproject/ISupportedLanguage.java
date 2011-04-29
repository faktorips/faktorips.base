/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsproject;

import java.util.Locale;

import org.faktorips.devtools.core.model.XmlSupport;

/**
 * An IPS project that supports a natural language provides labels for model elements in that
 * language.
 * <p>
 * <strong>Example supporting English and German language:</strong><br />
 * Policy component type: MotorPolicy<br />
 * English label: Motor Policy<br />
 * German label: KFZ-Vertrag
 * <p>
 * A supported language can be marked as "default language" which means that it will be used
 * whenever a label in a specific language is not available. If then no label in the default
 * language is available, the model element's original name will be used.
 * 
 * @author Alexander Weickmann
 * 
 * @since 3.1
 */
public interface ISupportedLanguage extends XmlSupport {

    public static final String XML_TAG_NAME = "SupportedLanguage"; //$NON-NLS-1$

    /**
     * Returns the {@link Locale} of the language.
     */
    public Locale getLocale();

    /**
     * Returns the name of the language.
     * <p>
     * This is a shortcut for<br />
     * <tt>getLocale().getDisplayLanguage();</tt>
     */
    public String getLanguageName();

    /**
     * Returns whether this supported language is also the "default language".
     */
    public boolean isDefaultLanguage();

}
