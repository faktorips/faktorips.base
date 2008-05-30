/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Calendar;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;

/**
 * 
 * @author Jan Ortmann
 */
public class TestPolicyComponent extends AbstractConfigurableModelObject {

    /**
     * 
     */
    public TestPolicyComponent() {
        super();
    }

    /**
     * @param productCmpt
     */
    public TestPolicyComponent(IProductComponent productCmpt) {
        super(productCmpt);
    }

    /**
     * {@inheritDoc}
     */
    public Calendar getEffectiveFromAsCalendar() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void removeChildModelObjectInternal(IModelObject child) {
    }

}
