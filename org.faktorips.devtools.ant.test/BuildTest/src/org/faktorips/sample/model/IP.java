/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model;

import org.faktorips.sample.model.internal.P;
import org.faktorips.runtime.model.annotation.IpsPublishedInterface;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.IProductComponent;
import java.util.Calendar;

/**
 * Published Interface von P.
 * 
 * @generated
 */
@IpsPublishedInterface(implementation = P.class)
@IpsProductCmptType(name = "P")
@IpsConfigures(IV.class)
@IpsChangingOverTime(IPAnpStufe.class)
@IpsDocumented(bundleName = "org.faktorips.sample.model.model-label-and-descriptions", defaultLocale = "en")
public interface IP extends IProductComponent {
	/**
	 * @generated
	 */
	public static final PBuilder.Factory BUILD = new PBuilder.Factory();

	/**
	 * Gibt die Anpassungsstufe zum uebergebenen Wirksamkeitsdatum zurueck. Gibt
	 * <code>null</code> zurueck, wenn es zu dem Datum keine gueltige
	 * Anpassungsstufe gibt.
	 * 
	 * @generated
	 */
	public IPAnpStufe getPAnpStufe(Calendar wirksamkeitsdatum);

	/**
	 * Erzeugt eine neue Instanz von V, die durch diesen Produktbaustein
	 * konfiguriert wird.
	 * 
	 * @generated
	 */
	public IV createV();

	/**
	 * Erzeugt ein neues PBuilder um dieses Produkt zu bearbeiten.
	 * 
	 * @generated
	 */
	public PBuilder builder();

}
