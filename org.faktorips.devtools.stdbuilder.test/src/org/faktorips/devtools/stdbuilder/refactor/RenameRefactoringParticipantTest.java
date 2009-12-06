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

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.refactor.IIpsRefactorings;
import org.faktorips.devtools.core.refactor.IpsRefactoringContribution;
import org.faktorips.devtools.core.refactor.RenameIpsElementDescriptor;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.runtime.IValidationContext;

public class RenameRefactoringParticipantTest extends AbstractStdBuilderTest {

    private static final String POLICY_TYPE_NAME = "Policy";

    private static final String POLICY_ATTRIBUTE_NAME = "attribute";

    private static final String PRODUCT_TYPE_NAME = "Product";

    private IPolicyCmptType policyCmptType;

    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private IFolder modelFolder;

    private IFolder internalFolder;

    private IType policyClass;

    private IType policyInterface;

    private IType productGenClass;

    private IType productGenInterface;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        policyCmptType = newPolicyCmptType(ipsProject, POLICY_TYPE_NAME);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(POLICY_ATTRIBUTE_NAME);
        policyCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptTypeAttribute.setProductRelevant(true);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);

        IProductCmptType productCmptType = newProductCmptType(ipsProject, PRODUCT_TYPE_NAME);
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());

        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());

        modelFolder = ipsProject.getProject().getFolder(Path.fromOSString("src/org/faktorips/sample/model"));
        internalFolder = modelFolder.getFolder("internal");
        policyInterface = getJavaType(POLICY_TYPE_NAME, false);
        policyClass = getJavaType(POLICY_TYPE_NAME, true);
        productGenInterface = getJavaType(PRODUCT_TYPE_NAME + "Gen", false);
        productGenClass = getJavaType(PRODUCT_TYPE_NAME + "Gen", true);
    }

    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        // Expect Java elements for published interface.
        assertTrue(policyInterface.getField("PROPERTY_ATTRIBUTE").exists());
        assertTrue(policyInterface.getMethod("getAttribute", new String[] {}).exists());
        assertTrue(policyInterface.getMethod("setAttribute", new String[] { "QString;" }).exists());
        assertTrue(productGenInterface.getMethod("getDefaultValueAttribute", new String[] {}).exists());
        assertTrue(productGenInterface.getMethod("getSetOfAllowedValuesForAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect Java elements for implementation.
        assertTrue(policyClass.getField("attribute").exists());
        assertTrue(policyClass.getMethod("getAttribute", new String[] {}).exists());
        assertTrue(policyClass.getMethod("setAttribute", new String[] { "QString;" }).exists());
        assertTrue(productGenClass.getField("defaultValueAttribute").exists());
        assertTrue(productGenClass.getField("setOfAllowedValuesAttribute").exists());
        assertTrue(productGenClass.getMethod("getDefaultValueAttribute", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("getSetOfAllowedValuesForAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Refactor the attribute.
        renamePolicyCmptTypeAttribute("test");

        // The former Java elements must no longer exist.
        assertFalse(policyInterface.getField("PROPERTY_ATTRIBUTE").exists());
        assertFalse(policyInterface.getMethod("getAttribute", new String[] {}).exists());
        assertFalse(policyInterface.getMethod("setAttribute", new String[] { "QString;" }).exists());
        assertFalse(productGenInterface.getMethod("getDefaultValueAttribute", new String[] {}).exists());
        assertFalse(productGenInterface.getMethod("getSetOfAllowedValuesForAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        assertFalse(policyClass.getField("attribute").exists());
        assertFalse(policyClass.getMethod("getAttribute", new String[] {}).exists());
        assertFalse(policyClass.getMethod("setAttribute", new String[] { "QString;" }).exists());
        assertFalse(productGenClass.getField("defaultValueAttribute").exists());
        assertFalse(productGenClass.getField("setOfAllowedValuesAttribute").exists());
        assertFalse(productGenClass.getMethod("getDefaultValueAttribute", new String[] {}).exists());
        assertFalse(productGenClass.getMethod("getSetOfAllowedValuesForAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for published interface.
        assertTrue(policyInterface.getField("PROPERTY_TEST").exists());
        assertTrue(policyInterface.getMethod("getTest", new String[] {}).exists());
        assertTrue(policyInterface.getMethod("setTest", new String[] { "QString;" }).exists());
        assertTrue(productGenInterface.getMethod("getDefaultValueTest", new String[] {}).exists());
        assertTrue(productGenInterface.getMethod("getSetOfAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for implementation.
        assertTrue(policyClass.getField("test").exists());
        assertTrue(policyClass.getMethod("getTest", new String[] {}).exists());
        assertTrue(policyClass.getMethod("setTest", new String[] { "QString;" }).exists());
        assertTrue(productGenClass.getField("defaultValueTest").exists());
        assertTrue(productGenClass.getField("setOfAllowedValuesTest").exists());
        assertTrue(productGenClass.getMethod("getDefaultValueTest", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("getSetOfAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());
    }

    private IType getJavaType(String typeName, boolean internal) {
        IFolder folder = internal ? internalFolder : modelFolder;
        String interfaceSeparator = internal ? "" : "I";
        return ((ICompilationUnit)JavaCore.create(folder.getFile(interfaceSeparator + typeName + ".java")))
                .getType(interfaceSeparator + typeName);
    }

    private void renamePolicyCmptTypeAttribute(String newName) throws CoreException {
        IpsRefactoringContribution contribution = (IpsRefactoringContribution)RefactoringCore
                .getRefactoringContribution(IIpsRefactorings.RENAME_POLICY_CMPT_TYPE_ATTRIBUTE);
        RenameIpsElementDescriptor renameDescriptor = (RenameIpsElementDescriptor)contribution.createDescriptor();
        renameDescriptor.setProject(ipsProject.getName());
        renameDescriptor.setTypeArgument(policyCmptType);
        renameDescriptor.setPartArgument(policyCmptTypeAttribute);
        ProcessorBasedRefactoring renameRefactoring = (ProcessorBasedRefactoring)renameDescriptor
                .createRefactoring(new RefactoringStatus());
        RenameRefactoringProcessor processor = (RenameRefactoringProcessor)renameRefactoring.getProcessor();
        processor.setNewElementName(newName);
        PerformRefactoringOperation operation = new PerformRefactoringOperation(renameRefactoring,
                CheckConditionsOperation.ALL_CONDITIONS);
        ResourcesPlugin.getWorkspace().run(operation, new NullProgressMonitor());
    }
}
