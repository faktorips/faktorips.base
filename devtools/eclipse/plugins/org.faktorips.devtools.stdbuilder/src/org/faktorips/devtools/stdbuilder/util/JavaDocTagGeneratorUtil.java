/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.util;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.model.builder.java.JavaSourceFileBuilder;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.GenericGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;

/**
 * This class is used to create the java doc tags provided by an {@link IAnnotationGenerator} in old
 * style builder that do not use Xpand/Xtend and the {@link ModelService} yet.
 * <p>
 * This class will be deleted when it is no longer needed.
 */
public class JavaDocTagGeneratorUtil {

    private JavaDocTagGeneratorUtil() {
        // do not instantiate
    }

    public static List<String> getJavaDocTags(IIpsObjectPartContainer element, StandardBuilderSet builderSet) {
        ArrayList<String> javaDocTags = new ArrayList<>();
        if (element != null) {
            GenericGeneratorModelNode modelNode = builderSet.getModelNode(element, GenericGeneratorModelNode.class);
            String annotation = modelNode.getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC);
            if (IpsStringUtils.isNotEmpty(annotation)) {
                /*
                 * Remove "@" from the beginning of the annotation string, as callers expect to add
                 * their own. Works even if there are multiple annotations in the annotation string,
                 * as only one "@" is added.
                 */
                javaDocTags.add(annotation.substring(1));
            }
        }
        return javaDocTags;
    }

    public static String[] getJavaDocTagsInclGenerated(IIpsObjectPartContainer element, StandardBuilderSet builderSet) {
        List<String> javaDocTags = getJavaDocTags(element, builderSet);
        javaDocTags.add(JavaSourceFileBuilder.ANNOTATION_GENERATED);
        return javaDocTags.toArray(new String[javaDocTags.size()]);
    }

}
