/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.dependency;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.internal.type.Association;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DependencyDetailTest {

    private static final String MY_NEW_NAME = "MyNewName";

    private static final String MY_NEW_TARGET = "myNewTarget";

    @Mock
    private Association part;

    private String propertyName = IAssociation.PROPERTY_TARGET;

    @Mock
    private IIpsPackageFragment targetIpsPackageFragment;

    private DependencyDetail dependencyDetail;

    @Before
    public void setUpDependencyDetail() {
        dependencyDetail = new DependencyDetail(part, propertyName);
    }

    @Test
    public void testRefactorAfterRename() throws Exception {
        when(targetIpsPackageFragment.getName()).thenReturn(MY_NEW_TARGET);

        dependencyDetail.refactorAfterRename(targetIpsPackageFragment, MY_NEW_NAME);

        verify(part).setTarget(MY_NEW_TARGET + "." + MY_NEW_NAME);
    }

    @Test(expected = IpsException.class)
    public void testRefactorAfterRename_exception() throws Exception {
        DependencyDetail myDependencyDetail = new DependencyDetail(part, "any");

        myDependencyDetail.refactorAfterRename(targetIpsPackageFragment, MY_NEW_NAME);
    }

}
