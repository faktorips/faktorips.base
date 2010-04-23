/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.util.StringUtil;

/**
 * Persistence provider for EclipseLink 1.1
 * 
 * @author Joerg Ortmann
 */
public class EclipseLink1PersistenceProvider implements IPersistenceProvider {
    // orphanRemoval annotation constants
    private static final String IMPORT_PRIVATE_OWNED = "org.eclipse.persistence.annotations.PrivateOwned"; //$NON-NLS-1$
    private static final String ANNOTATION_PRIVATE_OWNED = "@PrivateOwned"; //$NON-NLS-1$

    // converter annotation constants
    private static final String IMPORT_CONVERTER = "org.eclipse.persistence.annotations.Converter"; //$NON-NLS-1$
    private static final String IMPORT_CONVERT = "org.eclipse.persistence.annotations.Convert"; //$NON-NLS-1$
    private static final String ANNOTATION_CONVERTER = "@Converter"; //$NON-NLS-1$
    private static final String ANNOTATION_CONVERT = "@Convert"; //$NON-NLS-1$

    public boolean isSupportingConverters() {
        return true;
    }

    public boolean isSupportingOrphanRemoval() {
        return true;
    }

    public void addAnnotationOrphanRemoval(JavaCodeFragment javaCodeFragment) {
        javaCodeFragment.addImport(IMPORT_PRIVATE_OWNED);
        javaCodeFragment.appendln(ANNOTATION_PRIVATE_OWNED);
    }

    public String getRelationshipAnnotationAttributeOrphanRemoval() {
        // no attribute needed
        return null;
    }

    public void addAnnotationConverter(JavaCodeFragment javaCodeFragment,
            IPersistentAttributeInfo persistentAttributeInfo) {
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
    }
}
