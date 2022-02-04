/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal.test;

import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.IpsElement;

public class TestSampleIpsElement extends IpsElement implements ITestSampleIpsElement {

    @Override
    public AResource getCorrespondingResource() {
        // not needed for these tests
        return null;
    }

    @Override
    public void delete() throws CoreRuntimeException {
        // nothing to do
    }

}
