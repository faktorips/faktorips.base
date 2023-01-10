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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.internal.fl.TableUsageFunctionsResolver;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IOverridableLabeledElement;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.fl.JavaExprCompiler;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
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
            return productCmptType.findAllAttributes(getIpsProject());
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
            Datatype datatype = signature.findDatatype(ipsProject);
            if (datatype.isValueDatatype()) {
                return (ValueDatatype)datatype;
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
        return IpsStringUtils.isEmpty(getExpression());
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
        resolver = builderSet.createFlIdentifierResolver(this, compiler);
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
        HashMap<String, EnumDatatype> enumtypes = new HashMap<>();
        collectAllEnumDatatypes(enumtypes);
        return enumtypes.values().toArray(new EnumDatatype[enumtypes.size()]);
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

    private void searchAndAdd(IIpsProject ipsProject, String datatypeName, Map<String, EnumDatatype> types) {
        if (types.containsKey(datatypeName)) {
            return;
        }
        ValueDatatype datatype = ipsProject.findValueDatatype(datatypeName);
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
        ValueToXmlHelper.addValueToElement(getExpression() == null ? IpsStringUtils.EMPTY : getExpression().trim(),
                element, TAG_NAME_FOR_EXPRESSION);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);

        IBaseMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            String text = Messages.Formula_msgFormulaSignatureMissing;
            list.add(new Message(MSGCODE_SIGNATURE_CANT_BE_FOUND, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        // Do not use expression field as the actual expression may be defined in a template. Using
        // getExpression returns the expression from the template if applicable
        String expressionToValidate = getExpression();

        if (IpsStringUtils.isEmpty(expressionToValidate)) {
            if (!isFormulaMandatory()) {
                return;
            }
            String text = MessageFormat.format(Messages.Formula_msgExpressionMissing, getFormulaSignature());
            list.add(new Message(MSGCODE_EXPRESSION_IS_EMPTY, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        Datatype signatureDatatype = method.findDatatype(ipsProject);
        validateDatatype(list, signatureDatatype, method);
        if (list.containsErrorMsg()) {
            return;
        }
        JavaExprCompiler compiler = newExprCompiler(ipsProject);
        CompilationResult<JavaCodeFragment> result = compiler.compile(expressionToValidate);
        validateCompilationResult(list, result);
        if (list.containsErrorMsg() || signatureDatatype.equals(result.getDatatype())
                || compiler.getConversionCodeGenerator().canConvert(result.getDatatype(), signatureDatatype)) {
            return;
        }
        String text = MessageFormat.format(Messages.Formula_msgWrongReturntype, signatureDatatype,
                result.getDatatype().getName());
        list.add(new Message(MSGCODE_WRONG_FORMULA_DATATYPE, text, Message.ERROR, this, PROPERTY_EXPRESSION));
    }

    private void validateDatatype(MessageList list, Datatype signatureDatatype, IBaseMethod method) {
        if (signatureDatatype == null) {
            String text = MessageFormat.format(Messages.FormulaElement_msgDatatypeMissing, method.getDatatype(),
                    formulaSignature);
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
            if (signature instanceof IOverridableLabeledElement) {
                caption = ((IOverridableLabeledElement)signature).getLabelValueFromThisOrSuper(locale);
            } else if (signature instanceof ILabeledElement) {
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
