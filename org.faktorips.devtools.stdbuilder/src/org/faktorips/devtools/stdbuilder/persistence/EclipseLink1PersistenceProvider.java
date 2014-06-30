/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.IPersistenceProvider;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.util.StringUtil;

/**
 * Persistence provider for EclipseLink 1.1
 * 
 * @author Joerg Ortmann
 */
public class EclipseLink1PersistenceProvider implements IPersistenceProvider {

    public static final String ID_ECLIPSE_LINK_1_1 = "EclipseLink 1.1"; //$NON-NLS-1$

    // orphanRemoval annotation constants
    private static final String IMPORT_PRIVATE_OWNED = "org.eclipse.persistence.annotations.PrivateOwned"; //$NON-NLS-1$
    private static final String ANNOTATION_PRIVATE_OWNED = "@PrivateOwned"; //$NON-NLS-1$

    // converter annotation constants
    private static final String IMPORT_CONVERTER = "org.eclipse.persistence.annotations.Converter"; //$NON-NLS-1$
    private static final String IMPORT_CONVERT = "org.eclipse.persistence.annotations.Convert"; //$NON-NLS-1$
    private static final String ANNOTATION_CONVERTER = "@Converter"; //$NON-NLS-1$
    private static final String ANNOTATION_CONVERT = "@Convert"; //$NON-NLS-1$

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
        javaCodeFragment.addImport(IMPORT_PRIVATE_OWNED);
        javaCodeFragment.appendln(ANNOTATION_PRIVATE_OWNED);
    }

    @Override
    public String getRelationshipAnnotationAttributeOrphanRemoval() {
        // no attribute needed
        return null;
    }

    @Override
    public JavaCodeFragment getConverterAnnotations(IPersistentAttributeInfo persistentAttributeInfo) {
        JavaCodeFragment javaCodeFragment = new JavaCodeFragment();
        javaCodeFragment.addImport(IMPORT_CONVERTER);
        javaCodeFragment.addImport(IMPORT_CONVERT);

        String converterName = StringUtil.unqualifiedName(persistentAttributeInfo.getConverterQualifiedClassName());
        javaCodeFragment.append(ANNOTATION_CONVERTER);
        javaCodeFragment.append("(name="); //$NON-NLS-1$
        javaCodeFragment.appendQuoted(converterName);
        javaCodeFragment.append(", converterClass="); //$NON-NLS-1$
        javaCodeFragment.appendClassName(persistentAttributeInfo.getConverterQualifiedClassName());
        javaCodeFragment.append(".class"); //$NON-NLS-1$
        javaCodeFragment.append(")"); //$NON-NLS-1$
        javaCodeFragment.append(ANNOTATION_CONVERT);
        javaCodeFragment.append("("); //$NON-NLS-1$
        javaCodeFragment.appendQuoted(converterName);
        javaCodeFragment.appendln(")"); //$NON-NLS-1$
        return javaCodeFragment;
    }

    @Override
    public boolean isSupportingIndex() {
        return false;
    }

    @Override
    public JavaCodeFragment getIndexAnnotations(IPersistentAttributeInfo persistentAttributeInfo) {
        throw new UnsupportedOperationException();
    }
}
