/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This class is an assembly of methods common to Java builders and generators.
 * 
 * @author Peter Kuntz
 */
public final class JavaGeneratorHelper {

    private JavaGeneratorHelper() {
        // Helper class not to be instantiated.
    }

    /**
     * Adds an <code>Override</code> annotation to the java code fragment if the java compliance
     * level is greater than 1.5. It takes into account the fine differences regarding the
     * <code>Override</code> annotation for compliance level 1.5 and higher.
     * 
     * @param fragmentBuilder the annotation is added to this {@link JavaCodeFragmentBuilder}
     * @param iIpsProject the {@link IIpsProject} from which the {@link IJavaProject} is requested
     *            and checked regarding its compliance level
     * @param interfaceMethodImplementation to be able to decide if an Override annotation needs to
     *            be generated it must be known if the the generated method is an implementation of
     *            an interface method or an override of a super class method.
     */
    public final static void appendOverrideAnnotation(JavaCodeFragmentBuilder fragmentBuilder,
            IIpsProject iIpsProject,
            boolean interfaceMethodImplementation) {

        if (ComplianceCheck.isComplianceLevel5(iIpsProject) && !interfaceMethodImplementation) {
            fragmentBuilder.annotationLn(JavaSourceFileBuilder.ANNOTATION_OVERRIDE);
            return;
        }
        if (ComplianceCheck.isComplianceLevelGreaterJava5(iIpsProject)) {
            fragmentBuilder.annotationLn(JavaSourceFileBuilder.ANNOTATION_OVERRIDE);
        }
    }

    /**
     * Appends the list of classNames as a list of generics to the given fragmentBuilder if
     * compliance level is at least Java5. e.g. if your classNames is [Integer, String], the code
     * 
     * <pre>
     * <Integer, String>
     * </pre>
     * 
     * is added to the fragment builder.
     */
    public final static void appendGenerics(JavaCodeFragmentBuilder fragmentBuilder,
            IIpsProject ipsProject,
            String... classNames) {

        if (ComplianceCheck.isComplianceLevelAtLeast5(ipsProject)) {
            fragmentBuilder.appendGenerics(classNames);
        }
    }

    /**
     * Appends the list of classes as a list of generics to the given fragmentBuilder if compliance
     * level is at least Java5. e.g. if your classes are [Integer.class, String.class], the code
     * 
     * <pre>
     * <Integer, String>
     * </pre>
     * 
     * is added to the fragment builder.
     */
    public final static void appendGenerics(JavaCodeFragmentBuilder fragmentBuilder,
            IIpsProject ipsProject,
            Class<?>... classes) {
        if (ComplianceCheck.isComplianceLevelAtLeast5(ipsProject)) {
            fragmentBuilder.appendGenerics(classes);
        }
    }

    /**
     * Returns the default java doc comment for overridden methods.
     */
    public final static String getJavaDocCommentForOverriddenMethod() {
        return "{@inheritDoc}"; //$NON-NLS-1$
    }

}
