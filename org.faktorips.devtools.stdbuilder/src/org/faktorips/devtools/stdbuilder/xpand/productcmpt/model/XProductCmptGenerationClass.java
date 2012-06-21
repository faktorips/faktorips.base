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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenJavaClassNameProvider;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.internal.ProductComponentGeneration;

public class XProductCmptGenerationClass extends XProductClass {

    private final IJavaClassNameProvider prodGenJavaClassNameProvider;
    protected final List<XProductAttribute> attributes;
    protected final List<XProductAssociation> associations;

    public XProductCmptGenerationClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);
        prodGenJavaClassNameProvider = createProductCmptGenJavaClassNaming(getLanguageUsedInGeneratedSourceCode());

        attributes = initNodesForParts(getChangeableAttributes(), XProductAttribute.class);
        associations = initNodesForParts(getChangableAssociations(), XProductAssociation.class);
    }

    public static ProductCmptGenJavaClassNameProvider createProductCmptGenJavaClassNaming(Locale locale) {
        return new ProductCmptGenJavaClassNameProvider(locale) {

            @Override
            public String getImplClassName(IIpsSrcFile ipsSrcFile) {
                return super.getImplClassName(ipsSrcFile);// + "_X";
            }

        };
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return prodGenJavaClassNameProvider;
    }

    @Override
    public IProductCmptType getIpsObjectPartContainer() {
        return super.getIpsObjectPartContainer();
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponentGeneration.class.getName());
    }

    @Override
    public List<XProductAttribute> getAttributes() {
        return new CopyOnWriteArrayList<XProductAttribute>(attributes);
    }

    @Override
    public List<XProductAssociation> getAssociations() {
        // TODO This sorting is only needed to get the exactly same code as in former code
        // generator.

        List<XProductAssociation> result = new ArrayList<XProductAssociation>();
        for (XProductAssociation association : associations) {
            if (association.isOnetoMany() && association.hasMatchingAssociation()) {
                result.add(association);
            }
        }
        for (XProductAssociation association : associations) {
            if (!association.isOnetoMany() && association.hasMatchingAssociation()) {
                result.add(association);
            }
        }
        for (XProductAssociation association : associations) {
            if (association.isOnetoMany() && !association.hasMatchingAssociation()) {
                result.add(association);
            }
        }
        for (XProductAssociation association : associations) {
            if (!association.isOnetoMany() && !association.hasMatchingAssociation()) {
                result.add(association);
            }
        }
        return result;

        // return new CopyOnWriteArrayList<XProductAssociation>(associations);
    }

}
