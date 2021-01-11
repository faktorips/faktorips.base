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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.DependencyDetail;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * Abstract super class for dependency tests
 * 
 */
public abstract class AbstractDependencyTest extends AbstractIpsPluginTest {

    protected void assertSingleDependencyDetail(IIpsObject object,
            IDependency dependency,
            IIpsObjectPartContainer part,
            String propertyName) throws CoreException {

        List<IDependencyDetail> details = object.getDependencyDetails(dependency);
        DependencyDetail detail = new DependencyDetail(part, propertyName);
        assertEquals(1, details.size());
        assertTrue(details.contains(detail));
    }

    protected void assertDependencyDetailContained(IIpsObject object,
            IDependency dependency,
            IIpsObjectPartContainer part,
            String propertyName) throws CoreException {

        List<IDependencyDetail> details = object.getDependencyDetails(dependency);
        DependencyDetail detail = new DependencyDetail(part, propertyName);
        assertTrue(details.contains(detail));
    }
}
