/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * Abstract super class for dependency tests
 * 
 */
public abstract class AbstractDependencyTest extends AbstractIpsPluginTest {

    protected void assertSingleDependencyDetail(IIpsObject object,
            IDependency dependency,
            IIpsObjectPartContainer part,
            String propertyName) throws CoreRuntimeException {

        List<IDependencyDetail> details = object.getDependencyDetails(dependency);
        DependencyDetail detail = new DependencyDetail(part, propertyName);
        assertEquals(1, details.size());
        assertTrue(details.contains(detail));
    }

    protected void assertDependencyDetailContained(IIpsObject object,
            IDependency dependency,
            IIpsObjectPartContainer part,
            String propertyName) throws CoreRuntimeException {

        List<IDependencyDetail> details = object.getDependencyDetails(dependency);
        DependencyDetail detail = new DependencyDetail(part, propertyName);
        assertTrue(details.contains(detail));
    }
}
