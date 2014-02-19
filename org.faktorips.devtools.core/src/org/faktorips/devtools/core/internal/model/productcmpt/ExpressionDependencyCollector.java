/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierParser;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.QualifierNode;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.util.TextRegion;
import org.faktorips.fl.parser.FlParser;
import org.faktorips.fl.parser.ParseException;
import org.faktorips.fl.parser.SimpleNode;

/**
 * This class collects the dependencies from a formula expression text to the therein specified
 * {@link IIpsObject IPS objects}. The expression text is parsed completely to extract all
 * identifiers that address a specific IPS object.
 * <p>
 * The dependencies are returned as a {@link IpsObjectDependency}. For each IPS object used in the
 * expression a dependency detail is added. These details are of type
 * {@link ExpressionDependencyDetail} and can be used by the refactoring framework.
 * 
 * @author dirmeier
 */
public class ExpressionDependencyCollector {

    private final Expression expression;

    private final Map<IDependency, ExpressionDependencyDetail> result = new HashMap<IDependency, ExpressionDependencyDetail>();

    private final IdentifierVisitor identifierVisitor;

    /**
     * Creates a new {@link ExpressionDependencyCollector} for the given {@link Expression} and uses
     * the specified {@link IdentifierVisitor} to find the identifiers within the expression text.
     * 
     * @param expression The expression for which we need to get the dependencies
     * @param identifierVisitor An {@link IdentifierVisitor} that is used to find the identifiers
     */
    protected ExpressionDependencyCollector(Expression expression, IdentifierVisitor identifierVisitor) {
        this.expression = expression;
        this.identifierVisitor = identifierVisitor;
    }

    /**
     * Creates a new {@link ExpressionDependencyCollector} for the given {@link Expression} using a
     * new {@link IdentifierVisitor} to search for identifiers within the formula expression.
     * 
     * @param expression The expression for which we need to get the dependencies
     */
    protected ExpressionDependencyCollector(Expression expression) {
        this(expression, newIdentifierVisitor(expression));
    }

    private static IdentifierVisitor newIdentifierVisitor(IExpression expression) {
        IdentifierParser identifierParser = new IdentifierParser(expression, expression.getIpsProject(), IpsPlugin
                .getDefault().getIdentifierFilter());
        IdentifierVisitor identifierVisitor = new IdentifierVisitor(expression.getExpression(), identifierParser);
        return identifierVisitor;
    }

    Map<IDependency, ExpressionDependencyDetail> getResult() {
        return result;
    }

    /**
     * Start collecting the dependencies: parse the expression text, parse the identifier nodes to
     * search for identifier parts that reference other {@link IIpsObject}.
     * 
     * @return A map of found dependencies pointing to a list of corresponding dependency details.
     */
    public Map<IDependency, ExpressionDependencyDetail> collectDependencies() {
        SimpleNode node = parseExpression();
        if (node != null) {
            return collectDependencies(node);
        } else {
            return new HashMap<IDependency, ExpressionDependencyDetail>();
        }
    }

    protected SimpleNode parseExpression() {
        try {
            StringReader reader = new StringReader(expression.getExpression());
            FlParser parser = new FlParser(reader);
            SimpleNode rootNode = parser.start();
            return rootNode;
        } catch (ParseException e) {
            // we ignore parsing exceptions because they have to be recognized by validation already
            return null;
        }
    }

    /**
     * Starts collecting the dependencies using the {@link SimpleNode} from the previously parsed
     * expression text. The method visits the nodes and parses the identifier nodes to search for
     * identifier parts that reference other {@link IIpsObject IPS objects}.
     * 
     * @param node The {@link SimpleNode} that is the entry point of a previously parsed expression
     *            text.
     * @return A map of found dependencies pointing to a list of corresponding dependency details.
     */
    public Map<IDependency, ExpressionDependencyDetail> collectDependencies(SimpleNode node) {
        node.jjtAccept(identifierVisitor, null);
        Map<IdentifierNode, Integer> identifiers = identifierVisitor.getIdentifiers();
        for (Entry<IdentifierNode, Integer> identifierEntry : identifiers.entrySet()) {
            collectDependencies(identifierEntry.getKey(), identifierEntry.getValue());
        }
        return getResult();
    }

    void collectDependencies(IdentifierNode identifierNode, int identifierOffset) {
        if (identifierNode instanceof QualifierNode) {
            IpsObjectDependency dependency = createQualifiedNodeDependency(identifierNode);
            TextRegion textRegion = getTextRegion(identifierNode, identifierOffset, 1, -2);
            getDependencyDetail(dependency).addTextRegion(textRegion);
        }
        if (identifierNode.hasSuccessor()) {
            collectDependencies(identifierNode.getSuccessor(), identifierOffset);
        }
    }

    private IpsObjectDependency createQualifiedNodeDependency(IdentifierNode identifierNode) {
        QualifierNode qualifierNode = (QualifierNode)identifierNode;
        IpsObjectDependency dependency = IpsObjectDependency.createReferenceDependency(expression.getIpsObject()
                .getQualifiedNameType(), qualifierNode.getProductCmpt().getQualifiedNameType());
        return dependency;
    }

    TextRegion getTextRegion(IdentifierNode identifierNode, int identifierOffset, int startOffset, int endOffset) {
        TextRegion identiferRegion = identifierNode.getTextRegion().offset(identifierOffset);
        return identiferRegion.startOffset(startOffset).endOffset(endOffset);
    }

    private ExpressionDependencyDetail getDependencyDetail(IpsObjectDependency dependency) {
        ExpressionDependencyDetail detail = getResult().get(dependency);
        if (detail == null) {
            detail = new ExpressionDependencyDetail(expression);
            getResult().put(dependency, detail);
        }
        return detail;
    }

}