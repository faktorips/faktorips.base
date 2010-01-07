package org.faktorips.devtools.htmlexport.generators.html.objects;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.LayouterWrapperType;
import org.faktorips.devtools.htmlexport.helper.Util;
import org.faktorips.devtools.htmlexport.pages.elements.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.RootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TableRowPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.TextType;
import org.faktorips.devtools.htmlexport.pages.elements.WrapperPageElement;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public abstract class AbstractObjectContentPageElement<T extends IIpsObject> extends RootPageElement {

	protected T object;
	protected DocumentorConfiguration config;

	public static RootPageElement getInstance(IIpsObject object, DocumentorConfiguration config) {
		if (object.getIpsObjectType() == IpsObjectType.POLICY_CMPT_TYPE)
			return new PolicyCmptContentPageElement((PolicyCmptType) object, config);
		if (object.getIpsObjectType() == IpsObjectType.PRODUCT_CMPT_TYPE)
			return new ProductCmptContentPageElement((ProductCmptType) object, config);
		throw new NotImplementedException();
	}

	protected AbstractObjectContentPageElement(T object, DocumentorConfiguration config) {
		this.object = object;
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

			TablePageElement tablePageElement = new TablePageElement();

			tablePageElement.addPageElements(new TableRowPageElement(
					new PageElement[] { new TextPageElement("code"), new TextPageElement("message"),
							new TextPageElement("severity"), new TextPageElement("properties") }));

			for (Iterator iterator = messageList.iterator(); iterator.hasNext();) {
				Message msg = (Message) iterator.next();
				int severity = msg.getSeverity();
				tablePageElement.addPageElements(new TableRowPageElement(new PageElement[] {
						new TextPageElement(msg.getCode()),
						new TextPageElement(msg.getText()),
						new TextPageElement(severity == Message.ERROR ? "ERROR"
								: severity == Message.WARNING ? "WARNING" : severity == Message.INFO ? "INFO"
										: "Severity " + severity),
						new TextPageElement(Arrays.toString(msg.getInvalidObjectProperties())) }));
			}
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
