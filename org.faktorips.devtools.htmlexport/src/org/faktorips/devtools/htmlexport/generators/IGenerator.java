/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;

/**
 * An implementation of this interface generates output
 * 
 * @author dicker
 * 
 */
public interface IGenerator {
    /**
     * returns generated data as byte[]
     * 
     */
    public byte[] generate() throws UnsupportedEncodingException;

    public final String CHARSET = "UTF-8"; //$NON-NLS-1$
}
