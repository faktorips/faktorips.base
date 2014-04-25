/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.flidentifier.ast;

import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.util.TextRegion;
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

    InvalidIdentifierNode(Message errorMessage, TextRegion textRegion) {
        super(null, textRegion);
        this.errorMessage = errorMessage;
    }

    public Message getMessage() {
        return errorMessage;
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription(MultiLanguageSupport multiLanguageSupport) {
        // TODO Auto-generated method stub
        return null;
    }

}
