/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    public TestPolicyComponent() {
        super();
    }

    public TestPolicyComponent(IProductComponent productCmpt) {
        super(productCmpt);
    }

    public Calendar getEffectiveFromAsCalendar() {
        return null;
    }

    @Override
    public void removeChildModelObjectInternal(IModelObject child) {
        // do nothing
    }

}
