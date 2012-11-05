/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand;

import java.util.Map;

import org.eclipse.xtend.expression.Variable;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.util.LocalizedStringsSet;

public abstract class TypeBuilder<T extends XType> extends XpandBuilder<T> {

    private static final String GENERATE_INTERFACE = "generateInterface";
    private final boolean interfaceBuilder;

    public TypeBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, modelContext, modelService, localizedStringsSet);
        this.interfaceBuilder = interfaceBuilder;
    }

    public boolean isInterfaceBuilder() {
        return interfaceBuilder;
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return isInterfaceBuilder() || !getGeneratorModelContext().isGeneratePublishedInterfaces();
    }

    @Override
    protected Map<String, Variable> getGlobalVars() {
        Map<String, Variable> map = super.getGlobalVars();
        Variable generateInterface = new Variable(GENERATE_INTERFACE, generatesInterface());
        map.put(generateInterface.getName(), generateInterface);
        return map;
    }

    @Override
    protected boolean generatesInterface() {
        return isInterfaceBuilder();
    }

}