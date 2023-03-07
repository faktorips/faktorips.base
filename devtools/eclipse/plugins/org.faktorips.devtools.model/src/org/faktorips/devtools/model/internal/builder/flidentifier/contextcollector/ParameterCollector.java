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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.ParameterNode;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;

/**
 * The {@link ParameterCollector} checks the assumption whether the given parameter is a policy
 * component that may be configured by the current product component. If the assumption matches it
 * returns the product component of the formula. If not it returns null to indicate that the
 * assumption does not match.
 * 
 */
public class ParameterCollector extends AbstractProductCmptCollector {

    protected ParameterCollector(ParameterNode node, ContextProductCmptFinder finder) {
        super(node, finder);
    }

    @Override
    protected Set<IProductCmpt> getContextProductCmpts() {
        Datatype datatype = getNode().getDatatype();
        if (datatype instanceof IPolicyCmptType policyCmptType) {
            IProductCmptGeneration productCmptGeneration = getOriginGeneration();
            if (isMatchingPolicyCmptType(productCmptGeneration, policyCmptType)) {
                return new LinkedHashSet<>(Arrays.asList(productCmptGeneration.getProductCmpt()));
            }
        }
        // Do not return an empty list, see java doc of getContextProductCmpts()
        return null;
    }

    private boolean isMatchingPolicyCmptType(IProductCmptGeneration productCmptGeneration,
            IPolicyCmptType policyCmptType) {
        IPolicyCmptType matchingPolicyCmptType = getMatchingPolicyCmptType(productCmptGeneration);
        return matchingPolicyCmptType != null
                && matchingPolicyCmptType.isSubtypeOrSameType(policyCmptType, getIpsProject());
    }

    private IPolicyCmptType getMatchingPolicyCmptType(IProductCmptGeneration productCmptGeneration) {
        if (productCmptGeneration != null) {
            return productCmptGeneration.findPolicyCmptType(getIpsProject());
        }
        return null;
    }

}
