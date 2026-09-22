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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Opening this dialog builds a {@code ValueSetSpecificationControl} with the caller-supplied
 * allowed types and then calls {@code syncSelectionToModel()} once, so an already-valid configured
 * value set must survive unchanged, while one whose type is no longer in the allowed list must be
 * corrected.
 */
public class AnyValueSetEditDialogTest extends AbstractIpsPluginTest {

    private static final List<String> VALUES = Arrays.asList("1", "2", "4");

    private IConfiguredValueSet configuredValueSet;
    private Shell shell;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = newIpsProject();
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "PolicyType");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("attr");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setValueSetType(ValueSetType.ENUM);

        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(0);
        configuredValueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        configuredValueSet.setValueSetType(ValueSetType.ENUM);
        ((IEnumValueSet)configuredValueSet.getValueSet()).addValues(VALUES);
        productCmpt.getIpsSrcFile().save(null);

        shell = new Shell(Display.getCurrent());
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        shell.dispose();
        super.tearDown();
    }

    @Test
    public void testOpeningDialog_AllowedTypesStillContainCurrentType_PreservesValueSetAndValues() {
        AnyValueSetEditDialog dialog = new AnyValueSetEditDialog(configuredValueSet,
                Arrays.asList(ValueSetType.ENUM, ValueSetType.UNRESTRICTED), shell);
        try {
            dialog.create();

            assertThat(configuredValueSet.getValueSet().getValueSetType(), is(ValueSetType.ENUM));
            assertThat(((IEnumValueSet)configuredValueSet.getValueSet()).getValuesAsList(), is(VALUES));
        } finally {
            dialog.close();
        }
    }

    @Test
    public void testOpeningDialog_AllowedTypesNoLongerContainCurrentType_SwitchesToFirstAllowedType() {
        AnyValueSetEditDialog dialog = new AnyValueSetEditDialog(configuredValueSet,
                Arrays.asList(ValueSetType.UNRESTRICTED, ValueSetType.DERIVED), shell);
        try {
            dialog.create();

            assertThat(configuredValueSet.getValueSet().getValueSetType(), is(ValueSetType.UNRESTRICTED));
        } finally {
            dialog.close();
        }
    }
}
