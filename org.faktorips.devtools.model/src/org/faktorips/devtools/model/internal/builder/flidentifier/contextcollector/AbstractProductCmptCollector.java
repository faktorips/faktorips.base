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

import java.util.GregorianCalendar;
import java.util.Set;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;

/**
 * The product component collectors are called by the {@link ContextProductCmptFinder}. Every
 * implementation is responsible for one kind of {@link IdentifierNode}.
 * 
 */
public abstract class AbstractProductCmptCollector {

    private final ContextProductCmptFinder finder;
    private final IdentifierNode node;

    protected AbstractProductCmptCollector(IdentifierNode node, ContextProductCmptFinder finder) {
        this.node = node;
        this.finder = finder;
    }

    public IIpsProject getIpsProject() {
        return finder.getIpsProject();
    }

    public IdentifierNode getNode() {
        return node;
    }

    protected IProductCmptGeneration getOriginGeneration() {
        if (getExpression() instanceof IFormula) {
            IFormula formula = (IFormula)getExpression();
            if (formula.getPropertyValueContainer() instanceof IProductCmptGeneration) {
                return (IProductCmptGeneration)formula.getPropertyValueContainer();
            }
        }
        return null;
    }

    private IExpression getExpression() {
        return finder.getExpression();
    }

    protected GregorianCalendar getValidFrom() {
        IProductCmptGeneration originGeneration = getOriginGeneration();
        if (originGeneration != null) {
            return originGeneration.getValidFrom();
        } else {
            return null;
        }
    }

    /**
     * Returns the set of product components that could be the context of the current node according
     * to the description in {@link ContextProductCmptFinder}.
     * <p>
     * Important: This method may return an empty list or <code>null</code> with different meanings:
     * An empty list means there is no context product component while <code>null</code> means that
     * no product component could be determined because of failing assumptions.
     * 
     * @return The list of product components that may be the context component at the current
     *             position. Returns <code>null</code> if no context could be determined.
     */
    protected abstract Set<IProductCmpt> getContextProductCmpts();

    protected Set<IProductCmpt> getPreviousContextProductCmpts() {
        AbstractProductCmptCollector collector = finder.createCollector();
        return collector.getContextProductCmpts();
    }

}
