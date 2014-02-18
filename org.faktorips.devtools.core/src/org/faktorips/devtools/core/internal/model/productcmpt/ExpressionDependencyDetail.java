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

    private final TextRegion textRegion;

    /**
     * Creates the new dependency detail for the given expression. The property provided to the
     * super class is always {@link IExpression#PROPERTY_EXPRESSION}.
     * 
     * @param expression The expression for which you build the dependency
     */
    public ExpressionDependencyDetail(IExpression expression, TextRegion textRegion) {
        super(expression, IExpression.PROPERTY_EXPRESSION);
        this.textRegion = textRegion;
    }

    @Override
    public IExpression getPart() {
        return (IExpression)super.getPart();
    }

    @Override
    public void refactorAfterRename(IIpsPackageFragment targetIpsPackageFragment, String newName) throws CoreException {
        String refactoredString = getTextRegion().createFullRefactoredString(getPart().getExpression(), newName);
        getPart().setExpression(refactoredString);
    }

    TextRegion getTextRegion() {
        return textRegion;
    }

}
