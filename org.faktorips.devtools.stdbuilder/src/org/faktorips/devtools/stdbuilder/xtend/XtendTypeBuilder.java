/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XType;

public abstract class XtendTypeBuilder<T extends XType> extends XtendBuilder<T> {

    private final boolean interfaceBuilder;

    public XtendTypeBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, modelContext, modelService, localizedStringsSet);
        this.interfaceBuilder = interfaceBuilder;
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return interfaceBuilder
                || !getGeneratorModelContext().getBaseGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject());
    }

    @Override
    protected boolean generatesInterface() {
        return interfaceBuilder;
    }

}
