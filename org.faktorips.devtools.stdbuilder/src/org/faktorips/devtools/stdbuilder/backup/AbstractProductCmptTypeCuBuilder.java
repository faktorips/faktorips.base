package org.faktorips.devtools.stdbuilder.backup;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.util.LocalizedStringsSet;

public abstract class AbstractProductCmptTypeCuBuilder extends AbstractPcTypeBuilder {

    public AbstractProductCmptTypeCuBuilder(IJavaPackageStructure packageStructure, String kindId,
            LocalizedStringsSet stringsSet) {
        super(packageStructure, kindId, stringsSet);
    }
    
    /**
     * Overridden.
     */
    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        if (!attribute.isProductRelevant()) {
            return;
        }
        if (attribute.isChangeable()) {
            generateCodeForChangeableAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (attribute.getAttributeType()==AttributeType.CONSTANT) {
            generateCodeForConstantAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (attribute.isDerivedOrComputed()) {
            generateCodeForComputedAndDerivedAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else {
            throw new RuntimeException("Attribute " + attribute +" has an unknown type " + attribute.getAttributeType());
        }
    }
    
    protected abstract void generateCodeForChangeableAttribute(
            IAttribute a, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForConstantAttribute(
            IAttribute a, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;

    protected abstract void generateCodeForComputedAndDerivedAttribute(
            IAttribute a, 
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
}
