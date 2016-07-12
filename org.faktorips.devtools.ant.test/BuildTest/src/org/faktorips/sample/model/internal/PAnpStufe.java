/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model.internal;

import org.faktorips.sample.model.IPAnpStufe;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.valueset.ValueSet;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.sample.model.IP;
import org.w3c.dom.Element;
import java.util.Map;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.runtime.internal.EnumValues;
import org.faktorips.sample.model.IV;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentLink;
import java.util.List;
import java.util.ArrayList;
import org.faktorips.valueset.OrderedValueSet;

/**
 * Die Implementierung von IPAnpStufe.
 * 
 * @generated
 */
public class PAnpStufe extends ProductComponentGeneration implements IPAnpStufe {

	/**
	 * Membervariable fuer den Vorgabewert der Vertragseigenschaft a.
	 * 
	 * @generated
	 */
	private String defaultValueA = null;
	/**
	 * Instanzvariable fuer die erlaubte Wertemenge des Attributs a.
	 * 
	 * @generated
	 */
	private ValueSet<String> setOfAllowedValuesA;

	/**
	 * Erzeugt eine neue Instanz von PAnpStufe.
	 * 
	 * @generated
	 */
	public PAnpStufe(P productCmpt) {
		super(productCmpt);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public String getDefaultValueA() {
		return defaultValueA;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public ValueSet<String> getSetOfAllowedValuesForA(IValidationContext context) {
		return setOfAllowedValuesA;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IP getP() {
		return (IP) getProductComponent();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	protected void doInitPropertiesFromXml(Map<String, Element> configMap) {
		super.doInitPropertiesFromXml(configMap);
		doInitA(configMap);
	}

	/**
	 * @generated
	 */
	private void doInitA(Map<String, Element> configMap) {
		Element configElement = configMap.get(V.PROPERTY_A);
		if (configElement != null) {
			String value = ValueToXmlHelper.getValueFromElement(configElement,
					ValueToXmlHelper.XML_TAG_VALUE);
			defaultValueA = value;
			setOfAllowedValuesA = ValueToXmlHelper.getUnrestrictedValueSet(
					configElement, ValueToXmlHelper.XML_TAG_VALUE_SET);
			EnumValues values = ValueToXmlHelper.getEnumValueSetFromElement(
					configElement, ValueToXmlHelper.XML_TAG_VALUE_SET);
			if (values != null) {
				ArrayList<String> enumValues = new ArrayList<String>();
				for (int i = 0; i < values.getNumberOfValues(); i++) {
					enumValues.add(values.getValue(i));
				}
				setOfAllowedValuesA = new OrderedValueSet<String>(enumValues,
						values.containsNull(), null);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IV createV() {
		V policy = new V(getP());
		policy.setProductCmptGeneration(this);
		policy.initialize();
		return policy;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IV createPolicyComponent() {
		return createV();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IProductComponentLink<? extends IProductComponent> getLink(
			String linkName, IProductComponent target) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public List<IProductComponentLink<? extends IProductComponent>> getLinks() {
		List<IProductComponentLink<? extends IProductComponent>> list = new ArrayList<IProductComponentLink<? extends IProductComponent>>();
		return list;
	}

}
