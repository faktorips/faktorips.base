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

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.refactor.TextRegion;
import org.faktorips.devtools.core.model.DependencyDetail;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IExpression;

/**
 * This implementation of dependency detail supports the refactoring of dependencies that exists
 * within a formula expression. For example a formula expression may reference a product component
 * by its name. When the product component is renamed, we need to refactor the expression and
 * replace the old name by the new one. However we cannot use the default {@link DependencyDetail}
 * because it only supports the update of a whole property not only a text region within a property.
 * 
 */
public class ExpressionDependencyDetail extends DependencyDetail {

    private final SortedSet<TextRegion> textRegions = new TreeSet<TextRegion>();

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

    public void addTextRegion(TextRegion textRegion) {
        textRegions.add(textRegion);
    }

    @Override
    public void refactorAfterRename(IIpsPackageFragment targetIpsPackageFragment, String newName) throws CoreException {
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
