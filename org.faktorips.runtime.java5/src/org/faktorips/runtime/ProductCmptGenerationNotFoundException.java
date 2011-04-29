/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -5796918674803530938L;

    private String repositoryName;
    private String productCmptId;
    private Calendar effectiveDate;
    private boolean productCmptWasFound;

    /**
     * Creates a new exception that indicates that no product component generation with the given id
     * and effective date has been found.
     */
    public ProductCmptGenerationNotFoundException(String repositoryName, String productCmptId, Calendar effectiveDate,
            boolean productCmptWasFound) {
        super();
        this.repositoryName = repositoryName;
        this.productCmptId = productCmptId;
        this.effectiveDate = effectiveDate;
        this.productCmptWasFound = productCmptWasFound;
    }

    /**
     * Returns the name of the repository that was searched for the product component generation.
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Returns the product component id of the product component generation that hasn't been found.
     */
    public String getProductCmptId() {
        return productCmptId;
    }

    /**
     * Returns the effective date for that a generation was requested.
     */
    public Calendar getEffetiveDate() {
        return effectiveDate;
    }

    /**
     * Returns <code>true</code> if the product component was found in the repository but the
     * product component hasn't got a generation valid at the given effective date. Returns
     * <code>false</code> if the product component hasn't been found.
     */
    public boolean productCmptWasFound() {
        return productCmptWasFound;
    }

    @Override
    public String toString() {
        String date = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(effectiveDate.getTime());
        String foundCmpt = productCmptWasFound ? "was found" : "was not found";
        return "ProductComponentGeneration " + productCmptId + " effective on " + date
                + " not found in repository, product component  " + foundCmpt + " in repository " + repositoryName;
    }
}
