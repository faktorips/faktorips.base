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
import org.faktorips.devtools.core.model.DependencyDetail;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IExpression;

public class ExpressionDependencyDetail extends DependencyDetail {

    public ExpressionDependencyDetail(IIpsObjectPartContainer part) {
        super(part, IExpression.PROPERTY_EXPRESSION);
    }

    @Override
    public void refactorAfterRename(IIpsPackageFragment targetIpsPackageFragment, String newName) throws CoreException {
        // TODO Auto-generated method stub
    }

}
