/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * A runtime exception that indicates that a product component hasn't been found.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -9147053421136751930L;

    private final String repositoryName;
    private final String productCmptId;

    /**
     * Creates a new exception that indicates that no product component with the given id has been
     * found.
     */
    public ProductCmptNotFoundException(String repositoryName, String productCmptId) {
        super();
        this.repositoryName = repositoryName;
        this.productCmptId = productCmptId;
    }

    /**
     * Returns the id of the product component that hasn't been found.
     */
    public String getProductCmptId() {
        return productCmptId;
    }

    /**
     * Returns the name of the repository that was searched for the product component.
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    @Override
    public String toString() {
        return "ProductComponent " + productCmptId + " not found in repository " + repositoryName;
    }

}
