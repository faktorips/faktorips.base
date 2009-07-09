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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
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
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class Formula extends BaseIpsObjectPart implements IFormula {

    final static String TAG_NAME = "Formula"; //$NON-NLS-1$
    private final static String TAG_NAME_FOR_EXPRESSION = "Expression"; //$NON-NLS-1$

    private String formulaSignature = ""; //$NON-NLS-1$
    private String expression = ""; //$NON-NLS-1$
    private IpsObjectPartCollection testcases = new IpsObjectPartCollection(this, FormulaTestCase.class, IFormulaTestCase.class, FormulaTestCase.TAG_NAME);
    
    public Formula(IIpsObjectPart parent, int id) {
        super(parent, id);
        addTagToIgnore(TAG_NAME_FOR_EXPRESSION);
    }

    public Formula(IIpsObjectPart parent, int id, String formulaSignature) {
        super(parent, id);
        this.formulaSignature = formulaSignature;
        addTagToIgnore(TAG_NAME_FOR_EXPRESSION);
    }

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return formulaSignature;
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return formulaSignature;
    }

    /**
     * {@inheritDoc}
     */
    public IProdDefProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findFormulaSignature(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return ProdDefPropertyType.FORMULA;
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyValue() {
        return expression;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }
    
    /**
     * {@inheritDoc}
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmptGeneration().getProductCmpt().findProductCmptType(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public String getFormulaSignature() {
        return formulaSignature;
    }

    /**
     * {@inheritDoc}
     */
    public void setFormulaSignature(String newName) {
        String oldName = formulaSignature;
        formulaSignature = newName;
        valueChanged(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(formulaSignature)) {
            return null;
        }
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type==null) {
            return null;
        }
        return type.findFormulaSignature(formulaSignature, ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException {
        IMethod signature = findFormulaSignature(ipsProject);
        if (signature!=null) {
            Datatype datatype = signature.findDatatype(ipsProject);
            if (datatype.isValueDatatype()) {
                return (ValueDatatype)datatype;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getExpression() {
        return expression;
    }

    /**
     * {@inheritDoc}
     */
    public void setExpression(String newExpression) {
        String oldExpr = expression;
        expression = newExpression;
        valueChanged(oldExpr, newExpression);
    }

    /**
     * {@inheritDoc}
     */
    public ExprCompiler newExprCompiler(IIpsProject ipsProject) throws CoreException {
        return newExprCompiler(ipsProject, false);
    }

    /**
     * {@inheritDoc}
     */
    public ExprCompiler newExprCompiler(IIpsProject ipsProject, boolean formulaTest) throws CoreException {
        ExprCompiler compiler = ipsProject.newExpressionCompiler();
        
        // add the table functions based on the table usages defined in the product cmpt type
        IProductCmptGeneration gen = getProductCmptGeneration();
        compiler.add(new TableUsageFunctionsResolver(ipsProject, gen.getTableContentUsages()));
        
        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        IMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            return compiler;
        }
        IdentifierResolver resolver;
        if (! formulaTest){
            resolver = builderSet.createFlIdentifierResolver(this);
        } else {
            // create special identifier resolver for test methods
            resolver = builderSet.createFlIdentifierResolverForFormulaTest(this);
        }
        if (resolver == null) {
            return compiler;
        }
        compiler.setIdentifierResolver(resolver);
        
        return compiler;
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("Formula.gif"); //$NON-NLS-1$    
    }

    /**
     * {@inheritDoc}
     */
    public EnumDatatype[] getEnumDatatypesAllowedInFormula() throws CoreException {
        HashMap enumtypes = new HashMap();
        collectEnumsAllowedInFormula(enumtypes);
        return (EnumDatatype[])enumtypes.values().toArray(new EnumDatatype[enumtypes.size()]);
    }
    
    private void collectEnumsAllowedInFormula(Map nameToTypeMap) throws CoreException{
        collectEnumTypesFromAttributes(nameToTypeMap);
        collectEnumTypesFromMethod(nameToTypeMap);
    }
    
    private void collectEnumTypesFromAttributes(Map enumTypes) throws CoreException{
        IIpsProject ipsProject = getIpsProject();
        IProductCmptType productCmptType = getProductCmptGeneration().getProductCmpt().findProductCmptType(ipsProject);
        if(productCmptType != null){
            IAttribute[] attributes = productCmptType.findAllAttributes(ipsProject);
            for (int i = 0; i < attributes.length; i++) {
                Datatype datatype = attributes[i].findDatatype(ipsProject);
                if(datatype instanceof EnumDatatype){
                    enumTypes.put(datatype.getName(), datatype);
                }
            }
        }
    }
    
    private void collectEnumTypesFromMethod(Map enumtypes) throws CoreException {
        IIpsProject ipsProject = getIpsProject();
        IMethod method = findFormulaSignature(getIpsProject());
        if (method == null) {
            return;
        }
        Datatype valuetype = method.findDatatype(ipsProject);
        if (valuetype instanceof EnumDatatype) {
            enumtypes.put(valuetype.getName(), valuetype);
        }
        IParameter[] params = method.getParameters();
        for (int i = 0; i < params.length; i++) {
            try {
                Datatype datatype = ipsProject.findDatatype(params[i].getDatatype());
                if (datatype instanceof EnumDatatype) {
                    enumtypes.put(datatype.getName(), datatype);
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
    
    private void searchAndAdd(IIpsProject ipsProject, String datatypeName, Map types) throws CoreException {
        if (types.containsKey(datatypeName)) {
            return;
        }
        ValueDatatype datatype = ipsProject.findValueDatatype(datatypeName);
        if (datatype instanceof EnumDatatype) {
            types.put(datatypeName, datatype);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getParameterIdentifiersUsedInFormula(IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(expression)){
            return new String[0];
        }
        IProductCmptTypeMethod signature = findFormulaSignature(ipsProject);
        if (signature == null){
            return new String[0];
        }
        ExprCompiler compiler = newExprCompiler(ipsProject);
        CompilationResult compilationResult = compiler.compile(expression);
        
        // store the resolved identifiers in the cache
        String[] resolvedIdentifiers = compilationResult.getResolvedIdentifiers();
        if(resolvedIdentifiers.length == 0){
            return resolvedIdentifiers;
        }
        Map enumNamesToTypes = new HashMap();
        collectEnumsAllowedInFormula(enumNamesToTypes);
        List filteredIdentifieres = removeIdentifieresOfEnumDatatypes(enumNamesToTypes, resolvedIdentifiers);
        
        IAttribute[] attributes = signature.getProductCmptType().findAllAttributes(ipsProject);
        Set attributeNames = new HashSet(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            attributeNames.add(attributes[i].getName());
        }
        
        for (Iterator it = filteredIdentifieres.iterator(); it.hasNext();) {
            String idendtifier = (String)it.next();
            if(attributeNames.contains(idendtifier)){
                it.remove();
            }
        }
        return (String[])filteredIdentifieres.toArray(new String[filteredIdentifieres.size()]);
    }
    
    private List removeIdentifieresOfEnumDatatypes(Map enumDatatypes, String[] allIdentifiersUsedInFormula){
        List filteredIdentifiers = new ArrayList(allIdentifiersUsedInFormula.length);
        for (int i = 0; i < allIdentifiersUsedInFormula.length; i++) {
            if(allIdentifiersUsedInFormula[i] != null){
                if(allIdentifiersUsedInFormula[i].indexOf('.') != -1){
                    String identifierRoot = allIdentifiersUsedInFormula[i].substring(0, allIdentifiersUsedInFormula[i].indexOf('.'));
                    if(!enumDatatypes.containsKey(identifierRoot)){
                        filteredIdentifiers.add(allIdentifiersUsedInFormula[i]);
                    }
                    continue;
                }
                filteredIdentifiers.add(allIdentifiersUsedInFormula[i]);
            }
        }
        return filteredIdentifiers;
    }

    
    /**
     * {@inheritDoc}
     */
    public IFormulaTestCase newFormulaTestCase() {
        return (IFormulaTestCase)testcases.newPart();
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestCase getFormulaTestCase(String name) {
        return (IFormulaTestCase)testcases.getPartByName(name);
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestCase[] getFormulaTestCases() {
        return (IFormulaTestCase[])testcases.toArray(new IFormulaTestCase[testcases.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveFormulaTestCases(int[] indexes, boolean up) {
        return testcases.moveParts(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    public void removeFormulaTestCase(IFormulaTestCase formulaTest) {
        testcases.removePart(formulaTest);
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        formulaSignature = element.getAttribute(PROPERTY_FORMULA_SIGNATURE_NAME);
        expression = ValueToXmlHelper.getValueFromElement(element, TAG_NAME_FOR_EXPRESSION);
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_FORMULA_SIGNATURE_NAME, formulaSignature);
        ValueToXmlHelper.addValueToElement(expression, element, TAG_NAME_FOR_EXPRESSION); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
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
                list.add(new Message(msg.getCode(), msg.getText(), msg
                        .getSeverity(), this, PROPERTY_EXPRESSION));
            }
            return;
        }
        IMethod method = findFormulaSignature(ipsProject);
        if (method==null) {
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
        if (compiler.getConversionCodeGenerator().canConvert(
                result.getDatatype(), signatureDatatype)) {
            return;
        }
        String text = NLS.bind(Messages.Formula_msgWrongReturntype, signatureDatatype, result.getDatatype().getName());
        list.add(new Message(MSGCODE_WRONG_FORMULA_DATATYPE, text, Message.ERROR, this, PROPERTY_EXPRESSION));
    }


    class EnumDatatypesCollector extends PolicyCmptTypeHierarchyVisitor {
        
        private IIpsProject project;
        private Map enumtypes;
        
        public EnumDatatypesCollector(IIpsProject project, Map enumtypes) {
            super();
            this.project = project;
            this.enumtypes = enumtypes;
        }

        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IPolicyCmptTypeAttribute[] attr = currentType.getPolicyCmptTypeAttributes();
            for (int i = 0; i < attr.length; i++) {
                searchAndAdd(project, attr[i].getDatatype(), enumtypes);
            }
            return true;
        }
        
    }


}
