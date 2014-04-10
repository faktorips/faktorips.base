/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Test;

public class OpenProductCmptEditorTest extends AbstractIpsPluginTest {

    private static final String EDITOR_ID = "productCmptEditor";
    private IFileEditorInput editorInput;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IpsPlugin.getDefault().setTestMode(true);
        IIpsProject project = newIpsProject();
        ProductCmpt productCmpt = newProductCmpt(project, "ProductCmpt");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(new GregorianCalendar());

        IConfigElement configElement = generation.newConfigElement();
        configElement.setValueSetType(ValueSetType.ENUM);

        editorInput = ProductCmptEditorInput.createWithGeneration(generation);
    }

    @Test
    public void testOpenEditorWithConfigElementAndEnumDatatype() throws PartInitException, Exception {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .openEditor(editorInput, EDITOR_ID);

        assertNotNull(editor);
        assertThat(editor, IsInstanceOf.instanceOf(ProductCmptEditor.class));
        // and not an ErrorEditorPart
    }
}
