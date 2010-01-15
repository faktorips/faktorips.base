package org.faktorips.devtools.htmlexport.pages.standard;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.core.WrapperPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.types.MessageListTablePageElement;
import org.faktorips.util.message.MessageList;

public abstract class AbstractObjectContentPageElement<T extends IIpsObject> extends RootPageElement {

	protected T object;
	protected DocumentorConfiguration config;

	public static RootPageElement getInstance(IIpsObject object, DocumentorConfiguration config) {
		if (object.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE)
			return new PolicyCmptTypeContentPageElement((IPolicyCmptType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE)
			return new ProductCmptTypeContentPageElement((IProductCmptType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT)
			return new ProductCmptContentPageElement((IProductCmpt) object, config);
		throw new NotImplementedException();
	}

	protected AbstractObjectContentPageElement(T object, DocumentorConfiguration config) {
		this.object = object;
		this.config = config;
		setTitle(object.getName());
	}

	@Override
	public void build() {
		super.build();
		addPageElements(new LinkPageElement(object, object.getIpsPackageFragment(), "classes", new TextPageElement(Util
				.getIpsPackageName(object.getIpsPackageFragment()))));
		addPageElements(new TextPageElement(object.getName(), TextType.HEADING_1));

		// Typhierarchie
		addTypeHierarchie();

		// Strukturdaten
		addPageElements(new TextPageElement(object.getName(), TextType.HEADING_2));
		addStructureData();
		addPageElements(new TextPageElement("Projektverzeichnis: " + object.getIpsSrcFile().getIpsPackageFragment()));

		// Beschreibung
		addPageElements(new TextPageElement("Beschreibung", TextType.HEADING_2));
		addPageElements(new TextPageElement(
				StringUtils.isBlank(object.getDescription()) ? "keine Beschreibung vorhanden" : object.getDescription(),
				TextType.BLOCK));

		// Validations
		addValidationErrors();
	}

	private void addValidationErrors() {
		try {

			MessageList messageList = object.validate(object.getIpsProject());
			if (messageList.isEmpty())
				return;

			WrapperPageElement wrapper = new WrapperPageElement(LayouterWrapperType.BLOCK);
			wrapper.addPageElements(new TextPageElement("Validation Errors", TextType.HEADING_2));

			TablePageElement tablePageElement = new MessageListTablePageElement(messageList);

			wrapper.addPageElements(tablePageElement);

			addPageElements(wrapper);

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/*
	 * zum Ueberschreiben fuer Subklassen
	 */
	protected void addStructureData() {
	}

	/*
	 * zum Ueberschreiben fuer Subklassen
	 */
	protected void addTypeHierarchie() {
	}
}
