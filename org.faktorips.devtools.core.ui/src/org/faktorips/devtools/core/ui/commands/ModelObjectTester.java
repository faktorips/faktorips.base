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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * This property tester is able to test an Object if it is a model object or not.
 * 
 * @author dirmeier
 */
public class ModelObjectTester extends PropertyTester {

    public ModelObjectTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        return !((IIpsObject)receiver).getIpsObjectType().isProductDefinitionType();
    }

}
