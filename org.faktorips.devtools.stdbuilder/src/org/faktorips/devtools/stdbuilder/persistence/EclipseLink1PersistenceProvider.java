/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.IPersistenceProvider;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypePartInfo;
import org.faktorips.util.StringUtil;

/**
 * Persistence provider for EclipseLink 1.1
 */
public class EclipseLink1PersistenceProvider implements IPersistenceProvider {

    public static final String ID_ECLIPSE_LINK_1_1 = "EclipseLink 1.1"; //$NON-NLS-1$

    // orphanRemoval annotation constants
    private static final String ANNOTAITON_PRIVATE_OWNED = "org.eclipse.persistence.annotations.PrivateOwned"; //$NON-NLS-1$

    // converter annotation constants
    private static final String ANNOTATION_CONVERTER = "org.eclipse.persistence.annotations.Converter"; //$NON-NLS-1$
    private static final String ANNOTATION_CONVERT = "org.eclipse.persistence.annotations.Convert"; //$NON-NLS-1$

    @Override
    public boolean isSupportingConverters() {
        return true;
    }

    @Override
    public boolean isSupportingOrphanRemoval() {
        return true;
    }

    @Override
    public void addAnnotationOrphanRemoval(JavaCodeFragment javaCodeFragment) {
        javaCodeFragment.append(new JavaCodeFragmentBuilder().annotationLn(ANNOTAITON_PRIVATE_OWNED).getFragment());
    }

    @Override
    public String getRelationshipAnnotationAttributeOrphanRemoval() {
        // no attribute needed
        return null;
    }

    @Override
    public JavaCodeFragment getConverterAnnotations(IPersistentAttributeInfo persistentAttributeInfo) {
        String converterName = StringUtil.unqualifiedName(persistentAttributeInfo.getConverterQualifiedClassName());

        JavaCodeFragmentBuilder fragmentBuilder = new JavaCodeFragmentBuilder();

        JavaCodeFragment converterParams = new JavaCodeFragment();
        converterParams.append("name="); //$NON-NLS-1$
        converterParams.appendQuoted(converterName);
        converterParams.append(", converterClass="); //$NON-NLS-1$
        converterParams.appendClassName(persistentAttributeInfo.getConverterQualifiedClassName());
        converterParams.append(".class"); //$NON-NLS-1$
        fragmentBuilder.annotationLn(ANNOTATION_CONVERTER, converterParams);

        fragmentBuilder.annotationLn(ANNOTATION_CONVERT, '"' + converterName + '"');

        return fragmentBuilder.getFragment();
    }

    @Override
    public boolean isSupportingIndex() {
        return false;
    }

    @Override
    public JavaCodeFragment getIndexAnnotations(IPersistentTypePartInfo persistentAttributeInfo) {
        throw new UnsupportedOperationException();
    }
}
