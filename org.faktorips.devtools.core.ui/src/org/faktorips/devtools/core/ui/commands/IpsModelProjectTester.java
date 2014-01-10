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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This property tester is able to check if an {@link IIpsProject} is a modeling project.
 * 
 * @author noschinski2
 */
public class IpsModelProjectTester extends PropertyTester {

    public static final String PROPERTY_MODEL_IPS_PROJECT = "isModelIpsProject"; //$NON-NLS-1$

    public IpsModelProjectTester() {
        super();
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        if (!(receiver instanceof IIpsProject)) {
            return false;
        }

        if (property.equals(PROPERTY_MODEL_IPS_PROJECT)) {
            IIpsProject project = (IIpsProject)receiver;
            return project.isModelProject();
        }
        return false;
    }

}
