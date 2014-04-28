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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNode;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

public class ParameterCollector extends AbstractProductCmptCollector {

    protected ParameterCollector(IdentifierNode node, ContextProductCmptFinder finder) {
        super(node, finder);
    }

    @Override
    protected Set<IProductCmpt> getContextProductCmpts() {
        try {
            Datatype datatype = getNode().getDatatype();
            if (datatype instanceof IPolicyCmptType) {
                IPolicyCmptType policyCmptType = (IPolicyCmptType)datatype;
                IProductCmptGeneration productCmptGeneration = getOriginGeneration();
                if (productCmptGeneration != null
                        && policyCmptType.equals(productCmptGeneration.findPolicyCmptType(getIpsProject()))) {
                    return new LinkedHashSet<IProductCmpt>(Arrays.asList(productCmptGeneration.getProductCmpt()));
                }
            }
            // Do not return an empty list, see java doc of getContextProductCmpts()
            return null;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
