/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.Set;

import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;

/**
 * Utility class for counting products and references in a product structure tree.
 */
public class ProductStructureCounter {
    private ProductStructureCounter() {
        // Prevent instantiation
    }

    /**
     * Counts the number of product components and relations in the given product structure.
     *
     * @param structure the product structure to count
     * @return a ProductStructureCounts record containing the component and relation counts
     */
    public static ProductStructureCounts countProductsAndReferences(IProductCmptTreeStructure structure) {
        if (structure == null) {
            return new ProductStructureCounts(0, 0);
        }

        int componentCount = 0;
        int relationCount = 0;

        Set<IProductCmptStructureReference> allReferences = structure.toSet(false);

        for (IProductCmptStructureReference reference : allReferences) {
            if (reference instanceof IProductCmptReference) {
                componentCount++;
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                IProductCmptStructureReference[] children = reference.getChildren();
                if (children.length > 0) {
                    relationCount++;
                }
            }
        }

        return new ProductStructureCounts(componentCount, relationCount);
    }

}
