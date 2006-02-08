package org.faktorips.devtools.core.builder;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;

/**
 * An identifier resolver that resolves identifiers against a set of
 * <code>Parameter</code>s that can be registered via the <code>add()</code>
 * methods.
 */
public abstract class AbstractParameterIdentifierResolver implements
		IParameterIdentifierResolver {

	private IIpsProject project;

	private Parameter[] params = new Parameter[0];

	/**
	 * Provides the name of the getter method for the provided attribute.
	 */
	protected abstract String getParameterAttributGetterName(
			IAttribute attribute, Datatype datatype);

	public void setIpsProject(IIpsProject ipsProject) {
		ArgumentCheck.notNull(ipsProject);
		this.project = ipsProject;
	}

	public void setParameters(Parameter[] parameters) {
		ArgumentCheck.notNull(parameters);
		this.params = parameters;
	}

	/**
	 * Overridden method.
	 * 
	 * @see org.faktorips.fl.IdentifierResolver#compile(java.lang.String,
	 *      java.util.Locale)
	 */
	public CompilationResult compile(String identifier, Locale locale) {

		if (project == null) {
			throw new IllegalStateException(
					"The ipsproject needs to be set to this resolver before this method can be called.");
		}

		String paramName;
		String attributeName;
		int pos = identifier.indexOf('.');
		if (pos == -1) {
			paramName = identifier;
			attributeName = "";
		} else {
			paramName = identifier.substring(0, pos);
			attributeName = identifier.substring(pos + 1);
		}
		for (int i = 0; i < params.length; i++) {
			if (params[i].getName().equals(paramName)) {
				return compile(params[i], attributeName, locale);
			}
		}
		return CompilationResultImpl.newResultUndefinedIdentifier(locale,
				identifier);
	}

	private CompilationResult compile(Parameter param, String attributeName,
			Locale locale) {
		Datatype datatype;
		try {
			datatype = project.findDatatype(param.getDatatype());
			if (datatype == null) {
				String text = "The datatype " + param.getDatatype()
						+ "of parameter " + param.getName()
						+ " can't be resolved!";
				return new CompilationResultImpl(Message.newError(
						ExprCompiler.UNDEFINED_IDENTIFIER, text));
			}
		} catch (Exception e) {
			IpsPlugin.log(e);
			String text = "An error occured while resolving the datatype "
					+ param.getDatatype() + " of parameter " + param.getName()
					+ ".";
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.INTERNAL_ERROR, text));
		}
		if (datatype instanceof IPolicyCmptType) {
			return compilePcTypeAttributeIdentifier(param,
					(IPolicyCmptType) datatype, attributeName, locale);
		}
		if (datatype instanceof ValueDatatype) {
			return new CompilationResultImpl(param.getName(), datatype);
		}
		throw new RuntimeException("Unkown datatype class "
				+ datatype.getClass());
	}

	private CompilationResult compilePcTypeAttributeIdentifier(Parameter param,
			IPolicyCmptType pcType, String attributeName, Locale locale) {

		IAttribute attribute = null;
		try {
			attribute = pcType.getSupertypeHierarchy().findAttribute(pcType,
					attributeName);
		} catch (CoreException e) {
			IpsPlugin.log(e);
			String text = "An error occured while trying to retrieve the attribute "
					+ attributeName
					+ " from the policy component type "
					+ pcType + " ";
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.INTERNAL_ERROR, text));
		}
		if (attribute == null) {
			String text = "The parameter " + param.getName() + " of class "
					+ pcType.getName() + " has not attribute " + attributeName;
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.UNDEFINED_IDENTIFIER, text));
		}

		try {
			Datatype datatype = attribute.getIpsProject().findDatatype(
					attribute.getDatatype());
			if (datatype == null) {
				String text = "The datatype " + attribute.getDatatype()
						+ "of attribute " + attributeName
						+ " can't be resolved!";
				return new CompilationResultImpl(Message.newError(
						ExprCompiler.UNDEFINED_IDENTIFIER, text));
			}
			String code = param.getName() + '.'
					+ getParameterAttributGetterName(attribute, datatype) + "()";
			// attribute.getJavaMethod(
			// IAttribute.JAVA_GETTER_METHOD_IMPLEMENATION)
			// .getElementName() + "()";
			return new CompilationResultImpl(code, datatype);
		} catch (Exception e) {
			IpsPlugin.log(e);
			String text = "An error occured while resolving the datatype "
					+ attribute.getDatatype() + " of attribute "
					+ attributeName + ".";
			return new CompilationResultImpl(Message.newError(
					ExprCompiler.INTERNAL_ERROR, text));
		}
	}

}
