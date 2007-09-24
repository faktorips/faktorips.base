/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.BaseIpsObjectPart;
import org.faktorips.devtools.core.internal.model.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.product.IFormula;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype2.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype2.ProdDefPropertyType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExcelFunctionsResolver;
import org.faktorips.fl.ExprCompiler;
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

    final static String TAG_NAME = "Formula";
    private final static String TAG_NAME_FOR_EXPRESSION = "Expression";

    private String formulaSignature = "";
    private String expression = "";
    private IpsObjectPartCollection testcases = new IpsObjectPartCollection(this, FormulaTestCase.class, FormulaTestCase.TAG_NAME);
    
    public Formula(IIpsObjectPart parent, int id) {
        super(parent, id);
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
    public ExprCompiler getExprCompiler(IIpsProject ipsProject) throws CoreException {
        ExprCompiler compiler = new ExprCompiler();
        compiler.add(new ExcelFunctionsResolver(ipsProject.getExpressionLanguageFunctionsLanguage()));
        
        // add the table functions based on the table usages defined in the product cmpt type
        IProductCmptGeneration gen = getProductCmptGeneration();
        compiler.add(new TableUsageFunctionsResolver(ipsProject, gen.getTableContentUsages()));
        
        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        IMethod method = findFormulaSignature(ipsProject);
        if (method == null) {
            return compiler;
        }
        IParameterIdentifierResolver resolver = builderSet.getFlParameterIdentifierResolver();
        if (resolver == null) {
            return compiler;
        }
        resolver.setIpsProject(getIpsProject());
        resolver.setParameters(method.getParameters());
        resolver.setEnumDatatypes(getEnumDatatypesAllowedInFormula());
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
        collectEnumTypesFromMethod(enumtypes);
        collectEnumTypesFromUsedTableStructures(enumtypes);
        return (EnumDatatype[])enumtypes.values().toArray(new EnumDatatype[enumtypes.size()]);
    }
    
    private void collectEnumTypesFromMethod(HashMap enumtypes) throws CoreException {
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
    
    private void collectEnumTypesFromUsedTableStructures(HashMap enumtypes) throws CoreException {
        IProductCmptType type = getProductCmptGeneration().getProductCmpt().findProductCmptType(getIpsProject());
        if (type==null) {
            return;
        }
        ITableStructureUsage[] usages = type.getTableStructureUsages();
        for (int i = 0; i < usages.length; i++) {
            String[] tableNames = usages[i].getTableStructures();
            IIpsProject project = getIpsProject();
            for (int j = 0; j < tableNames.length; j++) {
                ITableStructure table = (ITableStructure)project.findIpsObject(new QualifiedNameType(tableNames[j], IpsObjectType.TABLE_STRUCTURE));
                if (table!=null) {
                    collectEnumTypesFromTable(table, project, enumtypes);
                }
            }
        }
    }
    
    private void collectEnumTypesFromTable(ITableStructure table, IIpsProject project, HashMap types) throws CoreException {
        IColumn[] columns = table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            searchAndAdd(project, columns[i].getDatatype(), types);
        }
    }
    
    private void searchAndAdd(IIpsProject ipsProject, String datatypeName, HashMap types) throws CoreException {
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
        IMethod signature = findFormulaSignature(ipsProject);
        if (signature == null){
            return new String[0];
        }
        ExprCompiler compiler = getExprCompiler(ipsProject);
        CompilationResult compilationResult = compiler.compile(expression);
        
        // store the resolved identifiers in the cache
        IParameterIdentifierResolver resolver = (IParameterIdentifierResolver)compiler.getIdentifierResolver();
        return resolver.removeIdentifieresOfEnumDatatypes(compilationResult.getResolvedIdentifiers());
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
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IIpsProject ipsProject = getIpsProject();
        if (StringUtils.isEmpty(expression)) {
            String text = "Expression is missing for formula " + formulaSignature;
            list.add(new Message(MSGCODE_EXPRESSION_IS_EMPTY, text, Message.ERROR, this, PROPERTY_EXPRESSION));
            return;
        }
        ExprCompiler compiler = getExprCompiler(ipsProject);
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
            String text = "The formula's signature can't be found.";
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
        // String text = NLS.bind(Messages.ConfigElement_msgReturnTypeMissmatch, attributeDatatype.getName(), result.getDatatype().getName());
        String text = "Formula should return {0} but returns a {1}. A conversion is not possible.";
        list.add(new Message(MSGCODE_WRONG_FORMULA_DATATYPE, text, Message.ERROR, this, PROPERTY_EXPRESSION));
    }


    class EnumDatatypesCollector extends PolicyCmptTypeHierarchyVisitor {
        
        private IIpsProject project;
        private HashMap enumtypes;
        
        public EnumDatatypesCollector(IIpsProject project, HashMap enumtypes) {
            super();
            this.project = project;
            this.enumtypes = enumtypes;
        }

        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            IAttribute[] attr = currentType.getAttributes();
            for (int i = 0; i < attr.length; i++) {
                searchAndAdd(project, attr[i].getDatatype(), enumtypes);
            }
            return true;
        }
        
    }


}
