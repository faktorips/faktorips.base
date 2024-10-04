/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.migration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestIpsModelExtensions;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.migration.MigrationForChangedAttribute.ChangedAttribute;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.devtools.model.versionmanager.IIpsFeatureVersionManager;
import org.junit.Test;

public class MigrationForChangedAttributeTest extends AbstractIpsPluginTest {

    private static final String FEATURE_ID = "my.feature";

    private IIpsFeatureVersionManager versionManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        versionManager = mock(IIpsFeatureVersionManager.class);
        when(versionManager.getFeatureId()).thenReturn(FEATURE_ID);
        when(versionManager.isRequiredForAllProjects()).thenReturn(false);
    }

    @Test
    public void testMigrationOfProductAttribute() {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            testIpsModelExtensions.setFeatureVersionManagers(versionManager);
            IIpsProject ipsProject = newIpsProjectWithFeature();
            ProductCmptType changedProductCmptType = newProductCmptType(ipsProject,
                    "model.products.ChangedProductCmptType");
            String oldAttributeName = "oldAttributeName";
            IProductCmptTypeAttribute changedAttribute = newAttribute(changedProductCmptType, oldAttributeName);
            ProductCmpt productCmpt = newProductCmpt(changedProductCmptType, "productToChange");
            setAttributeValue(productCmpt, changedAttribute, "aValue");
            changedAttribute.setName("newAttributeName");
            changedProductCmptType.getIpsSrcFile().save(null);
            MigrationForChangedAttribute migration = new MigrationForChangedAttribute(ipsProject, FEATURE_ID, "2",
                    "a description", new ChangedAttribute(
                            "model.products.ChangedProductCmptType", oldAttributeName, "newAttributeName"));

            migration.migrate(productCmpt.getIpsSrcFile());

            assertThat(productCmpt.getAttributeValue("oldAttributeName"), is(nullValue()));
            assertThat(productCmpt.getAttributeValue("newAttributeName").getValueHolder().getStringValue(),
                    is("aValue"));
        }
    }

    @Test
    public void testMigrationOfPolicyAttribute() {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            testIpsModelExtensions.setFeatureVersionManagers(versionManager);
            IIpsProject ipsProject = newIpsProjectWithFeature();
            PolicyCmptType changedPolicyCmptType = newPolicyAndProductCmptType(ipsProject,
                    "model.ChangedPolicyCmptType", "model.ChangedProductCmptType");
            IProductCmptType changedProductCmptType = changedPolicyCmptType.findProductCmptType(ipsProject);
            String oldAttributeName = "oldAttributeName";
            IPolicyCmptTypeAttribute changedAttribute = newAttribute(changedPolicyCmptType, oldAttributeName);
            ProductCmpt productCmpt = newProductCmpt(changedProductCmptType, "productToChange");
            setDefaultValue(productCmpt, changedAttribute, "aValue");
            setValueSetStringLength(productCmpt, changedAttribute, "10");
            changedAttribute.setName("newAttributeName");
            changedPolicyCmptType.getIpsSrcFile().save(null);
            MigrationForChangedAttribute migration = new MigrationForChangedAttribute(ipsProject, FEATURE_ID, "2",
                    "a description", new ChangedAttribute(
                            "model.ChangedPolicyCmptType", oldAttributeName, "newAttributeName"));

            migration.migrate(productCmpt.getIpsSrcFile());

            assertThat(productCmpt.getPropertyValue("oldAttributeName", IConfiguredDefault.class), is(nullValue()));
            assertThat(productCmpt.getPropertyValue("newAttributeName", IConfiguredDefault.class).getValue(),
                    is("aValue"));
            assertThat(productCmpt.getPropertyValue("oldAttributeName", IConfiguredValueSet.class), is(nullValue()));
            assertThat(productCmpt.getPropertyValue("newAttributeName", IConfiguredValueSet.class).getValueSet(),
                    is(instanceOf(IStringLengthValueSet.class)));
            assertThat(
                    ((IStringLengthValueSet)productCmpt.getPropertyValue("newAttributeName", IConfiguredValueSet.class)
                            .getValueSet()).getMaximumLength(),
                    is("10"));
        }
    }

    @Test
    public void testMigrationOfSubtype() {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            testIpsModelExtensions.setFeatureVersionManagers(versionManager);
            IIpsProject ipsProject = newIpsProjectWithFeature();
            ProductCmptType changedProductCmptType = newProductCmptType(ipsProject,
                    "model.products.ChangedProductCmptType");
            ProductCmptType subProductCmptType = newProductCmptType(ipsProject,
                    "model.products.SubProductCmptType");
            subProductCmptType.setSupertype("model.products.ChangedProductCmptType");
            String oldAttributeName = "oldAttributeName";
            IProductCmptTypeAttribute changedAttribute = newAttribute(changedProductCmptType, oldAttributeName);
            ProductCmpt productCmpt = newProductCmpt(subProductCmptType, "productToChange");
            setAttributeValue(productCmpt, changedAttribute, "aValue");
            changedAttribute.setName("newAttributeName");
            changedProductCmptType.getIpsSrcFile().save(null);
            MigrationForChangedAttribute migration = new MigrationForChangedAttribute(ipsProject, FEATURE_ID, "2",
                    "a description", new ChangedAttribute(
                            "model.products.ChangedProductCmptType", oldAttributeName, "newAttributeName"));

            migration.migrate(productCmpt.getIpsSrcFile());

            assertThat(productCmpt.getAttributeValue("oldAttributeName"), is(nullValue()));
            assertThat(productCmpt.getAttributeValue("newAttributeName").getValueHolder().getStringValue(),
                    is("aValue"));
        }
    }

    @Test
    public void testMigrationTwice() {
        try (TestIpsModelExtensions testIpsModelExtensions = TestIpsModelExtensions.get()) {
            testIpsModelExtensions.setFeatureVersionManagers(versionManager);
            IIpsProject ipsProject = newIpsProjectWithFeature();
            ProductCmptType changedProductCmptType = newProductCmptType(ipsProject,
                    "model.products.ChangedProductCmptType");
            String oldAttributeName = "oldAttributeName";
            IProductCmptTypeAttribute changedAttribute = newAttribute(changedProductCmptType, oldAttributeName);
            ProductCmpt productCmpt = newProductCmpt(changedProductCmptType, "productToChange");
            setAttributeValue(productCmpt, changedAttribute, "aValue");
            changedAttribute.setName("newAttributeName");
            changedProductCmptType.getIpsSrcFile().save(null);
            MigrationForChangedAttribute migration = new MigrationForChangedAttribute(ipsProject, FEATURE_ID, "2",
                    "a description", new ChangedAttribute(
                            "model.products.ChangedProductCmptType", oldAttributeName, "newAttributeName"));

            migration.migrate(productCmpt.getIpsSrcFile());

            assertThat(productCmpt.getAttributeValue("oldAttributeName"), is(nullValue()));
            assertThat(productCmpt.getAttributeValue("newAttributeName").getValueHolder().getStringValue(),
                    is("aValue"));

            migration.migrate(productCmpt.getIpsSrcFile());

            assertThat(productCmpt.getAttributeValue("oldAttributeName"), is(nullValue()));
            assertThat(productCmpt.getAttributeValue("newAttributeName").getValueHolder().getStringValue(),
                    is("aValue"));
        }
    }

    private void setAttributeValue(ProductCmpt productCmpt,
            IProductCmptTypeAttribute changedAttribute,
            String value) {
        IAttributeValue attributeValue = productCmpt.newPropertyValue(changedAttribute, IAttributeValue.class);
        attributeValue.setAttribute(changedAttribute.getName());
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, value));
        productCmpt.getIpsSrcFile().save(null);
    }

    private IProductCmptTypeAttribute newAttribute(ProductCmptType changedProductCmptType, String name) {
        IProductCmptTypeAttribute changedAttribute = changedProductCmptType.newProductCmptTypeAttribute(name);
        changedAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        changedProductCmptType.getIpsSrcFile().save(null);
        return changedAttribute;
    }

    private void setDefaultValue(ProductCmpt productCmpt,
            IPolicyCmptTypeAttribute changedAttribute,
            String value) {
        IConfiguredDefault defaultValue = productCmpt.newPropertyValue(changedAttribute, IConfiguredDefault.class);
        defaultValue.setPolicyCmptTypeAttribute(changedAttribute.getName());
        defaultValue.setValue(value);
        productCmpt.getIpsSrcFile().save(null);
    }

    private void setValueSetStringLength(ProductCmpt productCmpt,
            IPolicyCmptTypeAttribute changedAttribute,
            String value) {
        IConfiguredValueSet configuredValueSet = productCmpt.newPropertyValue(changedAttribute,
                IConfiguredValueSet.class);
        configuredValueSet.setPolicyCmptTypeAttribute(changedAttribute.getName());
        IStringLengthValueSet valueSet = (IStringLengthValueSet)configuredValueSet
                .changeValueSetType(ValueSetType.STRINGLENGTH);
        valueSet.setMaximumLength(value);
        productCmpt.getIpsSrcFile().save(null);
    }

    private IPolicyCmptTypeAttribute newAttribute(PolicyCmptType changedPolicyCmptType, String name) {
        IPolicyCmptTypeAttribute changedAttribute = changedPolicyCmptType.newPolicyCmptTypeAttribute(name);
        changedAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        changedAttribute.setValueSetConfiguredByProduct(true);
        changedPolicyCmptType.getIpsSrcFile().save(null);
        return changedAttribute;
    }

    private IIpsProject newIpsProjectWithFeature() {
        IIpsProject ipsProject = newIpsProject();
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setMinRequiredVersionNumber(FEATURE_ID, "1");
        ipsProject.setProperties(props);
        return ipsProject;
    }

}
