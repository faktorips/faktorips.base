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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AssociationNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * The product component collector is used to collect the product components that could be used as
 * qualifiers in context of an association qualification.
 * <p>
 * In formulas of product components there is the ability to navigate over policy associations. By
 * using a product component as qualifier it is possible to reduce the target result of an
 * association to policy components that are configured by the specified product component. To give
 * a proper content proposal we have made the following assumption to reduce the number of possible
 * product components: If there is a parameter of the policy component type that is configured by
 * the current product component type we assume that the parameter instance (the policy component)
 * is configured by the current product component. Using this assumption we could reduce the search
 * for product components to those that are actually in the product component structure.
 * 
 */
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
        List<IProductCmpt> result = new ArrayList<>(contextProductCmpts);
        Collections.sort(result, Comparator.comparing(IProductCmpt::getName));

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
            return new AssociationCollector((AssociationNode)node, this);
        } else if (node instanceof ParameterNode) {
            return new ParameterCollector((ParameterNode)node, this);
        } else if (node instanceof QualifierNode) {
            return new QualifierCollector((QualifierNode)node, this);
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
