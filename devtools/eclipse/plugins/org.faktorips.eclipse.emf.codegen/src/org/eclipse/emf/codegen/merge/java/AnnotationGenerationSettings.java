/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.eclipse.emf.codegen.merge.java;

import java.util.List;

import org.eclipse.emf.codegen.AdditionalAnnotationsLocation;

/**
 * These settings control the handling of annotations by {@link JMerger}. Additional annotations
 * (and their corresponding imports) are generated on generated methods, while additional annotations
 * for fields are generated on generated fields and enum constants. Members marked as
 * {@code @restrainedmodifiable} are excluded when {@link AdditionalAnnotationsLocation#OnlyGenerated}
 * is set. Annotations already present on the corresponding elements in the previous code are only
 * retained if they are included in the list of retained annotations.
 * <p>
 * If both {@code generatedStartTag} and {@code generatedEndTag} are non-empty, a
 * {@link GeneratedMemberMarkerInjector} will wrap each {@code @generated} member with those markers
 * in the final source output. Both must be set together or both left empty.
 *
 * @since 24.1
 */
public record AnnotationGenerationSettings(List<String> additionalImports, List<String> additionalAnnotations,
        List<String> additionalImportsForFields, List<String> additionalAnnotationsForFields,
        AdditionalAnnotationsLocation additionalAnnotationsLocation, List<String> retainedAnnotations,
        String generatedStartTag, String generatedEndTag) {

    public AnnotationGenerationSettings {
        additionalImports = List.copyOf(additionalImports);
        additionalAnnotations = List.copyOf(additionalAnnotations);
        additionalImportsForFields = List.copyOf(additionalImportsForFields);
        additionalAnnotationsForFields = List.copyOf(additionalAnnotationsForFields);
        retainedAnnotations = List.copyOf(retainedAnnotations);
        generatedStartTag = generatedStartTag != null ? generatedStartTag : "";
        generatedEndTag = generatedEndTag != null ? generatedEndTag : "";
    }

    public AnnotationGenerationSettings(List<String> additionalImports, List<String> additionalAnnotations,
            List<String> additionalImportsForFields, List<String> additionalAnnotationsForFields,
            String additionalAnnotationsLocation, List<String> retainedAnnotations,
            String generatedStartTag, String generatedEndTag) {
        this(additionalImports, additionalAnnotations,
                additionalImportsForFields, additionalAnnotationsForFields,
                AdditionalAnnotationsLocation.fromString(additionalAnnotationsLocation),
                retainedAnnotations, generatedStartTag, generatedEndTag);
    }

}
