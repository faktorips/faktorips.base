/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.model.builder.java.annotations.attribute.AttributeAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.enums.EnumAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.PolicyCmptAssociationAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.PolicyCmptDeclClassAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.PolicyCmptSeparateValidatorClassAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.policycmpt.persistence.PolicyCmptImplClassJpaAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.productcmpt.ProductCmptAssociationAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.productcmpt.ProductCmptDeclClassAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.productcmpt.TableUsageAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.table.TableAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.validation.ValidatedByAnnGenFactory;
import org.faktorips.devtools.model.builder.java.annotations.validation.ValidationRuleAnnGenFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class AnnotationGeneratorBuilder {

    private final IAnnotationGeneratorFactory[] annotationGeneratorFactories;

    private final IIpsProject ipsProject;

    private final DeprecationAnnotationGenerator deprecationAnnotationGenerator = new DeprecationAnnotationGenerator();
    private final DeprecationJavadocTagGenerator deprecationJavadocTagGenerator = new DeprecationJavadocTagGenerator();

    public AnnotationGeneratorBuilder(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        annotationGeneratorFactories = new IAnnotationGeneratorFactory[] {
                // JPA support
                new PolicyCmptImplClassJpaAnnGenFactory(),
                // Jaxb support
                new JaxbAnnGenFactory(),
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
                // separate validator class
                new PolicyCmptSeparateValidatorClassAnnGenFactory(),
                // product component implementation classes
                new ProductCmptDeclClassAnnGenFactory(),
                // published interfaces
                new PublishedInterfaceAnnGenFactory(),
                // extension properties
                new ExtensionPropertyAnnGen.Factory(),
                // labels and descriptions
                new LabelAndDescriptionAnnGen.Factory(),
                // table usage
                new TableUsageAnnGenFactory(),
                // enums
                new EnumAnnGenFactory(),
                // separate validator class
                new ValidatedByAnnGenFactory(),
                // validation rule
                new ValidationRuleAnnGenFactory() };
    }

    public Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> createAnnotationGenerators() {
        HashMap<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorsMap = new HashMap<>();
        List<IAnnotationGeneratorFactory> factories = getAnnotationGeneratorFactoriesRequiredForProject();
        for (AnnotatedJavaElementType type : AnnotatedJavaElementType.values()) {
            ArrayList<IAnnotationGenerator> annotationGenerators = new ArrayList<>();
            addDeprecationAnnotationGenerator(annotationGenerators, type);
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

    private void addDeprecationAnnotationGenerator(ArrayList<IAnnotationGenerator> annotationGenerators,
            AnnotatedJavaElementType type) {
        switch (type) {
            case POLICY_CMPT_DECL_CLASS:
            case POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD:
            case PRODUCT_CMPT_DECL_CLASS:
                break;
            case ELEMENT_JAVA_DOC:
                annotationGenerators.add(deprecationJavadocTagGenerator);
                break;

            default:
                annotationGenerators.add(deprecationAnnotationGenerator);
                break;
        }
    }

    private List<IAnnotationGeneratorFactory> getAnnotationGeneratorFactoriesRequiredForProject() {
        List<IAnnotationGeneratorFactory> factories = new ArrayList<>();
        for (IAnnotationGeneratorFactory annotationGeneratorFactorie : annotationGeneratorFactories) {
            if (annotationGeneratorFactorie.isRequiredFor(ipsProject)) {
                factories.add(annotationGeneratorFactorie);
            }
        }
        return factories;
    }

}
