/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.descriptorOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.internal.test.TestSampleIpsElement;
import org.faktorips.devtools.model.decorators.internal.test.TestSampleIpsElementDecorator;
import org.faktorips.devtools.model.decorators.internal.test.TestSampleIpsElementSubClass;
import org.faktorips.devtools.model.decorators.internal.test.UndecoratedIpsElement;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.junit.Test;

public class IpsDecoratorsTest {

    @SuppressWarnings("deprecation")
    private IpsDecorators ipsDecorators = IpsDecorators.get();

    @Test
    public void testGetDecorator_IncludesDecoratorFromExtensionPoint() throws Exception {
        IIpsElementDecorator decorator = ipsDecorators.getDecorator(TestSampleIpsElement.class);

        assertThat(decorator, is(instanceOf(TestSampleIpsElementDecorator.class)));
    }

    @Test
    public void testGetDecorator_ForSubclass() throws Exception {
        IIpsElementDecorator decorator = ipsDecorators.getDecorator(TestSampleIpsElementSubClass.class);

        assertThat(decorator, is(instanceOf(TestSampleIpsElementDecorator.class)));
    }

    @Test
    public void testGetDecorator_UnknownIpsElement() throws Exception {
        IIpsElementDecorator decorator = ipsDecorators.getDecorator(UndecoratedIpsElement.class);

        assertThat(decorator, is(sameInstance(IIpsElementDecorator.MISSING_ICON_PROVIDER)));
    }

    @Test
    public void testGetDecorator_ForIpsObjectType() throws Exception {
        IIpsElementDecorator decorator = ipsDecorators.getDecorator(IpsObjectType.POLICY_CMPT_TYPE);

        assertThat(decorator, is(instanceOf(SimpleIpsElementDecorator.class)));
        assertThat(decorator.getDefaultImageDescriptor(), is(descriptorOf(IpsDecorators.POLICY_CMPT_TYPE_IMAGE)));
    }

    @Test
    public void testGetDecorator_ForIpsObjectType_Template() throws Exception {
        IIpsElementDecorator decorator = ipsDecorators.getDecorator(IpsObjectType.PRODUCT_TEMPLATE);

        assertThat(decorator, is(instanceOf(ProductCmptDecorator.class)));
        assertThat(decorator.getDefaultImageDescriptor(),
                is(descriptorOf(ProductCmptDecorator.PRODUCT_CMPT_TEMPLATE_BASE_IMAGE)));
    }

    @Test
    public void testGetDecoratedClasses() throws Exception {
        Collection<Class<? extends IIpsElement>> decoratedClasses = ipsDecorators.getDecoratedClasses();

        assertThat(decoratedClasses, hasItem(IpsProject.class));
        // from extension point
        assertThat(decoratedClasses, hasItem(TestSampleIpsElement.class));
        // no decorator registered
        assertThat(decoratedClasses, not(hasItem(UndecoratedIpsElement.class)));
    }

}
