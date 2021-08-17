/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier.contextcollector;

import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * The {@link DefaultCollector} is instantiated for every node that has no explicit collector. While
 * there is a node it simply returns the context of the previous node. If there is no node it
 * returns <code>null</code>.
 */
class DefaultCollector extends AbstractProductCmptCollector {

    protected DefaultCollector(IdentifierNode node, ContextProductCmptFinder finder) {
        super(node, finder);
    }

    @Override
    protected Set<IProductCmpt> getContextProductCmpts() {
        if (getNode() == null) {
            return null;
        } else {
            return getPreviousContextProductCmpts();
        }
    }

}
