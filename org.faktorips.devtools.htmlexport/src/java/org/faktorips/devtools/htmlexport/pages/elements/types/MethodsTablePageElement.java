package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TableRowPageElement;

public class MethodsTablePageElement extends AbstractSpecificTablePageElement {
	protected IType type;

	public MethodsTablePageElement(IType type) {
		super();
		this.type = type;
	}

	@Override
	protected void addDataRows() {
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			addMethodRow(method);
		}

	}

	protected void addMethodRow(IMethod method) {
		subElements.add(new TableRowPageElement(PageElementUtils.createTextPageElements(getMethodData(method))));
	}

	protected List<String> getMethodData(IMethod method) {
		List<String> methodData = new ArrayList<String>();

		methodData.add(method.getName());
		methodData.add(method.isAbstract() ? "X" : "-");
		methodData.add(method.getModifier().getName());
		methodData.add(method.getDatatype());
		methodData.add(method.getSignatureString());
		methodData.add(method.getDescription());

		return methodData;
	}

	@Override
	protected List<String> getHeadline() {
		ArrayList<String> headline = new ArrayList<String>();

		headline.add(IMethod.PROPERTY_NAME);
		headline.add(IMethod.PROPERTY_ABSTRACT);
		headline.add(IMethod.PROPERTY_MODIFIER);
		headline.add(IMethod.PROPERTY_DATATYPE);
		headline.add("signature");
		headline.add(IMethod.PROPERTY_DESCRIPTION);
		
		return headline;
	}

}
