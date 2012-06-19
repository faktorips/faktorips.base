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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.internal.ProductComponent;

public class XProductCmptClass extends XProductClass {
    private final List<XProductAttribute> attributes;
    private final List<XProductAssociation> associations;

    public XProductCmptClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);

        attributes = initNodesForParts(getStaticAttributes(), XProductAttribute.class);
        associations = initNodesForParts(getStaticAssociations(), XProductAssociation.class);
    }

    @Override
    public List<XProductAttribute> getAttributes() {
        return new CopyOnWriteArrayList<XProductAttribute>(attributes);
    }

    @Override
    public List<XProductAssociation> getAssociations() {
        return new CopyOnWriteArrayList<XProductAssociation>(associations);
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponent.class);
    }

    public String getGetterMethodNameForGeneration() {
        IChangesOverTimeNamingConvention convention = getProductCmptType().getIpsProject()
                .getChangesInTimeNamingConventionForGeneratedCode();
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String generationConceptAbbreviation = convention.getGenerationConceptNameAbbreviation(locale);
        return "get" + getProductCmptType().getName() + generationConceptAbbreviation;
    }
}
