/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.SortedSet;
import java.util.TreeSet;

import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * This implementation of dependency detail supports the refactoring of dependencies that exist
 * within a formula expression. For example, a qualifier in a formula expression may reference a
 * product component by its name. When the product component is renamed, the expression must also be
 * refactored by replacing the old product component name by the new one.
 * <p>
 * To use this {@link ExpressionDependencyDetail} in a refactoring context, at least one
 * {@link TextRegion} must be added by calling {@link #addTextRegion(TextRegion)}. Each
 * {@link TextRegion} defines a region in the expression text that will be replaced by another
 * string (i.e. new product component name). If a single product component name is used multiple
 * times in an expression, multiple text regions must be added to the <em>same</em>
 * {@link ExpressionDependencyDetail}. Using multiple {@link ExpressionDependencyDetail details} in
 * that case will cause errors.
 * <p>
 * A separate implementation of {@link DependencyDetail} is required to enable the selective
 * replacement of text regions within a property string. The standard implementation only supports
 * replacing the entire property i.e. the complete expression.
 */
public class ExpressionDependencyDetail extends DependencyDetail implements IExpressionDependencyDetail {

    private final SortedSet<TextRegion> textRegions = new TreeSet<>();

    /**
     * Creates a new dependency detail for the given expression. The property provided to the super
     * class is always {@link IExpression#PROPERTY_EXPRESSION}.
     * 
     * @param expression The expression for which you build the dependency
     */
    public ExpressionDependencyDetail(IExpression expression) {
        super(expression, IExpression.PROPERTY_EXPRESSION);
    }

    @Override
    public IExpression getPart() {
        return (IExpression)super.getPart();
    }

    @Override
    public void addTextRegion(TextRegion textRegion) {
        textRegions.add(textRegion);
    }

    @Override
    public void refactorAfterRename(IIpsPackageFragment targetIpsPackageFragment, String newName) {
        String expressionText = getPart().getExpression();
        int offset = 0;
        for (TextRegion textRegion : getTextRegions()) {
            int expressionLength = expressionText.length();
            expressionText = textRegion.offset(offset).replaceTextRegion(expressionText, newName);
            offset += expressionText.length() - expressionLength;
        }
        getPart().setExpression(expressionText);
    }

    SortedSet<TextRegion> getTextRegions() {
        return textRegions;
    }

}
