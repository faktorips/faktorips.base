/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.GregorianCalendar;

import org.eclipse.ui.IEditorPart;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;

public class OpenProductCmptEditorTest extends AbstractIpsPluginTest {

    private IIpsObject ipsObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(project, "PolicyCmpt", "ProductCmpt");

        IProductCmptType productCmptType = policyCmptType.findProductCmptType(project);
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(new GregorianCalendar());

        ipsObject = generation.getIpsObject();
    }

    @Test
    public void testOpenEditorWithConfigElement() {
        IEditorPart editor = IpsUIPlugin.getDefault().openEditor(ipsObject);

        assertNotNull(editor);
        assertThat(editor, IsInstanceOf.instanceOf(ProductCmptEditor.class));
    }
}
