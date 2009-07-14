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

package org.faktorips.devtools.core.builder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 * An identifier resolver that resolves identifiers against a set of
 * <code>Parameter</code>s that can be registered via the <code>add()</code>
 * methods.
 */
public abstract class AbstractParameterIdentifierResolver implements IdentifierResolver{

	private IIpsProject ipsproject;
    private IFormula formula;
    private ExprCompiler exprCompiler;

    public AbstractParameterIdentifierResolver(IFormula formula, ExprCompiler exprCompiler) throws CoreException{
        ArgumentCheck.notNull(formula, this);
        ArgumentCheck.notNull(exprCompiler, this);
        this.formula = formula;
        this.exprCompiler = exprCompiler;
        ipsproject = formula.getIpsProject();
    }
    
    private IParameter[] getParameters() {
        try {
            return formula.findFormulaSignature(ipsproject).getParameters();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return new IParameter[0];
    }
    
    private IProductCmptType getProductCmptType() throws CoreException{
        return formula.findProductCmptType(ipsproject);
    }
    
	/**
	 * Provides the name of the getter method for the provided attribute.
	 */
	protected abstract String getParameterAttributGetterName(
			IAttribute attribute, Datatype datatype);

    private Map createEnumMap() throws CoreException{
        EnumDatatype[] enumtypes = formula.getEnumDatatypesAllowedInFormula();
        Map enumDatatypes = new HashMap(enumtypes.length);
        for (int i = 0; i < enumtypes.length; i++) {
            enumDatatypes.put(enumtypes[i].getName(), enumtypes[i]);
        }
        return enumDatatypes;
    }
    
    /**
     * {@inheritDoc}
	 */
	public CompilationResult compile(String identifier, ExprCompiler exprCompiler, Locale locale) {

		if (ipsproject == null) {
			throw new IllegalStateException(
					Messages.AbstractParameterIdentifierResolver_msgResolverMustBeSet);
		}

		String paramName;
		String attributeName;
		int pos = identifier.indexOf('.');
		if (pos == -1) {
			paramName = identifier;
			attributeName = ""; //$NON-NLS-1$
		} else {
			paramName = identifier.substring(0, pos);
			attributeName = identifier.substring(pos + 1);
		}
        IParameter[] params = getParameters();
		for (int i = 0; i < params.length; i++) {
			if (params[i].getName().equals(paramName)) {
				return compile(params[i], attributeName, locale);
			}
		}
		
        //assuming that the identifier is an attribute of the product component type
        //where the formula method is defined.
        CompilationResult result = compileThis(identifier);
        if(result != null){
            return result;
        }
		result = compileEnumDatatypeValueIdentifier(paramName, attributeName, locale);
		if(result != null){
			return result;
		}
		return CompilationResultImpl.newResultUndefinedIdentifier(locale,
				identifier);
	}

    private CompilationResult compileThis(String identifier){
        IProductCmptType productCmptType = null;
        try {
            productCmptType = getProductCmptType();
            IAttribute[] attributes = productCmptType.findAllAttributes(ipsproject);
            for (int i = 0; i < attributes.length; i++) {
                if(attributes[i].getName().equals(identifier)){
                    Datatype attrDatatype = attributes[i].findDatatype(ipsproject);
                    if(attrDatatype == null){
                        String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgNoDatatypeForProductCmptTypeAttribute, attributes[i].getName(), productCmptType.getQualifiedName());
                        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
                    }
                    String code = getParameterAttributGetterName(attributes[i], productCmptType) + "()"; //$NON-NLS-1$
                    return new CompilationResultImpl(code, attrDatatype);
                }
            }
        } catch (CoreException e) {
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgExceptionWhileResolvingIdentifierAtThis, identifier, productCmptType.getQualifiedName());
            IpsPlugin.log(new IpsStatus(text, e));
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
        }
        return null;
    }
    
    /**
     * Returns the compilation result for the a parameter and attribute name
     * @see #compile(String, Locale)
     */
	protected CompilationResult compile(IParameter param, String attributeName, Locale locale) {
        Datatype datatype;
        try {
            datatype = param.findDatatype(ipsproject);
            if (datatype == null) {
                String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved, param
                        .getDatatype(), param.getName());
                return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorParameterDatatypeResolving,
                    param.getDatatype(), param.getName());
            return new CompilationResultImpl(Message.newError(ExprCompiler.INTERNAL_ERROR, text));
        }
        if (datatype instanceof IType) {
            return compileTypeAttributeIdentifier(param, (IType)datatype, attributeName, locale);
        }
        if (datatype instanceof ValueDatatype) {
            return new CompilationResultImpl(param.getName(), datatype);
        }
        throw new RuntimeException("Unknown datatype class " //$NON-NLS-1$
                + datatype.getClass());
    }

    /**
     * Since the generation of a new instance statement of an {@link IEnumType} needs information
     * about the code generation the implementation is postponed to the generation implementation.
     * By default this method is an empty implementation.
     * 
     * @param fragment the {@link JavaCodeFragment} to add the new instance expression for the provided
     *          {@link IEnumType}
     * @throws CoreException 
     */
	protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
	        EnumTypeDatatypeAdapter enumType,
            ExprCompiler exprCompiler,
            String value) throws CoreException {
    }
	
	private CompilationResult compileEnumDatatypeValueIdentifier(
			String enumTypeName, String valueName, Locale locale) {
		
		try {
            Map enumDatatypes = createEnumMap();
            EnumDatatype enumType = (EnumDatatype)enumDatatypes.get(enumTypeName);
            if(enumType == null){
                return null;
            }
			String[] valueIds = enumType.getAllValueIds(true);
			for (int i = 0; i < valueIds.length; i++) {
			    String enumValueName = valueIds[i];
                if (ObjectUtils.equals(enumValueName, valueName)) {
                    JavaCodeFragment frag = new JavaCodeFragment();
                    frag.getImportDeclaration().add(enumType.getJavaClassName());
                    if(enumType instanceof EnumTypeDatatypeAdapter){
                        addNewInstanceForEnumType(frag, (EnumTypeDatatypeAdapter)enumType, exprCompiler, valueIds[i]);
                    } else {
                        DatatypeHelper helper = ipsproject.getDatatypeHelper(enumType);
                        frag.append(helper.newInstance(valueIds[i]));
                    }
                    return new CompilationResultImpl(frag, enumType);
                }
            }
		} catch (Exception e) {
			IpsPlugin.log(e);
			String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorDuringEnumDatatypeResolving, enumTypeName);
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.INTERNAL_ERROR, text));
		}
		return null;
	}

	private CompilationResult compileTypeAttributeIdentifier(IParameter param,
			IType type, String attributeName, Locale locale) {

        if (StringUtils.isEmpty(attributeName)) {
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, Messages.AbstractParameterIdentifierResolver_msgAttributeMissing));
        }
        
		IAttribute attribute = null;
		try {
			attribute = type.findAttribute(attributeName, ipsproject);
		} catch (CoreException e) {
			IpsPlugin.log(e);
			String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorRetrievingAttribute, attributeName, type);
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.INTERNAL_ERROR, text));
		}
		if (attribute == null) {
			String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoAttribute, new Object[] {param.getName(), type.getName(), attributeName});
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.UNDEFINED_IDENTIFIER, text));
		}

		try {
			Datatype datatype = attribute.findDatatype(ipsproject);
			if (datatype == null) {
				String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoDatatypeForAttribute, attribute.getDatatype(), attributeName);
				return new CompilationResultImpl(Message.newError(
						ExprCompiler.UNDEFINED_IDENTIFIER, text));
			}
			String code = param.getName() + '.'
					+ getParameterAttributGetterName(attribute, type)
					+ "()"; //$NON-NLS-1$
			return new CompilationResultImpl(code, datatype);
		} catch (Exception e) {
			IpsPlugin.log(e);
			String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoDatatypeForAttribute, attribute.getDatatype(), attributeName);
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.INTERNAL_ERROR, text));
		}
	}
    
}
