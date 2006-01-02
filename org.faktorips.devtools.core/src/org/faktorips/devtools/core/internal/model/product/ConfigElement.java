package org.faktorips.devtools.core.internal.model.product;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ParameterIdentifierResolver;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExcelFunctionsResolver;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.StringUtil;
import org.faktorips.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ConfigElement extends IpsObjectPart implements IConfigElement {

    final static String TAG_NAME = "ConfigElement";

    private ConfigElementType type = ConfigElementType.PRODUCT_ATTRIBUTE;
    private String pcTypeAttribute = "";
    private ValueSet valueSet = ValueSet.ALL_VALUES;
    private String value = "";

    public ConfigElement(ProductCmptGeneration parent, int id) {
        super(parent, id);
    }

    public ConfigElement() {
        super();
    }

    /**
     * Overridden.
     */
    public IProductCmpt getProductCmpt() {
        return (IProductCmpt)getParent().getParent();
    }

    /**
     * Overridden.
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }

    /**
     * Overridden.
     */
    public ConfigElementType getType() {
        return type;
    }

    /**
     * Overridden.
     */
    public void setType(ConfigElementType newType) {
        ConfigElementType oldType = type;
        type = newType;
        valueChanged(oldType, newType);

    }

    /**
     * Overridden.
     */
    public String getPcTypeAttribute() {
        return pcTypeAttribute;
    }

    /**
     * Overridden.
     */
    public void setPcTypeAttribute(String newName) {
        String oldName = pcTypeAttribute;
        pcTypeAttribute = newName;
        name = pcTypeAttribute;
        valueChanged(oldName, pcTypeAttribute);
    }

    /**
     * Overridden.
     */
    public String getValue() {
        return value;
    }

    /**
     * Overridden.
     */
    public void setValue(String newValue) {
        String oldValue = value;
        value = newValue;
        valueChanged(oldValue, value);
    }

    /**
     * Overridden.
     */
    public void delete() {
        ((ProductCmptGeneration)getParent()).removeConfigElement(this);
    }

    /**
     * Overridden.
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("AttributePublic.gif");
    }

    /**
     * Overridden.
     */
    public IAttribute findPcTypeAttribute() throws CoreException {
        IPolicyCmptType pcType = ((IProductCmpt)getIpsObject()).findPolicyCmptType();
        if (pcType == null) {
            return null;
        }
        IAttribute a = pcType.getAttribute(this.pcTypeAttribute);
        if (a != null) {
            return a;
        }
        ITypeHierarchy hierarchy = pcType.getSupertypeHierarchy();
        return hierarchy.findAttribute(pcType, pcTypeAttribute);
    }

    /**
     * Overridden.
     */
    public ExprCompiler getExprCompiler() throws CoreException {
        ExprCompiler compiler = new ExprCompiler();
        compiler.add(new ExcelFunctionsResolver(getIpsProject().getExpressionLanguageFunctionsLanguage()));
        compiler.add(new TableFunctionsResolver(getIpsProject()));
        ParameterIdentifierResolver resolver = new ParameterIdentifierResolver(getIpsProject());
        IAttribute a = findPcTypeAttribute();
        if (a == null) {
            return compiler;
        }
        Parameter[] params = a.getFormulaParameters();
        for (int i = 0; i < params.length; i++) {
            resolver.add(params[i]);
        }
        compiler.setIdentifierResolver(resolver);
        return compiler;
    }

    /**
     * Overridden.
     */
    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        IAttribute attribute = findPcTypeAttribute();
        if (attribute == null) {
            String text = "There is no attribute " + pcTypeAttribute + " defined in "
                    + getProductCmpt().getPolicyCmptType() + ".";
            list.add(new Message("", text, Message.ERROR, this, PROPERTY_VALUE));
            return;
        } 
        if (attribute.getAttributeType()==AttributeType.CHANGEABLE || attribute.getAttributeType()==AttributeType.CONSTANT) {
            validateValue(attribute.getDatatype(), list);
        } else {
            validateFormula(attribute, list);
        }
    }

    private void validateFormula(IAttribute attribute, MessageList list) throws CoreException {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        ExprCompiler compiler = getExprCompiler();
        CompilationResult result = compiler.compile(value);
        if (!result.successfull()) {
            MessageList compilerMessageList = result.getMessages();
            for (int i = 0; i < compilerMessageList.getNoOfMessages(); i++) {
                Message msg = compilerMessageList.getMessage(i);
                list.add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), this, PROPERTY_VALUE));
            }
            return;
        }
        Datatype attributeDatatype = attribute.findDatatype();
        if (attributeDatatype==null) {
            String text = "The attribute's datatype can't be found, so it is not possible to check if the formula returns the correct datatype.";
            list.add(new Message(IConfigElement.MSGCODE_CANT_FIND_ATTRIBUTE_DATATYPE, text, Message.ERROR, this, PROPERTY_VALUE));
            return;
        }
        if (attributeDatatype.equals(result.getDatatype())) {
            return;
        }
        if (compiler.getConversionCodeGenerator().canConvert(result.getDatatype(), attributeDatatype)) {
            return;
        }
        String text = "Formula should return " + attributeDatatype.getName() + " but returns a " + result.getDatatype().getName()
            + ". A conversion is not possible.";
        list.add(new Message(IConfigElement.MSGCODE_FORMULA_HAS_WRONG_DATATYPE, text, Message.ERROR, this, PROPERTY_VALUE));
    }

    private void validateValue(String datatype, MessageList list) throws CoreException {
        Datatype datatypeObject = getIpsProject().findDatatype(datatype);
        if (datatypeObject == null) {
            if (!StringUtils.isEmpty(value)) {
                String text = "The value can't be parsed because the datatype is unkown!";
                list.add(new Message("", text, Message.WARNING, this, PROPERTY_VALUE));
            }
            return;
        } 
        if (!datatypeObject.isValueDatatype()) {
            if (!StringUtils.isEmpty(datatype)) {
                String text = "The value can't be parsed because the datatype is not a value datatype!";
                list.add(new Message("", text, Message.WARNING, this, PROPERTY_VALUE));
                return;
            } 
        } 
        ValueDatatype valueDatatype = (ValueDatatype)datatypeObject;
        if (StringUtils.isNotEmpty(value)) {
            if (!valueDatatype.isParsable(value)) {
                String text = "The value " + value + " is not a " + valueDatatype.getName() + ".";
                list.add(new Message("", text, Message.ERROR, this, PROPERTY_VALUE));
                return;
            }
            if (!valueSet.contains(value, valueDatatype)) {
                list.add(new Message("", "The value " + value
                        + " is no member of the specified valueSet!", Message.ERROR, this, PROPERTY_VALUE));
            }
        }
    }

    /**
     * Overridden.
     */
    public IMethod getJavaMethod(int type) throws CoreException {
        if (type != JAVA_METHOD_COMPUTE) {
            throw new IllegalArgumentException("Unknown type");
        }
        if (getType() != ConfigElementType.FORMULA) {
            return null;
        }
        IAttribute attribute = findPcTypeAttribute();
        if (attribute == null) {
            throw new CoreException(new IpsStatus("IAttribute for config element " + this + " can't be found."));
        }
        String methodname = "compute" + StringUtils.capitalise(attribute.getName());
        IType javaType = getProductCmptGeneration().getJavaType(IProductCmptGeneration.JAVA_IMPLEMENTATION_TYPE);
        Parameter[] params = attribute.getFormulaParameters();
        String[] typeSignatures = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            Datatype datatype = getIpsProject().findDatatype(params[i].getDatatype());
            String datatypeClassname = StringUtil.unqualifiedName(datatype.getJavaClassName());
            typeSignatures[i] = Signature.createTypeSignature(datatypeClassname, false);
        }
        return javaType.getMethod(methodname, typeSignatures);
    }

    /**
     * Overridden.
     */
    public ValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Overridden.
     */
    public void setValueSet(ValueSet set) {
        ValueSet oldset = valueSet;
        valueSet = set;
        valueChanged(oldset, set);
    }

    /**
     * Overridden.
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * Overridden.
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        type = ConfigElementType.getConfigElementType(element.getAttribute(PROPERTY_TYPE));
        value = element.getAttribute(PROPERTY_VALUE);
        pcTypeAttribute = element.getAttribute(PROPERTY_PCTYPE_ATTRIBUTE);
        name = pcTypeAttribute;
        Element valueSetEl = XmlUtil.getFirstElement(element, ValueSet.XML_TAG);
        if (valueSetEl==null) {
            valueSet = ValueSet.ALL_VALUES;
        } else {
        	valueSet = ValueSet.createFromXml(valueSetEl);
        }
    }
    
    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TYPE, type.getId());
        element.setAttribute(PROPERTY_PCTYPE_ATTRIBUTE, pcTypeAttribute);
        element.setAttribute(PROPERTY_VALUE, value);
        element.appendChild(valueSet.toXml(element.getOwnerDocument()));
    }
}
