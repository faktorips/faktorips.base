/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.faktorips.devtools.model.ipsobject.IIpsObject;

/**
 * This property tester is able to test an Object if it is a model object or not.
 * 
 * @author dirmeier
 * @deprecated Use {@link IpsObjectTypeTester#PROPERTY_IS_MODEL_TYPE} instead to avoid needless
 *                 instantiation of {@link IIpsObject}.
 */
@Deprecated(since = "21.6", forRemoval = true)
public class ModelObjectTester extends PropertyTester {

    public ModelObjectTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        return !((IIpsObject)receiver).getIpsObjectType().isProductDefinitionType();
    }

}
