/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model;

import org.faktorips.sample.model.internal.V;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsAttributes;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.ITimedConfigurableModelObject;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IVisitorSupport;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.valueset.ValueSet;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.AttributeType;
import org.faktorips.runtime.modeltype.IModelTypeAttribute.ValueSetType;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsAttributeSetter;
import org.faktorips.runtime.IRuntimeRepository;

/**
 * Published Interface von V.
 * 
 * @generated
 */
@IpsPublishedInterface(implementation = V.class)
@IpsPolicyCmptType(name = "V")
@IpsAttributes({ "a" })
@IpsConfiguredBy(IP.class)
@IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
public interface IV extends ITimedConfigurableModelObject, IDeltaSupport,
		ICopySupport, IVisitorSupport {
	/**
	 * @generated
	 */
	public static final VBuilder.Factory BUILD = new VBuilder.Factory();

	/**
	 * Diese Konstante enthaelt den Namen der Eigenschaft a.
	 * 
	 * @generated
	 */
	public static final String PROPERTY_A = "a";

	/**
	 * Gibt den erlaubten Wertebereich fuer das Attribut a zurueck.
	 * 
	 * @generated
	 */
	@IpsAllowedValues("a")
	public ValueSet<String> getSetOfAllowedValuesForA(IValidationContext context);

	/**
	 * Gibt den Wert des Attributs a zurueck.
	 * 
	 * @generated
	 */
	@IpsAttribute(name = "a", type = AttributeType.CHANGEABLE, valueSetType = ValueSetType.AllValues)
	@IpsConfiguredAttribute(changingOverTime = true)
	public String getA();

	/**
	 * Setzt den Wert des Attributs a.
	 * 
	 * @generated
	 */
	@IpsAttributeSetter("a")
	public void setA(String newValue);

	/**
	 * Gibt P zurueck, welches V konfiguriert.
	 * 
	 * @generated
	 */
	public IP getP();

	/**
	 * Setzt neuen P.
	 * 
	 * @param p
	 *            Der neue P.
	 * @param initPropertiesWithConfiguratedDefaults
	 *            <code>true</code> falls die Eigenschaften mit den Defaultwerten
	 *            aus P belegt werden sollen.
	 * 
	 * @generated
	 */
	public void setP(IP p, boolean initPropertiesWithConfiguratedDefaults);

	/**
	 * Gibt Anpassungsstufe zurueck, welches V konfiguriert. Anpassungsstufe
	 * wird anhand des Wirksamkeitsdatum ermittelt.
	 * 
	 * @generated
	 */
	public IPAnpStufe getPAnpStufe();

	/**
	 * Erzeugt ein neues VBuilder um diesen Vertrag zu bearbeiten.
	 * 
	 * @generated
	 */
	public VBuilder builder();

	/**
	 * Das Runtime Repository wird benutzt um konfigurierten Ziele von
	 * Assoziationen mit Hilfe eines bestehenden Produkts zu erstellen.
	 * 
	 * @generated
	 */
	public VBuilder builder(IRuntimeRepository runtimeRepository);
}
