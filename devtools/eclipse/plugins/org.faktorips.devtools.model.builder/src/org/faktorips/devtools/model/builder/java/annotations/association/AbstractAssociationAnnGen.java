/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.association;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.XAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsDerivedUnion;
import org.faktorips.runtime.model.annotation.IpsMatchingAssociation;
import org.faktorips.runtime.model.annotation.IpsSubsetOfDerivedUnion;
import org.faktorips.runtime.model.type.AssociationKind;

public abstract class AbstractAssociationAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        if (!(modelNode instanceof XAssociation association)) {
            return new JavaCodeFragment();
        } else {
            return new JavaCodeFragmentBuilder().append(createAnnAssociation(association))
                    .append(createAnnDerivedUnion(association)).append(createAnnSubsetOfDerivedUnion(association))
                    .append(createAnnMatchingAssociation(association)).getFragment();
        }
    }

    @Override
    public abstract boolean isGenerateAnnotationFor(AbstractGeneratorModelNode ipsElement);

    /**
     * @return an annotation containing runtime information of this association
     * @see IpsAssociation
     */
    protected JavaCodeFragment createAnnAssociation(XAssociation association) {
        JavaCodeFragmentBuilder paramsBuilder = new JavaCodeFragmentBuilder().append("name = \""
                + association.getName(false) + "\"" + ", ");

        paramsBuilder.append("pluralName = \"" + association.getName(true) + "\"" + ", ");
        paramsBuilder.append("kind = ").appendClassName(AssociationKind.class)
                .append("." + association.getAssociationKind() + ", ");
        paramsBuilder.append("targetClass = ").appendClassName(association.getTargetQualifiedClassName())
                .append(".class, ");

        paramsBuilder.append("min = " + association.getMinCardinality() + ", ");
        if (association.getMaxCardinality() == Integer.MAX_VALUE) {
            paramsBuilder.append("max = ").append(association.addImport(Integer.class)).append(".MAX_VALUE");
        } else {
            paramsBuilder.append("max = " + association.getMaxCardinality());
        }
        if (association.isQualified()) {
            paramsBuilder.append(", qualified = true");
        }
        return new JavaCodeFragmentBuilder().annotationLn(IpsAssociation.class, paramsBuilder.getFragment())
                .getFragment();
    }

    /**
     * @return an annotation if the modelNode is a derived union, null if not
     * @see IpsDerivedUnion
     */
    protected JavaCodeFragment createAnnDerivedUnion(XAssociation association) {
        if (association.isDerivedUnion()) {
            return new JavaCodeFragmentBuilder().annotationLn(IpsDerivedUnion.class).getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

    /**
     * @return an annotation that annotates the name of the derived Union if the modelNode is a
     *             subset of a derived union. Else null.
     * @see IpsSubsetOfDerivedUnion
     */
    protected JavaCodeFragment createAnnSubsetOfDerivedUnion(XAssociation association) {
        if (association.isSubsetOfADerivedUnion()) {
            String derivedUnion = association.getSubsettedDerivedUnion().getName(false);
            return new JavaCodeFragmentBuilder()
                    .annotationLn(IpsSubsetOfDerivedUnion.class, "\"" + derivedUnion + "\"").getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

    /**
     * @return an annotation that annotates the matching policy/product association. Null if the
     *             association has no matching association.
     * @see IpsMatchingAssociation
     */
    protected JavaCodeFragment createAnnMatchingAssociation(XAssociation association) {
        XAssociation matchingAssociation = association.getMatchingAssociation();
        if (matchingAssociation == null) {
            return new JavaCodeFragment();
        } else {
            JavaCodeFragmentBuilder fragmentBuilder = new JavaCodeFragmentBuilder()
                    .append("source = ")
                    .appendClassName(
                            matchingAssociation.getSourceModelNodeNotConsiderChangingOverTime()
                                    .getPublishedInterfaceName())
                    .append(".class, ")
                    .append("name = \"" + matchingAssociation.getName(false) + "\"");

            return new JavaCodeFragmentBuilder().annotationLn(IpsMatchingAssociation.class,
                    fragmentBuilder.getFragment()).getFragment();
        }
    }
}
