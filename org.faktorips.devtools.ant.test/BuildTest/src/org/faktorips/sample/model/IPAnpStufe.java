/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model;

import org.faktorips.sample.model.internal.PAnpStufe;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.valueset.ValueSet;
import org.faktorips.runtime.IValidationContext;

/**
 * Die Anpassungsstufe von P.
 * 
 * @generated
 */
@IpsPublishedInterface(implementation = PAnpStufe.class)
public interface IPAnpStufe extends IProductComponentGeneration {

	/**
	 * Gibt den Defaultwert fuer die Eigenschaft a zurueck.
	 * 
	 * @generated
	 */
	@IpsDefaultValue("a")
	public String getDefaultValueA();

	/**
	 * Gibt den erlaubten Wertebereich fuer das Attribut a zurueck.
	 * 
	 * @generated
	 */
	@IpsAllowedValues("a")
	public ValueSet<String> getSetOfAllowedValuesForA(IValidationContext context);

	/**
	 * Gibt P zurueck, zu dem diese Anpassungsstufe gehoert.
	 * 
	 * @generated
	 */
	public IP getP();

	/**
	 * Erzeugt und initialisiert eine neues Vertragsteil, das durch diese
	 * ProductComponentGeneration konfiguriert wird. Das neue Vertragsteil in
	 * wird keiner Struktur eingefügt.
	 * 
	 * @generated
	 */
	public IV createV();

}
