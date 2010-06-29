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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
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
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.ArgumentCheck;

public class FormulaCompletionProcessor extends AbstractCompletionProcessor {

    private IMethod signature;
    private IFormula formula;

    public FormulaCompletionProcessor(IFormula formula) throws CoreException {
        super();
        ArgumentCheck.notNull(formula);
        this.formula = formula;
        setIpsProject(formula.getIpsProject());
        signature = formula.findFormulaSignature(formula.getIpsProject());
        setComputeProposalForEmptyPrefix(true);
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '.' };
    }

    @Override
    public void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        String identifier = getLastIdentifier(prefix);
        int pos = identifier.lastIndexOf('.');
        if (pos > 0) {
            String paramName = identifier.substring(0, pos);
            String attributePrefix = identifier.substring(pos + 1);
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

    private void addMatchingEnumTypes(List<ICompletionProposal> result, String enumTypePrefix, int replacementOffset)
            throws CoreException {

        EnumDatatype[] enumTypes = formula.getEnumDatatypesAllowedInFormula();
        for (EnumDatatype enumType : enumTypes) {
            if (enumType.getName().startsWith(enumTypePrefix)) {
                ICompletionProposal proposal = new CompletionProposal(enumType.getName(), replacementOffset,
                        enumTypePrefix.length(), enumType.getName().length(), new DefaultLabelProvider()
                                .getImage(enumType), enumType.getName(), null, null);
                result.add(proposal);
            }
        }
    }

    private void addMatchingEnumValues(List<ICompletionProposal> result,
            String enumTypeName,
            String enumValuePrefix,
            int replacementOffset) throws CoreException {

        EnumDatatype[] enumTypes = formula.getEnumDatatypesAllowedInFormula();
        for (EnumDatatype enumType : enumTypes) {
            if (enumType.getName().equals(enumTypeName)) {
                String[] valueIds = enumType.getAllValueIds(false);
                for (String valueId : valueIds) {
                    if (valueId.startsWith(enumValuePrefix)) {
                        addEnumValueToResult(result, enumType, valueId, replacementOffset, enumValuePrefix.length());
                    }
                }
                return;
            }
        }
    }

    private void addEnumValueToResult(List<ICompletionProposal> result,
            EnumDatatype enumType,
            String enumValue,
            int replacementOffset,
            int replacementLength) {

        ICompletionProposal proposal = new CompletionProposal(enumValue, replacementOffset, replacementLength,
                enumValue.length(), new DefaultLabelProvider().getImage(enumType), enumValue, null, null);
        result.add(proposal);
    }

    private void addMatchingProductCmptTypeAttributes(List<ICompletionProposal> result,
            String prefix,
            int replacementOffset) throws CoreException {

        IIpsProject ipsProject = formula.getIpsProject();
        IProductCmptType productCmptType = formula.findProductCmptType(formula.getIpsProject());
        if (productCmptType != null) {
            IAttribute[] attributes = productCmptType.findAllAttributes(ipsProject);
            for (IAttribute attribute : attributes) {
                if (attribute.getName().startsWith(prefix)) {
                    addPartToResult(result, attribute, attribute.getDatatype(), replacementOffset, prefix.length());
                }
            }
        }
    }

    private void addPartToResult(List<ICompletionProposal> result,
            IIpsObjectPart part,
            String datatype,
            int replacementOffset,
            int replacementLength) {

        String name = part.getName();
        String displayText = name + " - " + datatype; //$NON-NLS-1$
        ICompletionProposal proposal = new CompletionProposal(name, replacementOffset, replacementLength,
                replacementOffset + name.length(), new DefaultLabelProvider().getImage(part), displayText, null, null);
        result.add(proposal);
    }

    private void addMatchingParameters(List<ICompletionProposal> result, String prefix, int replacementOffset) {
        IParameter[] params = signature.getParameters();
        for (IParameter param : params) {
            if (param.getName().startsWith(prefix)) {
                addPartToResult(result, param, param.getDatatype(), replacementOffset, prefix.length());
            }
        }
    }

    private void addMatchingFunctions(List<ICompletionProposal> result, String prefix, int replacementOffset)
            throws CoreException {

        ExprCompiler compiler = formula.newExprCompiler(formula.getIpsProject());
        FlFunction[] functions = compiler.getFunctions();
        Arrays.sort(functions, new Comparator<FlFunction>() {

            @Override
            public int compare(FlFunction o1, FlFunction o2) {
                FlFunction f1 = o1;
                FlFunction f2 = o2;
                return f1.getName().compareTo(f2.getName());
            }
        });
        for (FlFunction function : functions) {
            if (function.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                addFunctionToResult(result, function, replacementOffset, prefix.length());
            }
        }
    }

    private void addFunctionToResult(List<ICompletionProposal> result,
            FlFunction function,
            int replacementOffset,
            int replacementLength) {

        String name = function.getName();
        StringBuffer displayText = new StringBuffer(name);
        displayText.append('(');
        Datatype[] argTypes = function.getArgTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0) {
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
        ICompletionProposal proposal = new CompletionProposal(name, replacementOffset, replacementLength,
                replacementOffset + name.length(), new DefaultLabelProvider().getImage(function), displayText
                        .toString(), null, description);
        result.add(proposal);
    }

    private void addMatchingAttributes(List<ICompletionProposal> result,
            String paramName,
            String attributePrefix,
            int replacementOffset) throws CoreException {

        IParameter param = getParameter(paramName);
        if (param == null) {
            return;
        }
        Datatype datatype = param.findDatatype(ipsProject);
        if (!(datatype instanceof IType)) {
            return;
        }
        IAttribute[] attributes = ((IType)datatype).findAllAttributes(ipsProject);
        List<String> attributeNames = new ArrayList<String>();
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getName().startsWith(attributePrefix)) {
                if (!attributeNames.contains(attributes[i].getName())) {
                    addAttributeToResult(result, attributes[i], replacementOffset, attributePrefix.length());
                    attributeNames.add(attributes[i].getName());
                }
            }
        }
    }

    private void addAttributeToResult(List<ICompletionProposal> result,
            IAttribute attribute,
            int replacementOffset,
            int replacementLength) {

        String name = attribute.getName();
        String displayText = name + " - " + attribute.getDatatype(); //$NON-NLS-1$
        ICompletionProposal proposal = new CompletionProposal(name, replacementOffset, replacementLength,
                name.length(), IpsUIPlugin.getImageHandling().getImage(attribute), displayText, null, attribute
                        .getDescription());
        result.add(proposal);
    }

    private IParameter getParameter(String name) {
        IParameter[] params = signature.getParameters();
        for (IParameter param : params) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }

    /**
     * The characters that are checked within this method have to be in sych with the identifier
     * tokens defined in the ffl.jjt grammar
     */
    private String getLastIdentifier(String s) {
        if (StringUtils.isEmpty(s)) {
            return ""; //$NON-NLS-1$
        }
        int i = s.length() - 1;
        while (i >= 0) {
            if (!Character.isLetterOrDigit(s.charAt(i)) && s.charAt(i) != '.' && s.charAt(i) != '_'
                    && s.charAt(i) != '-') {
                break;
            }
            i--;
        }
        return s.substring(i + 1);
    }
}
