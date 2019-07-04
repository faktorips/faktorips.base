/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;

import org.eclipse.ui.IEditorPart;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;

public class OpenProductCmptEditorTest extends AbstractIpsPluginTest {

    private IIpsObject ipsObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IpsPlugin.getDefault().setTestMode(true);
        IIpsProject project = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(project, "PolicyCmpt", "ProductCmpt");

        IProductCmptType productCmptType = policyCmptType.findProductCmptType(project);
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(new GregorianCalendar());

        ipsObject = generation.getIpsObject();
    }

    @Override
    public void tearDown() throws Exception {
        IpsPlugin.getDefault().setTestMode(false);
        super.tearDown();
    }

    @Test
    public void testOpenEditorWithConfigElement() {
        IEditorPart editor = IpsUIPlugin.getDefault().openEditor(ipsObject);

        assertNotNull(editor);
        assertThat(editor, IsInstanceOf.instanceOf(ProductCmptEditor.class));
    }
}
