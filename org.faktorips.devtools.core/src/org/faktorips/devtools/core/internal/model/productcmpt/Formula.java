/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
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
public class Formula extends BaseIpsObjectPart implements IFormula {

    public final static String TAG_NAME = "Formula"; //$NON-NLS-1$
    public final static String TAG_NAME_FOR_EXPRESSION = "Expression"; //$NON-NLS-1$

    private String formulaSignature = ""; //$NON-NLS-1$
    private String expression = ""; //$NON-NLS-1$
    private IpsObjectPartCollection<IFormulaTestCase> testcases = new IpsObjectPartCollection<IFormulaTestCase>(this,
            FormulaTestCase.class, IFormulaTestCase.class, FormulaTestCase.TAG_NAME);

    public Formula(IIpsObjectPart parent, String id) {
        super(parent, id);
        addTagToIgnore(TAG_NAME_FOR_EXPRESSION);
    }

    public Formula(IIpsObjectPart parent, String id, String formulaSignature) {
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
    public String getPropertyName() {
        return formulaSignature;
    }

    @Override
    public IProdDefProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findFormulaSignature(ipsProject);
    }

    @Override
    public ProdDefPropertyType getPropertyType() {
        return ProdDefPropertyType.FORMULA;
    }

    @Override
    public String getPropertyValue() {
        return expression;
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmptGeneration().getProductCmpt().findProductCmptType(ipsProject);
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
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(formulaSignature)) {
            return null;
        }
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findFormulaSignature(formulaSignature, ipsProject);
    }

    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException {
        IMethod signature = findFormulaSignature(ipsProject);
        if (signature != null) {
            Datatype datatype = signature.findDatatype(ipsProject);
            if (datatype.isValueDatatype()) {
                return (ValueDatatype)datatype;
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
    public ExprCompiler newExprCompiler(IIpsProject ipsProject) throws CoreException {
        return newExprCompiler(ipsProject, false);
    }

    @Override
    public ExtendedExprCompiler newExprCompiler(IIpsProject ipsProject, boolean formulaTest) throws CoreException {
        ExtendedExprCompiler compiler = ipsProject.newExpressionCompiler();

        // add the table functions based on the table usages defined in the product cmpt type
        IProductCmptGeneration gen = getProductCmptGeneration();
        compiler.add(new TableUsageFunctionsResolver(ipsProject, gen.getTableContentUsages()));

        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        IMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            return compiler;
        }
        IdentifierResolver resolver;
        if (!formulaTest) {
            resolver = builderSet.createFlIdentifierResolver(this, compiler);
        } else {
            // create special identifier resolver for test methods
            resolver = builderSet.createFlIdentifierResolverForFormulaTest(this, compiler);
        }
        if (resolver == null) {
            return compiler;
        }
        compiler.setIdentifierResolver(resolver);

        return compiler;
    }

    @Override
    public EnumDatatype[] getEnumDatatypesAllowedInFormula() throws CoreException {
        HashMap<String, EnumDatatype> enumtypes = new HashMap<String, EnumDatatype>();
        collectEnumsAllowedInFormula(enumtypes);
        return enumtypes.values().toArray(new EnumDatatype[enumtypes.size()]);
    }

    private void collectEnumsAllowedInFormula(Map<String, EnumDatatype> nameToTypeMap) throws CoreException {
        collectEnumTypesFromAttributes(nameToTypeMap);
        collectEnumTypesFromMethod(nameToTypeMap);
    }

    private void collectEnumTypesFromAttributes(Map<String, EnumDatatype> enumTypes) throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        IProductCmptType productCmptType = getProductCmptGeneration().getProductCmpt().findProductCmptType(ipsProject);
        if (productCmptType != null) {
            IAttribute[] attributes = productCmptType.findAllAttributes(ipsProject);
            for (IAttribute attribute : attributes) {
                Datatype datatype = attribute.findDatatype(ipsProject);
                if (datatype instanceof EnumDatatype) {
                    enumTypes.put(datatype.getName(), (EnumDatatype)datatype);
                }
            }
        }
    }

    private void collectEnumTypesFromMethod(Map<String, EnumDatatype> enumtypes) throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        IMethod method = findFormulaSignature(getIpsProject());
        if (method == null) {
            return;
        }
        Datatype valuetype = method.findDatatype(ipsProject);
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

    private void searchAndAdd(IIpsProject ipsProject, String datatypeName, Map<String, EnumDatatype> types)
            throws CoreException {
        if (types.containsKey(datatypeName)) {
            return;
        }
        ValueDatatype datatype = ipsProject.findValueDatatype(datatypeName);
        if (datatype instanceof EnumDatatype) {
            types.put(datatypeName, (EnumDatatype)datatype);
        }
    }

    @Override
    public String[] getParameterIdentifiersUsedInFormula(IIpsProject ipsProject) throws CoreException {
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

        IAttribute[] attributes = signature.getProductCmptType().findAllAttributes(ipsProject);
        Set<String> attributeNames = new HashSet<String>(attributes.length);
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
    public IFormulaTestCase newFormulaTestCase() {
        return testcases.newPart();
    }

    @Override
    public IFormulaTestCase getFormulaTestCase(String name) {
        return testcases.getPartByName(name);
    }

    @Override
    public IFormulaTestCase[] getFormulaTestCases() {
        return testcases.toArray(new IFormulaTestCase[testcases.size()]);
    }

    @Override
    public int[] moveFormulaTestCases(int[] indexes, boolean up) {
        return testcases.moveParts(indexes, up);
    }

    @Override
    public void removeFormulaTestCase(IFormulaTestCase formulaTest) {
        testcases.removePart(formulaTest);
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
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        if (StringUtils.isEmpty(expression)) {
            String text = NLS.bind(Messages.Formula_msgExpressionMissing, formulaSignature);
            list.add(new Message(MSGCODE_EXPRESSION_IS_EMPTY, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        ExprCompiler compiler = newExprCompiler(ipsProject);
        CompilationResult result = compiler.compile(expression);
        if (!result.successfull()) {
            MessageList compilerMessageList = result.getMessages();
            for (int i = 0; i < compilerMessageList.getNoOfMessages(); i++) {
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
        Datatype signatureDatatype = method.findDatatype(ipsProject);
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
    public String getCaption(Locale locale) throws CoreException {
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

    class EnumDatatypesCollector extends PolicyCmptTypeHierarchyVisitor {

        private IIpsProject project;

        private Map<String, EnumDatatype> enumtypes;

        public EnumDatatypesCollector(IIpsProject project, Map<String, EnumDatatype> enumtypes) {
            super();
            this.project = project;
            this.enumtypes = enumtypes;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IPolicyCmptTypeAttribute[] attr = currentType.getPolicyCmptTypeAttributes();
            for (IPolicyCmptTypeAttribute element : attr) {
                searchAndAdd(project, element.getDatatype(), enumtypes);
            }
            return true;
        }

    }

}
