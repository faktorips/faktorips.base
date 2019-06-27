/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
