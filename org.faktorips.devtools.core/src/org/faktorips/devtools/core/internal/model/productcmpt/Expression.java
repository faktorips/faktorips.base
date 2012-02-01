/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class Expression extends BaseIpsObjectPart implements IExpression {

    public final static String TAG_NAME = "Formula"; //$NON-NLS-1$
    public final static String TAG_NAME_FOR_EXPRESSION = "Expression"; //$NON-NLS-1$

    private String formulaSignature = ""; //$NON-NLS-1$
    private String expression = ""; //$NON-NLS-1$

    public Expression(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
        addTagToIgnore(TAG_NAME_FOR_EXPRESSION);
    }

    public Expression(IIpsObjectPartContainer parent, String id, String formulaSignature) {
        super(parent, id);
        this.formulaSignature = formulaSignature;
        addTagToIgnore(TAG_NAME_FOR_EXPRESSION);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public String getName() {
        return formulaSignature;
    }

    @Override
    public abstract IProductCmptType findProductCmptType(IIpsProject ipsProject);

    @Override
    public List<IAttribute> findMatchingProductCmptTypeAttributes() {
        final IProductCmptType productCmptType = findProductCmptType(getIpsProject());
        if (productCmptType != null) {
            try {
                return productCmptType.findAllAttributes(getIpsProject());
            } catch (final CoreException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String getFormulaSignature() {
        return formulaSignature;
    }

    @Override
    public void setFormulaSignature(String newName) {
        String oldName = formulaSignature;
        formulaSignature = newName;
        valueChanged(oldName, newName);
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) {
        IMethod signature = findFormulaSignature(ipsProject);
        if (signature != null) {
            try {
                Datatype datatype = signature.findDatatype(ipsProject);
                if (datatype.isValueDatatype()) {
                    return (ValueDatatype)datatype;
                }
            } catch (final CoreException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setExpression(String newExpression) {
        String oldExpr = expression;
        expression = newExpression;
        valueChanged(oldExpr, newExpression);
    }

    @Override
    public ExprCompiler newExprCompiler(IIpsProject ipsProject) {
        return newExprCompiler(ipsProject, false);
    }

    /**
     * Returns all {@link ITableContentUsage}s available for this expression.
     * 
     * @return all {@link ITableContentUsage}s available for this expression
     */
    abstract protected ITableContentUsage[] getTableContentUsages();

    @Override
    public ExtendedExprCompiler newExprCompiler(IIpsProject ipsProject, boolean formulaTest) {
        ExtendedExprCompiler compiler = ipsProject.newExpressionCompiler();

        // add the table functions based on the table usages defined in the product cmpt type
        compiler.add(new TableUsageFunctionsResolver(ipsProject, getTableContentUsages()));

        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        IMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            return compiler;
        }
        IdentifierResolver resolver;
        try {
            if (!formulaTest) {
                resolver = builderSet.createFlIdentifierResolver(this, compiler);
            } else {
                // create special identifier resolver for test methods
                resolver = builderSet.createFlIdentifierResolverForFormulaTest(this, compiler);
            }
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        if (resolver == null) {
            return compiler;
        }
        compiler.setIdentifierResolver(resolver);

        return compiler;
    }

    @Override
    public EnumDatatype[] getEnumDatatypesAllowedInFormula() {
        HashMap<String, EnumDatatype> enumtypes = new HashMap<String, EnumDatatype>();
        collectEnumsAllowedInFormula(enumtypes);
        return enumtypes.values().toArray(new EnumDatatype[enumtypes.size()]);
    }

    private void collectEnumsAllowedInFormula(Map<String, EnumDatatype> nameToTypeMap) {
        collectEnumTypesFromAttributes(nameToTypeMap);
        collectEnumTypesFromMethod(nameToTypeMap);
        collectEnumTypesFromAssociationNavigation(nameToTypeMap);
    }

    private void collectEnumTypesFromAssociationNavigation(final Map<String, EnumDatatype> nameToTypeMap) {
        IIpsProject ipsProject = getIpsProject();
        IMethod method = findFormulaSignature(ipsProject);
        String expression = getExpression();
        IParameter[] params = method.getParameters();
        try {
            for (IParameter param : params) {
                Datatype datatype = ipsProject.findDatatype(param.getDatatype());
                if (datatype instanceof IPolicyCmptType) {
                    IPolicyCmptType policyCmptType = (IPolicyCmptType)datatype;
                    String paramName = param.getName() + '.';
                    if (expression.contains(paramName)) {
                        String associationNavigations = expression.substring(expression.indexOf(paramName)
                                + paramName.length());
                        collectEnumTypesFromAssociationTarget(nameToTypeMap, ipsProject, policyCmptType,
                                associationNavigations);
                        // TODO more than once
                    }
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    private void collectEnumTypesFromAssociationTarget(final Map<String, EnumDatatype> nameToTypeMap,
            final IIpsProject ipsProject,
            final IPolicyCmptType policyCmptType,
            final String associationNavigations) throws CoreException {
        int indexOfOpeningBracket = associationNavigations.indexOf('[');
        int indexOfPeriod = associationNavigations.indexOf('.');
        if (indexOfOpeningBracket < 0 && indexOfPeriod < 0) {
            return;
        }
        String associationName = associationNavigations;
        String remainingAssociationNavigations = StringUtils.EMPTY;
        if (indexOfOpeningBracket > 0 && (indexOfPeriod > indexOfOpeningBracket || indexOfPeriod <= 0)) {
            associationName = associationNavigations.substring(0, indexOfOpeningBracket);
            remainingAssociationNavigations = associationNavigations.substring(associationNavigations.indexOf(']') + 1);
            if (remainingAssociationNavigations.startsWith(".")) { //$NON-NLS-1$
                remainingAssociationNavigations = remainingAssociationNavigations.substring(1);
            }
        } else if (indexOfPeriod > 0) {
            associationName = associationNavigations.substring(0, indexOfPeriod);
            remainingAssociationNavigations = associationNavigations.substring(indexOfPeriod + 1);
        }
        final IAssociation association = policyCmptType.findAssociation(associationName, ipsProject);
        if (association instanceof IPolicyCmptTypeAssociation) {
            final IPolicyCmptType targetPolicyCmptType = ((IPolicyCmptTypeAssociation)association)
                    .findTargetPolicyCmptType(ipsProject);
            final EnumDatatypesCollector collector = new EnumDatatypesCollector(ipsProject, nameToTypeMap);
            collector.start(targetPolicyCmptType);
            if (StringUtils.isNotBlank(remainingAssociationNavigations)) {
                collectEnumTypesFromAssociationTarget(nameToTypeMap, ipsProject, targetPolicyCmptType,
                        remainingAssociationNavigations);
            }
        }
    }

    protected abstract void collectEnumTypesFromAttributes(Map<String, EnumDatatype> enumTypes);

    private void collectEnumTypesFromMethod(Map<String, EnumDatatype> enumtypes) {
        IIpsProject ipsProject = getIpsProject();
        IMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            return;
        }
        Datatype valuetype = null;
        try {
            valuetype = method.findDatatype(ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        if (valuetype instanceof EnumDatatype) {
            enumtypes.put(valuetype.getName(), (EnumDatatype)valuetype);
        }
        IParameter[] params = method.getParameters();
        for (IParameter param : params) {
            try {
                Datatype datatype = ipsProject.findDatatype(param.getDatatype());
                if (datatype instanceof EnumDatatype) {
                    enumtypes.put(datatype.getName(), (EnumDatatype)datatype);
                    continue;
                }
                if (datatype instanceof IPolicyCmptType) {
                    IPolicyCmptType policyCmptType = (IPolicyCmptType)datatype;
                    EnumDatatypesCollector collector = new EnumDatatypesCollector(ipsProject, enumtypes);
                    collector.start(policyCmptType);
                }
            } catch (Exception e) {
                IpsPlugin.log(e);
            }
        }
    }

    private void searchAndAdd(IIpsProject ipsProject, String datatypeName, Map<String, EnumDatatype> types) {
        if (types.containsKey(datatypeName)) {
            return;
        }
        ValueDatatype datatype = null;
        try {
            datatype = ipsProject.findValueDatatype(datatypeName);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        if (datatype instanceof EnumDatatype) {
            types.put(datatypeName, (EnumDatatype)datatype);
        }
    }

    @Override
    public String[] getParameterIdentifiersUsedInFormula(IIpsProject ipsProject) {
        if (StringUtils.isEmpty(expression)) {
            return new String[0];
        }
        IProductCmptTypeMethod signature = findFormulaSignature(ipsProject);
        if (signature == null) {
            return new String[0];
        }
        ExprCompiler compiler = newExprCompiler(ipsProject);
        CompilationResult compilationResult = compiler.compile(expression);

        // store the resolved identifiers in the cache
        String[] resolvedIdentifiers = compilationResult.getResolvedIdentifiers();
        if (resolvedIdentifiers.length == 0) {
            return resolvedIdentifiers;
        }
        Map<String, EnumDatatype> enumNamesToTypes = new HashMap<String, EnumDatatype>();
        collectEnumsAllowedInFormula(enumNamesToTypes);
        List<String> filteredIdentifieres = removeIdentifieresOfEnumDatatypes(enumNamesToTypes, resolvedIdentifiers);

        List<IAttribute> attributes = Collections.emptyList();
        try {
            attributes = signature.getProductCmptType().findAllAttributes(ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        Set<String> attributeNames = new HashSet<String>(attributes.size());
        for (IAttribute attribute : attributes) {
            attributeNames.add(attribute.getName());
        }

        for (Iterator<String> it = filteredIdentifieres.iterator(); it.hasNext();) {
            String idendtifier = it.next();
            if (attributeNames.contains(idendtifier)) {
                it.remove();
            }
        }
        return filteredIdentifieres.toArray(new String[filteredIdentifieres.size()]);
    }

    private List<String> removeIdentifieresOfEnumDatatypes(Map<String, EnumDatatype> enumDatatypes,
            String[] allIdentifiersUsedInFormula) {
        List<String> filteredIdentifiers = new ArrayList<String>(allIdentifiersUsedInFormula.length);
        for (String element : allIdentifiersUsedInFormula) {
            if (element != null) {
                if (element.indexOf('.') != -1) {
                    String identifierRoot = element.substring(0, element.indexOf('.'));
                    if (!enumDatatypes.containsKey(identifierRoot)) {
                        filteredIdentifiers.add(element);
                    }
                    continue;
                }
                filteredIdentifiers.add(element);
            }
        }
        return filteredIdentifiers;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        formulaSignature = element.getAttribute(PROPERTY_FORMULA_SIGNATURE_NAME);
        expression = ValueToXmlHelper.getValueFromElement(element, TAG_NAME_FOR_EXPRESSION);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_FORMULA_SIGNATURE_NAME, formulaSignature);
        ValueToXmlHelper.addValueToElement(expression, element, TAG_NAME_FOR_EXPRESSION);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        try {
            super.validateThis(list, ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        if (StringUtils.isEmpty(expression)) {
            String text = NLS.bind(Messages.Formula_msgExpressionMissing, formulaSignature);
            list.add(new Message(MSGCODE_EXPRESSION_IS_EMPTY, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        ExprCompiler compiler = newExprCompiler(ipsProject);
        CompilationResult result = compiler.compile(expression);
        if (!result.successfull()) {
            MessageList compilerMessageList = result.getMessages();
            for (int i = 0; i < compilerMessageList.size(); i++) {
                Message msg = compilerMessageList.getMessage(i);
                list.add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_EXPRESSION));
            }
            return;
        }
        IMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            String text = Messages.Formula_msgFormulaSignatureMissing;
            list.add(new Message(MSGCODE_SIGNATURE_CANT_BE_FOUND, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        Datatype signatureDatatype = null;
        try {
            signatureDatatype = method.findDatatype(ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        if (signatureDatatype == null) {
            String text = Messages.ConfigElement_msgDatatypeMissing;
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE_FORMULA, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        if (signatureDatatype.equals(result.getDatatype())) {
            return;
        }
        if (compiler.getConversionCodeGenerator().canConvert(result.getDatatype(), signatureDatatype)) {
            return;
        }
        String text = NLS.bind(Messages.Formula_msgWrongReturntype, signatureDatatype, result.getDatatype().getName());
        list.add(new Message(MSGCODE_WRONG_FORMULA_DATATYPE, text, Message.ERROR, this, PROPERTY_EXPRESSION));
    }

    @Override
    public String getCaption(Locale locale) {
        ArgumentCheck.notNull(locale);

        String caption = null;
        IProductCmptTypeMethod signature = findFormulaSignature(getIpsProject());
        if (signature != null) {
            caption = signature.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(formulaSignature);
    }

    class EnumDatatypesCollector extends TypeHierarchyVisitor<IPolicyCmptType> {

        private IIpsProject project;

        private Map<String, EnumDatatype> enumtypes;

        public EnumDatatypesCollector(IIpsProject project, Map<String, EnumDatatype> enumtypes) {
            super(project);
            this.project = project;
            this.enumtypes = enumtypes;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            List<IPolicyCmptTypeAttribute> attr = currentType.getPolicyCmptTypeAttributes();
            for (IPolicyCmptTypeAttribute element : attr) {
                searchAndAdd(project, element.getDatatype(), enumtypes);
            }
            return true;
        }

    }

}
