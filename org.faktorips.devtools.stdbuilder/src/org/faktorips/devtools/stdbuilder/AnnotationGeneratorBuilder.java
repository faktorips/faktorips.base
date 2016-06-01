/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassJaxbAnnGenFactory;
import org.faktorips.devtools.stdbuilder.policycmpttype.persistence.PolicyCmptImplClassJpaAnnGenFactory;
import org.faktorips.devtools.stdbuilder.xpand.PublishedInterfaceAnnGenFactory;
import org.faktorips.devtools.stdbuilder.xpand.attribute.AttributeAnnGenFactory;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptAssociationAnnGenFactory;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptDeclClassAnnGenFactory;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptAssociationAnnGenFactory;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptDeclClassAnnGenFactory;
import org.faktorips.devtools.stdbuilder.xpand.table.TableAnnGenFactory;

public class AnnotationGeneratorBuilder {

    private final IAnnotationGeneratorFactory[] annotationGeneratorFactories;

    private final IIpsProject ipsProject;

    public AnnotationGeneratorBuilder(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        annotationGeneratorFactories = new IAnnotationGeneratorFactory[] {
                // JPA support
                new PolicyCmptImplClassJpaAnnGenFactory(),
                // Jaxb support
                new PolicyCmptImplClassJaxbAnnGenFactory(),
                // since version java doc
                new SinceVersionJavaDocTagGenerator.Factory(),
                // tables and table rows
                new TableAnnGenFactory(),
                // attributes
                new AttributeAnnGenFactory(),
                // policy associations
                new PolicyCmptAssociationAnnGenFactory(),
                // product associations
                new ProductCmptAssociationAnnGenFactory(),
                // policy component implementation classes
                new PolicyCmptDeclClassAnnGenFactory(),
                // product component implementation classes
                new ProductCmptDeclClassAnnGenFactory(),
                // published interfaces
                new PublishedInterfaceAnnGenFactory(),
                // extension properties
                new ExtensionPropertyAnnGen.Factory() };
    }

    public Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> createAnnotationGenerators() {
        HashMap<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorsMap = new HashMap<AnnotatedJavaElementType, List<IAnnotationGenerator>>();
        List<IAnnotationGeneratorFactory> factories = getAnnotationGeneratorFactoriesRequiredForProject();

        for (AnnotatedJavaElementType type : AnnotatedJavaElementType.values()) {
            ArrayList<IAnnotationGenerator> annotationGenerators = new ArrayList<IAnnotationGenerator>();
            for (IAnnotationGeneratorFactory annotationGeneratorFactory : factories) {
                IAnnotationGenerator annotationGenerator;
                annotationGenerator = annotationGeneratorFactory.createAnnotationGenerator(type);
                if (annotationGenerator != null) {
                    annotationGenerators.add(annotationGenerator);
                }
            }
            annotationGeneratorsMap.put(type, annotationGenerators);
        }
        return annotationGeneratorsMap;
    }

    private List<IAnnotationGeneratorFactory> getAnnotationGeneratorFactoriesRequiredForProject() {
        List<IAnnotationGeneratorFactory> factories = new ArrayList<IAnnotationGeneratorFactory>();
        for (IAnnotationGeneratorFactory annotationGeneratorFactorie : annotationGeneratorFactories) {
            if (annotationGeneratorFactorie.isRequiredFor(ipsProject)) {
                factories.add(annotationGeneratorFactorie);
            }
        }
        return factories;
    }

}
