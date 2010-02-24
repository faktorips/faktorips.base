package org.faktorips.devtools.htmlexport.pages.standard;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.WrapperType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;

public class EnumContentContentPageElement extends AbstractObjectContentPageElement<IEnumContent> {

	protected IEnumType enumType;

	protected EnumContentContentPageElement(IEnumContent object, DocumentorConfiguration config) {
		super(object, config);
		try {
			enumType = object.getIpsProject().findEnumType(object.getEnumType());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void build() {
		super.build();

		// EnumType
		addPageElements(new WrapperPageElement(WrapperType.BLOCK)
				.addPageElements(new TextPageElement("Aufzählungstyp: ")).addPageElements(
						new LinkPageElement(enumType, "content", enumType.getQualifiedName(), true)));
		
		// Werte
		addPageElements(createValuesTable());
	}

	protected PageElement createValuesTable() {
		WrapperPageElement wrapper = new WrapperPageElement(WrapperType.BLOCK);
		wrapper.addPageElements(new TextPageElement("Werte", TextType.HEADING_2));

		wrapper.addPageElements(getTableOrAlternativeText(new EnumValuesTablePageElement(object), "keine Werte"));

		return wrapper;
	}

}
