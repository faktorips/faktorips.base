/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * A runtime exception that indicates that a product component hasn't been found.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -9147053421136751930L;

    private String repositoryName;
    private String productCmptId;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ProductComponent " + productCmptId + " not found in repository " + repositoryName;
    }
}
