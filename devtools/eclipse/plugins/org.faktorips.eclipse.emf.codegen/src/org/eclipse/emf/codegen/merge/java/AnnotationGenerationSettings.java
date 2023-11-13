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
 * (and their corresponding imports) are generated on all generated elements (with the exception on
 * methods marked as {@code @restrainedmodifiable}, if the
 * {@link AdditionalAnnotationsLocation#OnlyGenerated} is set), while annotations already present on
 * the corresponding elements in the previous code are only retained if they are included in the
 * list of retained annotations.
 *
 * @since 24.1
 */
public record AnnotationGenerationSettings(List<String> additionalImports, List<String> additionalAnnotations,
        AdditionalAnnotationsLocation additionalAnnotationsLocation, List<String> retainedAnnotations) {

    public AnnotationGenerationSettings {
        additionalImports = List.copyOf(additionalImports);
        additionalAnnotations = List.copyOf(additionalAnnotations);
        retainedAnnotations = List.copyOf(retainedAnnotations);
    }

    public AnnotationGenerationSettings(List<String> additionalImports, List<String> additionalAnnotations,
            String additionalAnnotationsLocation, List<String> retainedAnnotations) {
        this(additionalImports, additionalAnnotations,
                AdditionalAnnotationsLocation.fromString(additionalAnnotationsLocation),
                retainedAnnotations);
    }

}
