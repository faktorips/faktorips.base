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
    @Override
    public void removeChildModelObjectInternal(IModelObject child) {
        // do nothing
    }

}
