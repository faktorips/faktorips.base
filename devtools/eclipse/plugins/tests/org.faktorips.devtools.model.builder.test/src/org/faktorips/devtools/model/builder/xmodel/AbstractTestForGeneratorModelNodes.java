/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import org.junit.Before;
import org.mockito.Mock;

public class AbstractTestForGeneratorModelNodes {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Before
    public void initTestObjects() {
        // nothing to do
    }

    public GeneratorModelContext getModelContext() {
        return modelContext;
    }

    public ModelService getModelService() {
        return modelService;
    }

}
