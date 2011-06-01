/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.refactor;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.intParam;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class PullUpRefactoringParticipantTest extends RefactoringParticipantTest {

    @Test
    public void testPullUpPolicyCmptTypeAttribute() throws CoreException {
        // Create target policy component type configured by a product component type
        IPolicyCmptType targetPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "TargetPolicy");
        IProductCmptType targetProductCmptType = newProductCmptType(ipsProject, "TargetProduct");
        targetPolicyCmptType.setConfigurableByProductCmptType(true);
        targetPolicyCmptType.setProductCmptType(targetProductCmptType.getQualifiedName());
        targetProductCmptType.setConfigurationForPolicyCmptType(true);
        targetProductCmptType.setPolicyCmptType(targetPolicyCmptType.getQualifiedName());

        // Create source policy component type, also configured by a product component type
        IPolicyCmptType sourcePolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "SourcePolicy");
        IProductCmptType sourceProductCmptType = newProductCmptType(ipsProject, "SourceProduct");
        sourcePolicyCmptType.setSupertype(targetPolicyCmptType.getQualifiedName());
        sourceProductCmptType.setSupertype(targetProductCmptType.getQualifiedName());
        sourcePolicyCmptType.setConfigurableByProductCmptType(true);
        sourcePolicyCmptType.setProductCmptType(sourceProductCmptType.getQualifiedName());
        sourceProductCmptType.setConfigurationForPolicyCmptType(true);
        sourceProductCmptType.setPolicyCmptType(sourcePolicyCmptType.getQualifiedName());

        // Create the policy component type attribute to pull up
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = createPolicyCmptTypeAttribute("foo", sourcePolicyCmptType);

        // Save source files and build Java source code
        saveIpsSrcFile(targetPolicyCmptType);
        saveIpsSrcFile(targetProductCmptType);
        saveIpsSrcFile(sourcePolicyCmptType);
        saveIpsSrcFile(sourceProductCmptType);
        performFullBuild(ipsProject);

        // Perform the refactoring
        performPullUpRefactoring(policyCmptTypeAttribute, targetPolicyCmptType);

        // Check whether the Java source code was modified correctly
        PolicyCmptTypeAttributeExpectations expectations = new PolicyCmptTypeAttributeExpectations(
                policyCmptTypeAttribute, targetPolicyCmptType, targetProductCmptType);
        expectations.check(sourcePolicyCmptType, sourceProductCmptType, intParam());
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(String name, IPolicyCmptType policyCmptType) {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(name);
        policyCmptTypeAttribute.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setProductRelevant(true);
        policyCmptTypeAttribute.setDefaultValue("0");
        return policyCmptTypeAttribute;
    }

}
