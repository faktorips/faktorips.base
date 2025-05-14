/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmptKind;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.junit.Test;

public class ProductCmptKindTest extends AbstractIpsPluginTest {

    @Test
    public void testCreateProductCmptKindStringIIpsProject_NoNamingStrategy_NoPrefix() {
        IIpsProject ipsProject = newIpsProject();

        IProductCmptKind productCmptKind = ProductCmptKind.createProductCmptKind("Foo 2025-01", ipsProject);

        assertThat(productCmptKind.getName(), is("Foo 2025-01"));
        assertThat(productCmptKind.getRuntimeId(), is("Foo 2025-01"));
    }

    @Test
    public void testCreateProductCmptKindStringIIpsProject() {
        IIpsProject ipsProject = newIpsProject();
        IProductCmptNamingStrategy strategy = new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false);
        setProjectProperty(ipsProject, props -> {
            props.setProductCmptNamingStrategy(strategy);
            props.setRuntimeIdPrefix("pre.");
        });

        IProductCmptKind productCmptKind = ProductCmptKind.createProductCmptKind("Foo 2025-01", ipsProject);

        assertThat(productCmptKind.getName(), is("Foo"));
        assertThat(productCmptKind.getRuntimeId(), is("pre.Foo"));
    }

    @Test
    public void testCreateProductCmptKindStringIProductCmptNamingStrategyString() {
        IProductCmptKind productCmptKind = ProductCmptKind.createProductCmptKind("Bar 2025-01",
                new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false), "foo.");

        assertThat(productCmptKind.getName(), is("Bar"));
        assertThat(productCmptKind.getRuntimeId(), is("foo.Bar"));
    }

    @Test
    public void testCreateProductCmptKindStringIProductCmptNamingStrategyString_WithPostfix() {
        IProductCmptKind productCmptKind = ProductCmptKind.createProductCmptKind("Bar 2025-01b",
                new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", true), "foo.");

        assertThat(productCmptKind.getName(), is("Bar"));
        assertThat(productCmptKind.getRuntimeId(), is("foo.Bar"));
    }

    @Test
    public void testCreateProductCmptKindStringIProductCmptNamingStrategyString_NoPrefix() {
        IProductCmptKind productCmptKind = ProductCmptKind.createProductCmptKind("Bar 2025-01",
                new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false), "");

        assertThat(productCmptKind.getName(), is("Bar"));
        assertThat(productCmptKind.getRuntimeId(), is("Bar"));
    }

    @Test
    public void testCreateProductCmptKindStringIProductCmptNamingStrategyString_NoVersionIdProductCmptNamingStrategy() {
        IProductCmptKind productCmptKind = ProductCmptKind.createProductCmptKind("Bar 2025-01",
                new NoVersionIdProductCmptNamingStrategy(), "foo.");

        assertThat(productCmptKind.getName(), is("Bar 2025-01"));
        assertThat(productCmptKind.getRuntimeId(), is("foo.Bar 2025-01"));
    }

    @Test
    public void testCreateProductCmptKindStringIProductCmptNamingStrategyString_NonParsable() {
        IProductCmptKind productCmptKind = ProductCmptKind.createProductCmptKind("Bar",
                new DateBasedProductCmptNamingStrategy(" ", "yyyy-MM", false), "foo.");

        assertThat(productCmptKind, is(nullValue()));
    }

}
