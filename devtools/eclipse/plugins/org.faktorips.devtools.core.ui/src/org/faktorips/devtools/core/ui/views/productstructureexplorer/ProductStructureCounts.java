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

/**
 * Record that holds the count of product components and relation in a product structure.
 *
 * @param productCount the number of product components
 * @param relationCount the number of relations
 */
public record ProductStructureCounts(int productCount, int relationCount) {
    /**
     * creates a new ProductStructureCounts with the given counts
     *
     * @param productCount the number of product components
     * @param relationCount the number of relations
     */
    public ProductStructureCounts {
        // ProductStructureCounts constructor
    }
}
