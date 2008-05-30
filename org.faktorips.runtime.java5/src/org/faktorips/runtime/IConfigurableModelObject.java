/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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
     * Returns the product component this policy component is based on.
     */
    public IProductComponent getProductComponent();
    
    /**
     * Returns the date since when this model object is effective.
     */
    public Calendar getEffectiveFromAsCalendar();
    
    /**
     * Initializes the model object with the defaults from it's product component generation.
     */
    public void initialize();

}
