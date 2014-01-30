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

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.eclipse.jface.text.Region;
import org.faktorips.util.message.Message;

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

    InvalidIdentifierNode(Message errorMessage, Region region) {
        super(null, region);
        this.errorMessage = errorMessage;
    }

    public Message getMessage() {
        return errorMessage;
    }

}
