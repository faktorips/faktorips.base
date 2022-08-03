/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsproject;

import java.util.Locale;

import org.faktorips.devtools.model.XmlSupport;

/**
 * An IPS project that supports a natural language provides labels for model elements in that
 * language.
 * <p>
 * <strong>Example supporting English and German language:</strong><br>
 * Policy component type: MotorPolicy<br>
 * English label: Motor Policy<br>
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

    String XML_TAG_NAME = "SupportedLanguage"; //$NON-NLS-1$

    /**
     * Returns the {@link Locale} of the language.
     */
    Locale getLocale();

    /**
     * Returns the name of the language.
     * <p>
     * This is a shortcut for<br>
     * <code>getLocale().getDisplayLanguage();</code>
     */
    String getLanguageName();

    /**
     * Returns whether this supported language is also the "default language".
     */
    boolean isDefaultLanguage();

}
