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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.ArgumentCheck;


/**
 * 
 */
public class FormulaCompletionProcessor extends AbstractCompletionProcessor {

    private IMethod signature;
    private IFormula formula;
    
    public FormulaCompletionProcessor(IFormula formula) throws CoreException {
        super();
        ArgumentCheck.notNull(formula);
        this.formula = formula;
        setIpsProject(formula.getIpsProject());
        this.signature = formula.findFormulaSignature(formula.getIpsProject());
        setComputeProposalForEmptyPrefix(true);
    }

    /**
     * {@inheritDoc}
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] {'.'};
    }
    
    /** 
     * {@inheritDoc}
     */
    public void doComputeCompletionProposals(
            String prefix, 
            int documentOffset, 
            List result) throws Exception {
        
        String identifier = getLastIdentifier(prefix);
        int pos = identifier.lastIndexOf('.');
        if (pos>0) {
            String paramName = identifier.substring(0, pos);
            String attributePrefix = identifier.substring(pos+1);
            int replacementOffset = prefix.length() - attributePrefix.length();
            addMatchingAttributes(result, paramName, attributePrefix, replacementOffset);
            addMatchingEnumValues(result, paramName, attributePrefix, replacementOffset);
        } else {
            int replacementOffset = prefix.length() - identifier.length();
            addMatchingProductCmptTypeAttributes(result, identifier, replacementOffset);
            addMatchingParameters(result, identifier, replacementOffset);
            addMatchingFunctions(result, identifier, replacementOffset);
            addMatchingEnumTypes(result, identifier, replacementOffset);
        }
    }
    
    private void addMatchingEnumTypes(List result, String enumTypePrefix, int replacementOffset) throws CoreException{
    	EnumDatatype[] enumTypes = formula.getEnumDatatypesAllowedInFormula();
    	for (int i = 0; i < enumTypes.length; i++) {
			if(enumTypes[i].getName().startsWith(enumTypePrefix)){
		        CompletionProposal proposal = new CompletionProposal(
		        		enumTypes[i].getName(), replacementOffset, enumTypePrefix.length(), enumTypes[i].getName().length(),  
		                new DefaultLabelProvider().getImage(enumTypes[i]), enumTypes[i].getName(), null, null);
		        result.add(proposal);
			}
		}
    }
    
    private void addMatchingEnumValues(List result, String enumTypeName, String enumValuePrefix, int replacementOffset) throws CoreException{
		EnumDatatype[] enumTypes = formula.getEnumDatatypesAllowedInFormula();
		for (int i = 0; i < enumTypes.length; i++) {
			if(enumTypes[i].getName().equals(enumTypeName)){
				String[] valueIds = enumTypes[i].getAllValueIds(false);
				for (int t = 0; t < valueIds.length; t++) {
				    String valueId = valueIds[t];
				    if(enumTypes[i].isSupportingNames()){
				        valueId = enumTypes[i].getValueName(valueIds[t]);
				    }
		    		if(valueId.startsWith(enumValuePrefix)){
		    			addEnumValueToResult(result, enumTypes[i], valueId, replacementOffset, enumValuePrefix.length());
		    		}
				}
				return;
			}
		}
    }
    
    private void addEnumValueToResult(List result, EnumDatatype enumType, String enumValue, int replacementOffset, int replacementLength){
        CompletionProposal proposal = new CompletionProposal(
                enumValue, replacementOffset, replacementLength, enumValue.length(),  
                new DefaultLabelProvider().getImage(enumType), enumValue, null, null);
        result.add(proposal);
    }
    
    private void addMatchingProductCmptTypeAttributes(List result, String prefix, int replacementOffset) throws CoreException{
        IIpsProject ipsProject = formula.getIpsProject();
        IProductCmptType productCmptType = formula.findProductCmptType(formula.getIpsProject());
        if(productCmptType != null){
            IAttribute[] attributes = productCmptType.findAllAttributes(ipsProject);
            for (int i = 0; i < attributes.length; i++) {
                if(attributes[i].getName().startsWith(prefix)){
                    addPartToResult(result, attributes[i], attributes[i].getDatatype(), replacementOffset, prefix.length());
                }
            }
        }
    }

    private void addPartToResult(List result, IIpsObjectPart part, String datatype, int replacementOffset, int replacementLength) {
        String name = part.getName();
        String displayText = name + " - " + datatype; //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(
                name, replacementOffset, replacementLength, replacementOffset + name.length(),  
                new DefaultLabelProvider().getImage(part), displayText, null, null);
        result.add(proposal);
    }
    
    private void addMatchingParameters(List result, String prefix, int replacementOffset) {
        IParameter[] params = signature.getParameters();
        for (int i=0; i<params.length; i++) {
            if (params[i].getName().startsWith(prefix)) {
                addPartToResult(result, params[i], params[i].getDatatype(), replacementOffset, prefix.length());
            }
        }
    }
    
    private void addMatchingFunctions(List result, String prefix, int replacementOffset) throws CoreException {
        ExprCompiler compiler = formula.newExprCompiler(formula.getIpsProject());
        FlFunction[] functions = compiler.getFunctions();
        Arrays.sort(functions, new Comparator() {

            public int compare(Object o1, Object o2) {
                FlFunction f1 = (FlFunction)o1;
                FlFunction f2 = (FlFunction)o2;
                return f1.getName().compareTo(f2.getName());
            }
        });
        for (int i=0; i<functions.length; i++) {
            if (functions[i].getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                addFunctionToResult(result, functions[i], replacementOffset, prefix.length());
            }
        }
    }
    
    private void addFunctionToResult(List result, FlFunction function, int replacementOffset, int replacementLength) {
        String name = function.getName();
        StringBuffer displayText = new StringBuffer(name);
        displayText.append('(');
        Datatype[] argTypes = function.getArgTypes();
        for (int i=0; i<argTypes.length; i++) {
            if (i>0) {
                displayText.append(", "); //$NON-NLS-1$
            }
            displayText.append(argTypes[i].getName());
        }
        displayText.append(')');
        displayText.append(" - "); //$NON-NLS-1$
        displayText.append(function.getType().getName());
        String description = function.getDescription();
        description = description.replaceAll("\r\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        description = description.replaceAll("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        CompletionProposal proposal = new CompletionProposal(
                name, replacementOffset, replacementLength, replacementOffset + name.length(),  
                new DefaultLabelProvider().getImage(function), displayText.toString(), null, description);
        result.add(proposal);
    }
    
    private void addMatchingAttributes(
            List result, 
            String paramName, 
            String attributePrefix, 
            int replacementOffset) throws CoreException {
        
        IParameter param = getParameter(paramName);
        if (param==null) {
            return;
        }
        Datatype datatype = param.findDatatype(ipsProject);
        if(!(datatype instanceof IType)){
            return;
        }
        IAttribute[] attributes = ((IType)datatype).findAllAttributes(ipsProject);
        List attributeNames = new ArrayList();
        for (int i=0; i<attributes.length; i++) {
            if (attributes[i].getName().startsWith(attributePrefix)) {
                if (!attributeNames.contains(attributes[i].getName())) {
                    addAttributeToResult(result, paramName, attributes[i], replacementOffset, attributePrefix.length());
                    attributeNames.add(attributes[i].getName());
                }
            }
        }
    }
    
    private void addAttributeToResult(
            List result, 
            String paramName, 
            IAttribute attribute, 
            int replacementOffset,
            int replacementLength) {
        String name = attribute.getName();
        String displayText = name + " - " + attribute.getDatatype(); //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(
                name, replacementOffset, replacementLength, name.length(),  
                attribute.getImage(), displayText, null, attribute.getDescription());
        result.add(proposal);
    }
    
    private IParameter getParameter(String name) {
        IParameter[] params = signature.getParameters();
        for (int i=0; i<params.length; i++) {
            if (params[i].getName().equals(name)) {
                return params[i];
            }
        }
        return null;
    }

    /*
     * The characters that are checked within this method have to be in sych with the identifier tokens defined
     * in the ffl.jjt grammar
     */ 
    private String getLastIdentifier(String s) {
        if (StringUtils.isEmpty(s)) {
            return ""; //$NON-NLS-1$
        }
        int i=s.length()-1;
        while (i>=0) {
            if (!Character.isLetterOrDigit(s.charAt(i)) && s.charAt(i)!='.' && s.charAt(i)!='_' && s.charAt(i)!='-') {
               break; 
            }
            i--;
        }
        return s.substring(i+1);
    }
}
