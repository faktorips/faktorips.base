/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.Calendar;

/**
 * Base interface for all model objects that are configurable by a product component.
 * 
 * @author Jan Ortmann
 */

public interface IConfigurableModelObject extends IModelObject {

    /**
     * The name of the property 'productComponent'.
     */
    public final static String PROPERTY_PRODUCT_COMPONENT = "productComponent";

    /**
     * The name of the property 'productCmptGeneration'.
     */
    public final static String PROPERTY_PRODUCT_CMPT_GENERATION = "productCmptGeneration";

    /**
     * Returns the product component this policy component is based on.
     */
    public IProductComponent getProductComponent();

    /**
     * Returns the product component generation this policy component is based on.
     */
    public IProductComponentGeneration getProductCmptGeneration();

    /**
     * Returns the date since when this model object is effective.
     */
    public Calendar getEffectiveFromAsCalendar();

    /**
     * Initializes the model object with the defaults from it's product component generation.
     */
    public void initialize();

}
