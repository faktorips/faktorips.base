/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.junit.Before;
import org.mockito.Mock;

public class AbstractTestForGeneratorModelNodes {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Before
    public void initTestObjects() {
    }

    public GeneratorModelContext getModelContext() {
        return modelContext;
    }

    public ModelService getModelService() {
        return modelService;
    }

}
