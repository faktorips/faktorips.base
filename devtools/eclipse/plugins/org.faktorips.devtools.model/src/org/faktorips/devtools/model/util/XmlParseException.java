/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class XmlParseException extends Exception {

    private static final long serialVersionUID = 5415428180315171850L;

    public XmlParseException() {
        super();
    }

    public XmlParseException(String message) {
        super(message);
    }

    public XmlParseException(Throwable cause) {
        super(cause);
    }

    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
