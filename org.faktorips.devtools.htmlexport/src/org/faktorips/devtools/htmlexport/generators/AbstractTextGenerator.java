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

package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * AbstractTextGenerator generates Text without using an {@link ILayouter}
 * 
 * <br/>
 * Useful for structuring data like e.g. frame definitions
 * 
 * @author dicker
 * 
 */
public abstract class AbstractTextGenerator implements IGenerator {

    @Override
    public final byte[] generate() {
        if (!Charset.isSupported(CHARSET)) {
            return generateText().getBytes();
        }
        try {
            return generateText().getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            // should never be reached
            return generateText().getBytes();
        }
    }

    /**
     * returns the generated String
     */
    public abstract String generateText();

}
