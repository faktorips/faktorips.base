/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model;

import org.faktorips.runtime.IProductComponent;
import java.util.Calendar;

/**
 * Published Interface von P.
 * 
 * @generated
 */
public interface IP extends IProductComponent {
	/**
	 * @generated
	 */
	public final static PBuilder.Factory BUILD = new PBuilder.Factory();

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
