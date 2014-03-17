/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.GenericGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;

/**
 * This class is used to create the java doc tags provided by an {@link IAnnotationGenerator} in old
 * style builder that do not use Xpand/Xtend and the {@link ModelService} yet.
 * <p>
 * This class will be deleted when it is not needed anymore.
 */
public class JavaDocTagGeneratorUtil {

    private JavaDocTagGeneratorUtil() {
        // do not instantiate
    }

    public static List<String> getJavaDocTags(IIpsObjectPartContainer element, StandardBuilderSet builderSet) {
        ArrayList<String> javaDocTags = new ArrayList<String>();
        if (element != null) {
            GenericGeneratorModelNode modelNode = builderSet.getModelNode(element, GenericGeneratorModelNode.class);
            String annotation = modelNode.getAnnotations(AnnotatedJavaElementType.ELEMENT_JAVA_DOC);
            if (StringUtils.isNotEmpty(annotation)) {
                javaDocTags.add(annotation.substring(1));
            }
        }
        return javaDocTags;
    }

    public static String[] getJavaDocTagsInklGenerated(IIpsObjectPartContainer element, StandardBuilderSet builderSet) {
        List<String> javaDocTags = getJavaDocTags(element, builderSet);
        javaDocTags.add(JavaSourceFileBuilder.ANNOTATION_GENERATED[0]);
        return javaDocTags.toArray(new String[javaDocTags.size()]);
    }

}
