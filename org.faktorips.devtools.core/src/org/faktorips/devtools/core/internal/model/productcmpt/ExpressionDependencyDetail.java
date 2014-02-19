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
 * <p>
 * To use this {@link ExpressionDependencyDetail} in refactoring context you have to add at least
 * one {@link TextRegion} by calling {@link #addTextRegion(TextRegion)}. If you have multiple text
 * regions that points to the same dependency, it is necessary to have only one
 * {@link ExpressionDependencyDetail} containing all the text regions. Otherwise the refactoring
 * will break because the first refactoring may break the position of the second one.
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

    /**
     * Add a text region which should be refactored when the dependency target is renamed. The text
     * region points to the position within the expression text that represents the dependency.
     * Every added text region is replaces by the new name when
     * {@link #refactorAfterRename(IIpsPackageFragment, String)} is called.
     * 
     * @param textRegion A text region that points to a position within the expression text
     */
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
