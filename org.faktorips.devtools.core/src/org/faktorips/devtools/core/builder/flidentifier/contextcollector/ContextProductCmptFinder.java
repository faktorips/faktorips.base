/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.builder.flidentifier.contextcollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

public class ContextProductCmptFinder {

    private final LinkedList<IdentifierNode> nodes;
    private final IExpression expression;
    private final IIpsProject ipsProject;

    public ContextProductCmptFinder(LinkedList<IdentifierNode> nodes, IExpression expression, IIpsProject ipsProject) {
        this.nodes = nodes;
        this.expression = expression;
        this.ipsProject = ipsProject;
    }

    public List<IProductCmpt> getContextProductCmpts() {
        Set<IProductCmpt> contextProductCmpts = createCollector().getContextProductCmpts();
        if (contextProductCmpts == null) {
            return Collections.emptyList();
        }
        List<IProductCmpt> result = new ArrayList<IProductCmpt>(contextProductCmpts);
        Collections.sort(result, new Comparator<IProductCmpt>() {

            @Override
            public int compare(IProductCmpt o1, IProductCmpt o2) {
                return o1.getName().compareTo(o2.getName());
            }

        });

        return result;
    }

    protected AbstractProductCmptCollector createCollector() {
        IdentifierNode node;
        if (nodes.isEmpty()) {
            node = null;
        } else {
            node = nodes.pop();
        }
        if (node instanceof AssociationNode) {
            return new AssociationCollector(node, this);
        } else if (node instanceof ParameterNode) {
            return new ParameterCollector(node, this);
        } else if (node instanceof QualifierNode) {
            return new QualifierCollector(node, this);
        } else {
            return new DefaultCollector(node, this);
        }
    }

    protected IIpsProject getIpsProject() {
        return ipsProject;
    }

    protected IExpression getExpression() {
        return expression;
    }

}
