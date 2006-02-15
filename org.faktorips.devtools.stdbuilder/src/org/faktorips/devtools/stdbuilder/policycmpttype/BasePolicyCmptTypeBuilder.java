package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractPcTypeBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class BasePolicyCmptTypeBuilder extends AbstractPcTypeBuilder {

    public BasePolicyCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet stringsSet) {
        super(builderSet, kindId, stringsSet);
    }
    
    protected String getEffectiveDateMethodName() {
        return "getEffectiveFrom";
    }

    protected void generateCodeForAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        AttributeType type = attribute.getAttributeType();
        if (type == AttributeType.CHANGEABLE) {
            generateCodeForChangeableAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.CONSTANT) {
            generateCodeForConstantAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.DERIVED) {
            generateCodeForDerivedAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else if (type == AttributeType.COMPUTED) {
            generateCodeForComputedAttribute(attribute, datatypeHelper, memberVarsBuilder, methodsBuilder);
        } else {
            throw new RuntimeException("Unkown attribute type " + type);
        }
    }
    
    protected abstract void generateCodeForConstantAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
     
    protected abstract void generateCodeForChangeableAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
        
    
    protected abstract void generateCodeForDerivedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    protected abstract void generateCodeForComputedAttribute(IAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException;
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForRelation(IRelation relation,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelations(IRelation containerRelation,
            IRelation[] subRelations,
            JavaCodeFragmentBuilder memberVarsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // TODO Auto-generated method stub

    }

}
