package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.faktorips.devtools.core.internal.model.type.Method;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Represents a table with the {@link Method}s of an {@link IType} as rows and the attributes of the method as columns
 * @author dicker
 *
 */
public class MethodsTablePageElement extends AbstractSpecificTablePageElement {
	protected IType type;

	/**
	 * Creates a {@link MethodsTablePageElement} for the specified {@link IType}
	 * @param type
	 */
	public MethodsTablePageElement(IType type) {
		super();
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#addDataRows()
	 */
	@Override
	protected void addDataRows() {
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			addMethodRow(method);
		}

	}

	/**
	 * adds a row for the given method
	 * @param method
	 */
	protected void addMethodRow(IMethod method) {
		addSubElement(new TableRowPageElement(PageElementUtils.createTextPageElements(getMethodData(method))));
	}

	/**
	 * returns the method's data
	 * @param method
	 * @return
	 */
	protected List<String> getMethodData(IMethod method) {
		List<String> methodData = new ArrayList<String>();

		methodData.add(method.getName());
		methodData.add(method.isAbstract() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
		methodData.add(method.getModifier().getName());
		methodData.add(method.getDatatype());
		methodData.add(method.getSignatureString());
		methodData.add(method.getDescription());

		return methodData;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#getHeadline()
	 */
	@Override
	protected List<String> getHeadline() {
		ArrayList<String> headline = new ArrayList<String>();

		headline.add(Messages.MethodsTablePageElement_headlineName);
		headline.add(Messages.MethodsTablePageElement_headlineAbstract);
		headline.add(Messages.MethodsTablePageElement_headlineModifier);
		headline.add(Messages.MethodsTablePageElement_headlineDatatype);
		headline.add(Messages.MethodsTablePageElement_headlineSignature);
		headline.add(Messages.MethodsTablePageElement_headlineDescription);

		return headline;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement#isEmpty()
	 */
	public boolean isEmpty() {
		return ArrayUtils.isEmpty(type.getMethods());
	}

}
