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

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.decorators.internal.IpsModelDecoratorsPluginActivator;
import org.faktorips.devtools.model.decorators.internal.SimpleIpsElementDecorator;

public class TestSampleIpsElementDecorator extends SimpleIpsElementDecorator {

    public TestSampleIpsElementDecorator() {
        super(ImageDescriptor.createFromURL(
                IpsModelDecoratorsPluginActivator.getBundle().getEntry("testicons/TestSampleIpsElement.gif")));
    }

}
