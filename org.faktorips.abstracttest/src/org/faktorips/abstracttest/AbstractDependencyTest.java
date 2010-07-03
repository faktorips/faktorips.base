/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.abstracttest;

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
