/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.fl.TableUsageFunctionsResolver;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.fl.JavaExprCompiler;
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
        IBaseMethod signature = findFormulaSignature(ipsProject);
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

    /**
     * Returns the formula expression. Note that this method is overridden in the {@link Formula}
     * subclass to return the formula expression from a template if applicable.
     */
    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setExpression(String newExpression) {
        String oldExpr = expression;
        setExpressionInternal(newExpression);
        valueChanged(oldExpr, newExpression);
    }

    void setExpressionInternal(String newExpression) {
        expression = newExpression;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isEmpty(getExpression());
    }

    @Override
    public JavaExprCompiler newExprCompiler(IIpsProject ipsProject) {
        ExtendedExprCompiler compiler = ipsProject.newExpressionCompiler();

        // add the table functions based on the table usages defined in the product cmpt type
        compiler.add(new TableUsageFunctionsResolver(ipsProject, getTableContentUsages()));

        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        IBaseMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            return compiler;
        }
        IdentifierResolver<JavaCodeFragment> resolver;
        try {
            resolver = builderSet.createFlIdentifierResolver(this, compiler);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        if (resolver == null) {
            return compiler;
        }
        compiler.setIdentifierResolver(resolver);

        return compiler;
    }

    /**
     * Returns all {@link ITableContentUsage}s available for this expression.
     * 
     * @return all {@link ITableContentUsage}s available for this expression
     */
    protected abstract ITableContentUsage[] getTableContentUsages();

    @Override
    public EnumDatatype[] getEnumDatatypesAllowedInFormula() {
        HashMap<String, EnumDatatype> enumtypes = new HashMap<String, EnumDatatype>();
        collectEnumsAllowedInFormula(enumtypes);
        return enumtypes.values().toArray(new EnumDatatype[enumtypes.size()]);
    }

    private void collectEnumsAllowedInFormula(Map<String, EnumDatatype> nameToTypeMap) {
        if (getIpsProject().getReadOnlyProperties().isAssociationsInFormulas()) {
            collectAllEnumDatatypes(nameToTypeMap);
        } else {
            collectEnumTypesFromAttributes(nameToTypeMap);
            collectEnumTypesFromMethod(nameToTypeMap);
        }
    }

    private void collectAllEnumDatatypes(final Map<String, EnumDatatype> nameToTypeMap) {
        IIpsProject ipsProject = getIpsProject();
        Datatype[] datatypes = ipsProject.findDatatypes(true, false);
        for (Datatype datatype : datatypes) {
            if (datatype instanceof EnumDatatype) {
                EnumDatatype enumDatatyp = (EnumDatatype)datatype;
                nameToTypeMap.put(enumDatatyp.getName(), enumDatatyp);
            }
        }
    }

    protected abstract void collectEnumTypesFromAttributes(Map<String, EnumDatatype> enumTypes);

    private void collectEnumTypesFromMethod(Map<String, EnumDatatype> enumtypes) {
        IIpsProject ipsProject = getIpsProject();
        IBaseMethod method = findFormulaSignature(ipsProject);
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
        }
    }

    private void searchAndAdd(IIpsProject ipsProject, String datatypeName, Map<String, EnumDatatype> types) {
        if (types.containsKey(datatypeName)) {
            return;
        }
        ValueDatatype datatype = null;
        datatype = ipsProject.findValueDatatype(datatypeName);
        if (datatype instanceof EnumDatatype) {
            types.put(datatypeName, (EnumDatatype)datatype);
        }
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
        ValueToXmlHelper.addValueToElement(getExpression() == null ? StringUtils.EMPTY : getExpression().trim(),
                element, TAG_NAME_FOR_EXPRESSION);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        try {
            super.validateThis(list, ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }

        IBaseMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            String text = Messages.Formula_msgFormulaSignatureMissing;
            list.add(new Message(MSGCODE_SIGNATURE_CANT_BE_FOUND, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        // Do not use expression field as the actual expression may be defined in a template. Using
        // getExpression returns the expression from the template if applicable
        String expressionToValidate = getExpression();

        if (StringUtils.isEmpty(expressionToValidate)) {
            if (!isFormulaMandatory()) {
                return;
            }
            String text = NLS.bind(Messages.Formula_msgExpressionMissing, getFormulaSignature());
            list.add(new Message(MSGCODE_EXPRESSION_IS_EMPTY, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        Datatype signatureDatatype = null;
        try {
            signatureDatatype = method.findDatatype(ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
        validateDatatype(list, signatureDatatype, method);
        if (list.containsErrorMsg()) {
            return;
        }
        JavaExprCompiler compiler = newExprCompiler(ipsProject);
        CompilationResult<JavaCodeFragment> result = compiler.compile(expressionToValidate);
        validateCompilationResult(list, result);
        if (list.containsErrorMsg()) {
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

    private void validateDatatype(MessageList list, Datatype signatureDatatype, IBaseMethod method) {
        if (signatureDatatype == null) {
            String text = NLS.bind(Messages.FormulaElement_msgDatatypeMissing, method.getDatatype(), formulaSignature);
            list.add(new Message(MSGCODE_UNKNOWN_DATATYPE_FORMULA, text, Message.ERROR, this, PROPERTY_EXPRESSION));
        }
    }

    private void validateCompilationResult(MessageList list, CompilationResult<JavaCodeFragment> result) {
        if (!result.successfull()) {
            MessageList compilerMessageList = result.getMessages();
            for (int i = 0; i < compilerMessageList.size(); i++) {
                Message msg = compilerMessageList.getMessage(i);
                list.add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_EXPRESSION));
            }
        }
    }

    @Override
    public String getCaption(Locale locale) {
        ArgumentCheck.notNull(locale);

        String caption = null;
        IBaseMethod signature = findFormulaSignature(getIpsProject());
        if (signature != null) {
            if (signature instanceof ILabeledElement) {
                caption = ((ILabeledElement)signature).getLabelValue(locale);
            } else {
                caption = signature.getSignatureString();
            }
        }
        return caption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(formulaSignature);
    }

    @Override
    public Map<IDependency, IExpressionDependencyDetail> dependsOn() {
        return createDependencyCollector().collectDependencies();
    }

    protected ExpressionDependencyCollector createDependencyCollector() {
        return new ExpressionDependencyCollector(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Expression: "); //$NON-NLS-1$
        builder.append(getFormulaSignature());
        builder.append(" : "); //$NON-NLS-1$
        builder.append(getExpression());
        return builder.toString();
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
