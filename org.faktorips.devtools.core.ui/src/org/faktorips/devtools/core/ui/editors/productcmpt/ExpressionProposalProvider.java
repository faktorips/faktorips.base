/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.MultiLanguageSupport;
import org.faktorips.devtools.core.builder.flidentifier.AttributeParser;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.fl.IdentifierKind;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.internal.model.method.Parameter;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.internal.ContentProposal;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FlFunction;
import org.faktorips.util.ArgumentCheck;

/**
 * A {@link IContentProposalProvider} for {@link IExpression}s for use in a
 * {@link ContentProposalAdapter}.
 * 
 * @author schwering
 */
public class ExpressionProposalProvider implements IContentProposalProvider {

    private IBaseMethod signature;
    private IExpression expression;
    private IIpsProject ipsProject;
    private MultiLanguageSupport multiLanguageSupport;

    public ExpressionProposalProvider(IExpression expression) {
        super();
        ArgumentCheck.notNull(expression);
        this.expression = expression;
        this.ipsProject = expression.getIpsProject();
        this.signature = expression.findFormulaSignature(ipsProject);
        multiLanguageSupport = IpsPlugin.getMultiLanguageSupport();
    }

    private IdentifierFilter getIdentifierFilter() {
        return IpsPlugin.getDefault().getIdentifierFilter();
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        String prefix = contents.substring(0, position);
        String identifier = getLastIdentifier(prefix);
        int pos = identifier.lastIndexOf('.');
        List<IContentProposal> result = new LinkedList<IContentProposal>();
        if (pos > 0) {
            String paramName = identifier.substring(0, pos);
            String attributePrefix = identifier.substring(pos + 1);
            addMatchingFunctions(result, identifier);
            addMatchingAttributes(result, paramName, attributePrefix);
            addDefaultValuesToResult(result, paramName, attributePrefix);
            addMatchingEnumValues(result, paramName, attributePrefix);
            if (ipsProject.getReadOnlyProperties().isAssociationsInFormulas()) {
                addMatchingAssociations(result, paramName, attributePrefix);
            }
            addAdditionalProposals(result, getAdditionalProposals(paramName, attributePrefix));
        } else {
            addMatchingParameters(result, identifier);
            addMatchingFunctions(result, identifier);
            addMatchingProductCmptTypeAttributes(result, identifier);
            addMatchingEnumTypes(result, identifier);
            addAdditionalProposals(result, getAdditionalProposals(identifier));
        }
        return result.toArray(new IContentProposal[result.size()]);
    }

    private void addAdditionalProposals(final List<IContentProposal> result,
            final List<IContentProposal> additionalProposals) {
        Collections.sort(additionalProposals, new SortContentProposal());
        for (IContentProposal proposal : additionalProposals) {
            if (!result.contains(proposal)) {
                result.add(proposal);
            }
        }
    }

    private void addMatchingEnumTypes(List<IContentProposal> result, String enumTypePrefix) {
        EnumDatatype[] enumTypes = expression.getEnumDatatypesAllowedInFormula();
        Collections.sort(Arrays.asList(enumTypes));
        for (EnumDatatype enumType : enumTypes) {
            if (checkMatchingNameWithCaseInsensitive(enumType.getName(), enumTypePrefix)) {
                IContentProposal proposal = new ContentProposal(enumType.getName(), enumType.getName(),
                        enumType.getName(), enumTypePrefix);
                result.add(proposal);
            }
        }
    }

    private void addMatchingEnumValues(List<IContentProposal> result, String enumTypeName, String enumValuePrefix) {
        EnumDatatype[] enumTypes = expression.getEnumDatatypesAllowedInFormula();
        Collections.sort(Arrays.asList(enumTypes));
        for (EnumDatatype enumType : enumTypes) {
            if (enumType.getName().equals(enumTypeName)) {
                String[] valueIds = enumType.getAllValueIds(false);
                for (String valueId : valueIds) {
                    if (checkMatchingNameWithCaseInsensitive(valueId, enumValuePrefix)) {
                        String valueName = enumType.getValueName(valueId);
                        addEnumValueToResult(result, valueId, enumValuePrefix, valueName);
                    }
                }
                return;
            }
        }
    }

    private void addEnumValueToResult(List<IContentProposal> result, String enumValue, String prefix, String valueName) {
        IContentProposal proposal = new ContentProposal(enumValue, enumValue + '(' + valueName + ')', null, prefix);
        result.add(proposal);
    }

    private void addMatchingProductCmptTypeAttributes(List<IContentProposal> result, String prefix) {
        List<IAttribute> attributes = expression.findMatchingProductCmptTypeAttributes();
        Collections.sort(attributes, new SortList());
        for (IAttribute attribute : attributes) {
            if (isAttributeIdentifierAllowed(attribute, prefix)) {
                addPartToResult(result, attribute, attribute.getDatatype(), prefix);
            }
        }
    }

    private boolean isAttributeIdentifierAllowed(IAttribute attribute, String prefix) {
        return isAttributeAllowedByFilter(attribute) && isNameMatching(attribute, prefix);
    }

    private boolean isAttributeAllowedByFilter(IAttribute attribute) {
        return getIdentifierFilter().isIdentifierAllowed(attribute, IdentifierKind.ATTRIBUTE);
    }

    private boolean isNameMatching(IAttribute attribute, String prefix) {
        return checkMatchingNameWithCaseInsensitive(attribute.getName(), prefix);
    }

    private void addPartToResult(List<IContentProposal> result, IIpsObjectPart part, String datatype, String prefix) {
        String name = part.getName();
        String displayText = name + " - " + datatype; //$NON-NLS-1$
        String description = null;
        ILabeledElement labeledElement = null;
        IDescribedElement describedElement = null;
        if (part instanceof ILabeledElement) {
            labeledElement = (ILabeledElement)part;
        }
        if (part instanceof IDescribedElement) {
            describedElement = (IDescribedElement)part;
        }
        if (part instanceof Parameter) {
            try {
                Datatype type = ((Parameter)part).findDatatype(ipsProject);
                if (type instanceof ILabeledElement) {
                    labeledElement = (ILabeledElement)type;
                }
                if (type instanceof IDescribedElement) {
                    describedElement = (IDescribedElement)type;
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e.getLocalizedMessage(), e);
            }
        }
        if (labeledElement != null) {
            String localizedLabel = multiLanguageSupport.getLocalizedLabel(labeledElement);
            if (StringUtils.isNotBlank(localizedLabel)) {
                description = localizedLabel;
            }
        }
        if (describedElement != null) {
            String localizedDescription = multiLanguageSupport.getLocalizedDescription(describedElement);
            if (StringUtils.isNotBlank(localizedDescription)) {
                if (StringUtils.isNotBlank(description)) {
                    description = description + " - " + localizedDescription; //$NON-NLS-1$
                } else {
                    description = localizedDescription;
                }
            }
        }
        IContentProposal proposal = new ContentProposal(name, displayText, description, prefix);
        result.add(proposal);
    }

    private void addMatchingParameters(List<IContentProposal> result, String prefix) {
        IParameter[] params = getSignature().getParameters();
        Collections.sort(Arrays.asList(params), new SortList());
        for (IParameter param : params) {
            if (checkMatchingNameWithCaseInsensitive(param.getName(), prefix)) {
                addPartToResult(result, param, param.getDatatype(), prefix);
            }
        }
    }

    /**
     * This method should be used by subclasses that want to provide additional
     * {@link IContentProposal}s.
     * 
     * @param prefix the prefix for the proposals
     * @return additional {@link IContentProposal}s
     */
    protected List<IContentProposal> getAdditionalProposals(final String prefix) {
        return Collections.emptyList();
    }

    /**
     * This method should be used by subclasses that want to provide additional
     * {@link IContentProposal}s.
     * 
     * @param paramName the name of the parameter on which the additional proposals are based
     * @param prefix the prefix for the proposals
     * @return additional {@link IContentProposal}s
     */
    protected List<IContentProposal> getAdditionalProposals(final String paramName, final String prefix) {
        return Collections.emptyList();
    }

    private void addMatchingFunctions(List<IContentProposal> result, String prefix) {
        ExprCompiler<JavaCodeFragment> compiler = expression.newExprCompiler(expression.getIpsProject());
        FlFunction<JavaCodeFragment>[] functions = compiler.getFunctions();
        for (FlFunction<JavaCodeFragment> function : functions) {
            if (checkMatchingNameWithCaseInsensitive(function.getName(), prefix)) {
                addFunctionToResult(result, function, prefix);
            }
        }
    }

    protected boolean checkMatchingNameWithCaseInsensitive(final String name, final String prefix) {
        if (name.toLowerCase().startsWith(prefix.toLowerCase())) {
            return true;
        }
        return checkMatchingQualifiedName(name, prefix);
    }

    private boolean checkMatchingQualifiedName(final String name, final String prefix) {
        String newName = name;
        int pos = newName.indexOf('.');
        while (pos > 0) {
            newName = newName.substring(pos + 1);
            if (newName.startsWith(prefix)) {
                return true;
            }
            pos = newName.indexOf('.');
        }
        return false;
    }

    private void addFunctionToResult(List<IContentProposal> result, FlFunction<JavaCodeFragment> function, String prefix) {
        String name = function.getName();
        StringBuffer displayText = new StringBuffer(name);
        displayText.append('(');
        Datatype[] argTypes = function.getArgTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0) {
                displayText.append("; "); //$NON-NLS-1$
            }
            displayText.append(argTypes[i].getName());
        }
        displayText.append(')');
        displayText.append(" - "); //$NON-NLS-1$
        displayText.append(function.getType().getName());
        String description = function.getDescription();
        ContentProposal proposal = new ContentProposal(name, displayText.toString(), description, prefix);
        result.add(proposal);
    }

    private void addMatchingAttributes(List<IContentProposal> result, String paramName, String attributePrefix) {
        try {
            Datatype datatype = findParamDatatype(paramName);
            if (!(datatype instanceof IType)) {
                return;
            }
            List<IAttribute> attributes = ((IType)datatype).findAllAttributes(getIpsProject());
            Collections.sort(attributes, new SortList());
            List<String> attributeNames = new ArrayList<String>();
            for (IAttribute attribute : attributes) {
                if (isAttributeIdentifierAllowed(attribute, attributePrefix)) {
                    if (!attributeNames.contains(attribute.getName())) {
                        addAttributeToResult(result, attribute, attributePrefix);
                        attributeNames.add(attribute.getName());
                    }
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    protected Datatype findParamDatatype(final String paramName) {
        final String[] names = paramName.split("\\."); //$NON-NLS-1$
        final IParameter param = getParameter(names[0]);
        if (param == null) {
            return null;
        }
        final IIpsProject ipsProjectTmp = getIpsProject();
        try {
            Datatype target = param.findDatatype(ipsProjectTmp);
            for (int i = 1; i < names.length; i++) {
                String name = names[i];
                boolean isIndexed = false;
                String qualifier = null;
                if (name.indexOf('[') > 0) {
                    String index = name.substring(name.indexOf('[') + 1, name.indexOf(']'));
                    if (index.indexOf('"') >= 0) {
                        qualifier = index.substring(index.indexOf('"') + 1, index.indexOf('"', index.indexOf('"') + 1));
                    }
                    name = name.substring(0, name.indexOf('['));
                    isIndexed = true;
                }
                final boolean isList = target instanceof ListOfTypeDatatype;
                if (isList) {
                    target = ((ListOfTypeDatatype)target).getBasicDatatype();
                }
                final IAssociation association = ((IType)target).findAssociation(name, ipsProjectTmp);
                target = association.findTarget(ipsProjectTmp);
                if (qualifier != null && target instanceof IPolicyCmptType) {
                    IIpsSrcFile[] productCmptSrcFiles = ipsProjectTmp.findAllProductCmptSrcFiles(
                            ((IPolicyCmptType)target).findProductCmptType(ipsProjectTmp), true);
                    for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
                        if (qualifier.equals(ipsSrcFile.getIpsObjectName())) {
                            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                            if (productCmpt != null) {
                                IPolicyCmptType policyCmptType = productCmpt.findPolicyCmptType(ipsProjectTmp);
                                if (policyCmptType != null) {
                                    target = policyCmptType;
                                }
                            }
                        }
                    }
                }
                if (!isIndexed && (isList || association.is1ToManyIgnoringQualifier())) {
                    target = new ListOfTypeDatatype(target);
                }
            }
            return target;
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    private void addAttributeToResult(List<IContentProposal> result, IAttribute attribute, String prefix) {
        String name = attribute.getName();
        String displayText = name + " - " + attribute.getDatatype(); //$NON-NLS-1$
        String localizedLabel = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(attribute);
        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(attribute);
        String description = null;
        if (StringUtils.isNotBlank(localizedLabel)) {
            description = localizedLabel;
        }
        if (StringUtils.isNotBlank(localizedDescription)) {
            if (StringUtils.isNotBlank(description)) {
                description = description + " - " + localizedDescription; //$NON-NLS-1$
            } else {
                description = localizedDescription;
            }
        }
        IContentProposal proposal = new ContentProposal(name, displayText, description, prefix);
        result.add(proposal);
    }

    protected IParameter getParameter(String name) {
        IParameter[] params = getSignature().getParameters();
        for (IParameter param : params) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }

    /**
     * The characters that are checked within this method have to be in synch with the identifier
     * tokens defined in the ffl.jjt grammar
     */
    private String getLastIdentifier(String s) {
        if (StringUtils.isEmpty(s)) {
            return ""; //$NON-NLS-1$
        }
        int i = s.length() - 1;
        boolean isInQuotes = false;
        while (i >= 0) {
            char c = s.charAt(i);
            if (c == '"') {
                isInQuotes = !isInQuotes;
            } else if (!isLegalChar(c, isInQuotes)) {
                break;
            }
            i--;
        }
        return s.substring(i + 1);
    }

    private boolean isLegalChar(char c, boolean isInQuotes) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '-' || c == '[' || c == ']'
                || (isInQuotes && c == ' ');
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    public IBaseMethod getSignature() {
        return signature;
    }

    private void addDefaultValuesToResult(final List<IContentProposal> result,
            final String paramName,
            final String attributePrefix) {
        try {
            final Datatype datatype = findParamDatatype(paramName);
            if (!(datatype instanceof IPolicyCmptType)) {
                return;
            }
            String prefix = attributePrefix;
            if (prefix.indexOf(AttributeParser.VALUE_SUFFIX_SEPARATOR_CHAR) > 0) {
                prefix = prefix.substring(0, prefix.indexOf(AttributeParser.VALUE_SUFFIX_SEPARATOR_CHAR));
            }
            final List<IAttribute> attributes = findProductRelevantAttributes((IPolicyCmptType)datatype,
                    getIpsProject());
            Collections.sort(attributes, new SortList());
            final List<String> attributeNames = new ArrayList<String>();
            for (final IAttribute attribute : attributes) {
                if (isDefaultIdentifierAllowed(attribute, attributePrefix)
                        && !attributeNames.contains(attribute.getName())) {
                    addDefaultValueToResult(result, attribute, attributePrefix);
                    attributeNames.add(attribute.getName());
                }
            }

        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    private boolean isDefaultIdentifierAllowed(IAttribute attribute, String prefix) {
        return isDefaultIdentifierAllowedByFilter(attribute) && isNameMatching(attribute, prefix);
    }

    private boolean isDefaultIdentifierAllowedByFilter(IAttribute attribute) {
        return getIdentifierFilter().isIdentifierAllowed(attribute, IdentifierKind.DEFAULT_IDENTIFIER);
    }

    private void addDefaultValueToResult(final List<IContentProposal> result,
            final IAttribute attribute,
            final String prefix) {
        String name = attribute.getName() + AttributeParser.DEFAULT_VALUE_SUFFIX;
        final String displayText = name
                + " - " + attribute.getDatatype() + Messages.ExpressionProposalProvider_defaultValue; //$NON-NLS-1$
        final String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(attribute);
        final IContentProposal proposal = new ContentProposal(name, displayText, localizedDescription, prefix);
        result.add(proposal);
    }

    private List<IAttribute> findProductRelevantAttributes(final IPolicyCmptType datatype, final IIpsProject ipsProject)
            throws CoreException {
        final List<IAttribute> attributes = datatype.findAllAttributes(ipsProject);
        for (Iterator<IAttribute> iterator = attributes.iterator(); iterator.hasNext();) {
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)iterator.next();
            if (!attribute.isProductRelevant()) {
                iterator.remove();
            }
        }
        return attributes;
    }

    private void addMatchingAssociations(final List<IContentProposal> result,
            final String paramName,
            final String attributePrefix) {
        try {
            Datatype datatype = findParamDatatype(paramName);
            boolean isList = false;
            if (datatype instanceof ListOfTypeDatatype) {
                datatype = ((ListOfTypeDatatype)datatype).getBasicDatatype();
                isList = true;
            }
            if (!(datatype instanceof IType)) {
                return;
            }
            final List<IAssociation> associations = ((IType)datatype).findAllAssociations(getIpsProject());
            Collections.sort(associations, new SortList());
            final List<String> associationNames = new ArrayList<String>();
            for (final IAssociation association : associations) {
                if (checkMatchingNameWithCaseInsensitive(association.getName(), attributePrefix)
                        && !associationNames.contains(association.getName())) {
                    addAssociationToResult(result, association, attributePrefix,
                            association.is1ToManyIgnoringQualifier() || isList);
                    associationNames.add(association.getName());
                }
            }
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    private void addAssociationToResult(final List<IContentProposal> result,
            final IAssociation association,
            final String prefix,
            boolean addIndexedProposal) {
        final String name = association.getName();
        try {
            IType target = association.findTarget(ipsProject);
            String displayText = name + " -> " + target.getUnqualifiedName(); //$NON-NLS-1$
            final String localizedDescription = IpsPlugin.getMultiLanguageSupport()
                    .getLocalizedDescription(association);
            final IContentProposal proposal = new ContentProposal(name, displayText, localizedDescription, prefix);
            result.add(proposal);
            if (addIndexedProposal) {
                final IContentProposal proposalWithIndex = new ContentProposal(name + "[0]", //$NON-NLS-1$
                        displayText + "[0]", localizedDescription, prefix); //$NON-NLS-1$
                result.add(proposalWithIndex);
            }
            if (target instanceof IPolicyCmptType && ((IPolicyCmptType)target).isConfigurableByProductCmptType()) {
                IProductCmptType productCmptType = ((IPolicyCmptType)target).findProductCmptType(ipsProject);
                IIpsSrcFile[] productCmptSrcFiles = ipsProject.findAllProductCmptSrcFiles(productCmptType, true);
                for (IIpsSrcFile srcFile : productCmptSrcFiles) {
                    String qualifier = "[\"" + srcFile.getIpsObjectName() + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$
                    final IContentProposal proposalWithQualifier = new ContentProposal(name + qualifier, displayText
                            + qualifier, localizedDescription, prefix);
                    result.add(proposalWithQualifier);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    private static final class SortContentProposal implements Comparator<IContentProposal>, Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 343634258851900476L;

        @Override
        public int compare(IContentProposal proposalToCompare, IContentProposal proposalCompareTo) {
            return proposalToCompare.getLabel().compareTo(proposalCompareTo.getLabel());
        }
    }

    private static final class SortList implements Comparator<IIpsObjectPart>, Serializable {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 4277046702978775385L;

        @Override
        public int compare(IIpsObjectPart toCompare, IIpsObjectPart compareTo) {
            return toCompare.getName().compareTo(compareTo.getName());
        }

    }
}
