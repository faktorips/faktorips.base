/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Locale;

import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.values.InternationalString;

/**
 * An {@link PropertiesReadingInternationalString} could be used for string properties that could be
 * translated in different languages. The {@link PropertiesReadingInternationalString} makes thereby
 * use of a {@link MessagesHelper}.
 */
public class PropertiesReadingInternationalString implements InternationalString {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 5041960764276425054L;

    private final MessagesHelper messageHelper;

    private final String key;

    public PropertiesReadingInternationalString(String key, MessagesHelper messagesHelper) {
        this.key = key;
        messageHelper = messagesHelper;
    }

    public String get(Locale locale) {
        return messageHelper.getMessage(key, locale);
    }

}
