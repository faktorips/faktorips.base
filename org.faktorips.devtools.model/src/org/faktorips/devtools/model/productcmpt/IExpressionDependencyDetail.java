/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.util.TextRegion;

/**
 * This implementation of dependency detail supports the refactoring of dependencies that exist
 * within a formula expression. For example, a qualifier in a formula expression may reference a
 * product component by its name. When the product component is renamed, the expression must also be
 * refactored by replacing the old product component name by the new one.
 * <p>
 * To use this {@link IExpressionDependencyDetail} in a refactoring context, at least one
 * {@link TextRegion} must be added by calling {@link #addTextRegion(TextRegion)}. Each
 * {@link TextRegion} defines a region in the expression text that will be replaced by another
 * string (i.e. new product component name). If a single product component name is used multiple
 * times in an expression, multiple text regions must be added to the <em>same</em>
 * {@link IExpressionDependencyDetail}. Using multiple {@link IExpressionDependencyDetail details}
 * in that case will cause errors.
 * <p>
 * A separate implementation of {@link IDependencyDetail} is required to enable the selective
 * replacement of text regions within a property string. The standard implementation only supports
 * replacing the entire property i.e. the complete expression.
 */
public interface IExpressionDependencyDetail extends IDependencyDetail {

    /**
     * Add a text region which should be refactored when the dependency target is renamed. The text
     * region points to the position within the expression text that represents the dependency.
     * Every added text region is replaces by the new name when
     * {@link #refactorAfterRename(IIpsPackageFragment, String)} is called.
     * 
     * @param textRegion A text region that points to a position within the expression text
     */
    public void addTextRegion(TextRegion textRegion);

}
