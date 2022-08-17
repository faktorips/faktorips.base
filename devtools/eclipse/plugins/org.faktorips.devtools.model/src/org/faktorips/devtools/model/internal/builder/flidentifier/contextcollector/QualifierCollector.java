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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * This collector returns the product component that is qualified by a {@link QualifierNode}.
 * 
 */
public class QualifierCollector extends AbstractProductCmptCollector {

    protected QualifierCollector(QualifierNode node, ContextProductCmptFinder finder) {
        super(node, finder);
    }

    @Override
    protected Set<IProductCmpt> getContextProductCmpts() {
        return new LinkedHashSet<>(Arrays.asList(((QualifierNode)getNode()).getProductCmpt()));
    }

}
