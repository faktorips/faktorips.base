/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
    String get(Locale locale);

}
