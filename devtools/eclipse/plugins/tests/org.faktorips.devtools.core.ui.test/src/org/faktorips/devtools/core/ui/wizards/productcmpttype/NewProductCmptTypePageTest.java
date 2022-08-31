/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpttype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class NewProductCmptTypePageTest extends AbstractIpsPluginTest {

    @Mock
    private IStructuredSelection selection;
    private IIpsProject ipsProject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    @Test
    public void testFinishIpsObjectsExtension_changingOverTime_noSupertype() throws Exception {
        Set<IIpsObject> modifiedIpsObjects = Collections.emptySet();
        IProductCmptType newProductCmptType = newProductCmptType(ipsProject, "my.NewProductCmptType");
        // would come from the default as set in the project properties
        newProductCmptType.setChangingOverTime(true);
        NewProductCmptTypePage newProductCmptTypePage = new TestNewProductCmptTypePage("");

        newProductCmptTypePage.finishIpsObjectsExtension(newProductCmptType, modifiedIpsObjects);

        assertTrue(modifiedIpsObjects.isEmpty());
        assertTrue(newProductCmptType.isChangingOverTime());
    }

    @Test
    public void testFinishIpsObjectsExtension_notChangingOverTime_noSupertype() throws Exception {
        Set<IIpsObject> modifiedIpsObjects = Collections.emptySet();
        IProductCmptType newProductCmptType = newProductCmptType(ipsProject, "my.NewProductCmptType");
        // would come from the default as set in the project properties
        newProductCmptType.setChangingOverTime(false);
        NewProductCmptTypePage newProductCmptTypePage = new TestNewProductCmptTypePage("");

        newProductCmptTypePage.finishIpsObjectsExtension(newProductCmptType, modifiedIpsObjects);

        assertTrue(modifiedIpsObjects.isEmpty());
        assertFalse(newProductCmptType.isChangingOverTime());
    }

    @Test
    public void testFinishIpsObjectsExtension_notChangingOverTime_supertypeChangingOverTime() throws Exception {
        Set<IIpsObject> modifiedIpsObjects = Collections.emptySet();
        IProductCmptType newProductCmptType = newProductCmptType(ipsProject, "my.NewProductCmptType");
        // would come from the default as set in the project properties
        newProductCmptType.setChangingOverTime(false);

        ProductCmptType superProductCmptType = newProductCmptType(ipsProject, "test.SuperType");
        superProductCmptType.setChangingOverTime(true);

        NewProductCmptTypePage newProductCmptTypePage = new TestNewProductCmptTypePage("test.SuperType");

        newProductCmptTypePage.finishIpsObjectsExtension(newProductCmptType, modifiedIpsObjects);

        assertTrue(modifiedIpsObjects.isEmpty());
        assertTrue(newProductCmptType.isChangingOverTime());
    }

    @Test
    public void testFinishIpsObjectsExtension_changingOverTime_supertypeNotChangingOverTime() throws Exception {
        Set<IIpsObject> modifiedIpsObjects = Collections.emptySet();
        IProductCmptType newProductCmptType = newProductCmptType(ipsProject, "my.NewProductCmptType");
        // would come from the default as set in the project properties
        newProductCmptType.setChangingOverTime(true);

        ProductCmptType superProductCmptType = newProductCmptType(ipsProject, "test.SuperType");
        superProductCmptType.setChangingOverTime(false);

        NewProductCmptTypePage newProductCmptTypePage = new TestNewProductCmptTypePage("test.SuperType");

        newProductCmptTypePage.finishIpsObjectsExtension(newProductCmptType, modifiedIpsObjects);

        assertTrue(modifiedIpsObjects.isEmpty());
        assertFalse(newProductCmptType.isChangingOverTime());
    }

    /*
     * Overwrite some methods that delegate directly to UI-Fields we don't want to initialise for
     * this test
     */
    private final class TestNewProductCmptTypePage extends NewProductCmptTypePage {
        private String superType;

        private TestNewProductCmptTypePage(String superType) {
            super(selection);
            this.superType = superType;
        }

        @Override
        public String getSuperType() {
            return superType;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        protected String getPolicyCmptTypeName() {
            return "";
        }

        @Override
        protected IIpsProject getIpsProject() {
            return ipsProject;
        }
    }

}
