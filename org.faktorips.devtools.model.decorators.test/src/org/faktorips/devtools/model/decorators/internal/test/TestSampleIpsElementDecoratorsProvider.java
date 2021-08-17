/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal.test;

import java.util.Collections;
import java.util.Map;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.decorators.IIpsElementDecorator;
import org.faktorips.devtools.model.decorators.IIpsElementDecoratorsProvider;

public class TestSampleIpsElementDecoratorsProvider implements IIpsElementDecoratorsProvider {

    private Map<Class<? extends IIpsElement>, IIpsElementDecorator> decorators;

    public TestSampleIpsElementDecoratorsProvider() {
        decorators = Collections.singletonMap(TestSampleIpsElement.class, new TestSampleIpsElementDecorator());
    }

    @Override
    public Map<Class<? extends IIpsElement>, IIpsElementDecorator> getDecoratorsByElementClass() {
        return decorators;
    }

}
