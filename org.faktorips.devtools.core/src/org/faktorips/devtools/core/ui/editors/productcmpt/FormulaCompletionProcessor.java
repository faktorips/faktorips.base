package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ParameterIdentifierResolver;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.ArgumentCheck;


/**
 * 
 */
public class FormulaCompletionProcessor extends AbstractCompletionProcessor {

    private ExprCompiler compiler;
    private ParameterIdentifierResolver identifierResolver;

    public FormulaCompletionProcessor(IIpsProject project, ExprCompiler compiler) {
        super();
        ArgumentCheck.notNull(project);
        setIpsProject(project);
        ArgumentCheck.notNull(compiler);
        this.compiler = compiler;
        ArgumentCheck.isInstanceOf(compiler.getIdentifierResolver(), ParameterIdentifierResolver.class);
        setComputeProposalForEmptyPrefix(true);
        identifierResolver = (ParameterIdentifierResolver)compiler.getIdentifierResolver();
    }

    /**
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] {'.'};
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.AbstractCompletionProcessor#doComputeCompletionProposals(java.lang.String, java.util.List)
     */
    protected void doComputeCompletionProposals(
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
        } else {
            int replacementOffset = prefix.length() - identifier.length();
            addMatchingParameters(result, identifier, replacementOffset);
            addMatchingFunctions(result, identifier, replacementOffset);
        }
    }
    
    private void addMatchingParameters(List result, String prefix, int replacementOffset) {
        Parameter[] params = identifierResolver.getParameters();
        for (int i=0; i<params.length; i++) {
            if (params[i].getName().startsWith(prefix)) {
                addParamToResult(result, params[i], replacementOffset, prefix.length());
            }
        }
    }
    
    private void addParamToResult(List result, Parameter param, int replacementOffset, int replacementLength) {
        String name = param.getName();
        String displayText = name + " - " + param.getDatatype(); //$NON-NLS-1$
        CompletionProposal proposal = new CompletionProposal(
                name, replacementOffset, replacementLength, replacementOffset + name.length(),  
                new DefaultLabelProvider().getImage(param), displayText, null, null);
        result.add(proposal);
    }
    
    private void addMatchingFunctions(List result, String prefix, int replacementOffset) {
        FlFunction[] functions = compiler.getFunctions();
        Arrays.sort(functions, new Comparator() {

            public int compare(Object o1, Object o2) {
                FlFunction f1 = (FlFunction)o1;
                FlFunction f2 = (FlFunction)o2;
                return f1.getName().compareTo(f2.getName());
            }
            
        });
        for (int i=0; i<functions.length; i++) {
            if (functions[i].getName().startsWith(prefix)) {
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
        
        Parameter param = getParameter(paramName);
        if (param==null) {
            return;
        }
        IPolicyCmptType pcType = ipsProject.findPolicyCmptType(param.getDatatype());
        if (pcType==null) {
            return;
        }
        ITypeHierarchy hierarchy = pcType.getSupertypeHierarchy();
        IAttribute[] attributes = hierarchy.getAllAttributes(pcType);
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
                name, replacementOffset, replacementLength, replacementOffset + name.length(),  
                attribute.getImage(), displayText, null, attribute.getDescription());
        result.add(proposal);
    }
    
    private Parameter getParameter(String name) {
        Parameter[] params = identifierResolver.getParameters();
        for (int i=0; i<params.length; i++) {
            if (params[i].getName().equals(name)) {
                return params[i];
            }
        }
        return null;
    }

    private String getLastIdentifier(String s) {
        if (StringUtils.isEmpty(s)) {
            return ""; //$NON-NLS-1$
        }
        int i=s.length()-1;
        while (i>=0) {
            if (!Character.isLetterOrDigit(s.charAt(i)) && s.charAt(i)!='.') {
               break; 
            }
            i--;
        }
        return s.substring(i+1);
    }
}
