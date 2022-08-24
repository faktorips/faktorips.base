/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.ast;

import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.runtime.Message;

/**
 * This node is a special kind of {@link IdentifierNode}. It do not relay represent any identifier
 * part but is created in case of exceptions or errors while parsing the identifier. The
 * {@link InvalidIdentifierNode} consists of a helpful human readable and translated error message
 * that helps the user to correct the invalid identifier part.
 * 
 * @author dirmeier
 */
public class InvalidIdentifierNode extends IdentifierNode {

    private final Message errorMessage;

    InvalidIdentifierNode(Message errorMessage, TextRegion textRegion) {
        super(null, textRegion);
        this.errorMessage = errorMessage;
    }

    public Message getMessage() {
        return errorMessage;
    }

}
