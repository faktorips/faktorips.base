/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model;

import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.sample.model.internal.P;
import org.faktorips.sample.model.internal.PAnpStufe;
import org.faktorips.runtime.internal.DateTime;
import java.util.TimeZone;

/**
 * Implementierung von PBuilder. Ein PBuilder stellt alle Funktionen bereit, die
 * für das Erstellen einer Instanz der Klasse P notwendig sind. Beachte, dass
 * diese Klasse für Tests gedacht ist. Alle Produktbausteintypen müssen hierfür
 * in einem {@link InMemoryRuntimeRepository} liegen, da die Instanzen in
 * Runtime Repository nicht verändert werden können.
 * 
 * @generated
 */
public class PBuilder {

	/**
	 * @generated
	 */
	private final InMemoryRuntimeRepository runtimeRepository;

	/**
	 * @generated
	 */
	private final P p;

	/**
	 * @generated
	 */
	private PAnpStufe currentGeneration;

	/**
	 * @generated
	 */
	protected void setCurrentAnpStufe(PAnpStufe currentGeneration) {
		this.currentGeneration = currentGeneration;
	}

	/**
	 * Erzeugt eine neue Instanz von PBuilder mit einem Produkt. Das Produkt
	 * muss im angegebenen {@link InMemoryRuntimeRepository} liegen, das selbst
	 * nicht null sein darf.
	 * 
	 * @generated
	 */
	protected PBuilder(P product, InMemoryRuntimeRepository runtimeRepository,
			PAnpStufe currentGeneration) {
		if (product == null || runtimeRepository == null) {
			throw new RuntimeException(
					"Das Produkt und Repository dürfen nicht null sein!");
		} else {
			runtimeRepository.getExistingProductComponent(product.getId());

			this.runtimeRepository = runtimeRepository;
			this.p = product;
			this.currentGeneration = currentGeneration;
		}
	}

	/**
	 * Erstellt eine neue Anpassungsstufe vom Produkt P, die ab dem gegebenen
	 * Datum gueltig ist. Falls es bereits eine Anpassungsstufe zu diesem Datum
	 * existiert, ersetzt die gespeicherte Anpassungstufe mit der
	 * Anpassungsstufe zum Bearbeiten, die dem angegebenen GueltigAb-Datum hat.
	 * 
	 * @generated
	 */
	public PBuilder anpStufe(int year, int month, int day) {
		DateTime genDate = new DateTime(year, month, day);
		PAnpStufe generation = (PAnpStufe) getRepository()
				.getProductComponentGeneration(p.getId(),
						genDate.toGregorianCalendar(TimeZone.getDefault()));

		if (generation == null || !genDate.equals(generation.getValidFrom())) {
			generation = new PAnpStufe(p);
			generation.setValidFrom(new DateTime(year, month, day));
			runtimeRepository.putProductCmptGeneration(generation);

		}
		setCurrentAnpStufe(generation);
		return this;
	}

	/**
	 * Ersetzt die gespeicherte Anpassungstufe mit der neuesten Anpassungstufe
	 * zum Produkt.
	 * 
	 * @generated
	 */
	public PBuilder latestAnpStufe() {
		setCurrentAnpStufe((PAnpStufe) getRepository()
				.getLatestProductComponentGeneration(get()));

		return this;
	}

	/**
	 * @return {@link InMemoryRuntimeRepository}, welches gespeichert wurde.
	 * 
	 * @generated
	 */
	public InMemoryRuntimeRepository getRepository() {
		return this.runtimeRepository;
	}

	/**
	 * @return Instanz von P, die gebaut wurde
	 * 
	 * @generated
	 */
	public IP get() {
		return p;

	}

	/**
	 * @return gespeicherte PAnpStufe
	 * 
	 * @generated
	 */
	public IPAnpStufe getCurrentGeneration() {
		return currentGeneration;

	}

	/**
	 * @return die neuste PAnpStufe des gebauten Produkts
	 * 
	 * @generated
	 */
	public IPAnpStufe getLatestAnpStufe() {
		return (IPAnpStufe) runtimeRepository
				.getLatestProductComponentGeneration(p);

	}

	/**
	 * Interne Methode
	 * 
	 * @generated
	 */
	public static Class<?> getProductClass() {
		return P.class;
	}

	/**
	 * Erzeugt ein neues PBuilder mit P das im gegeben Runtime Repository liegt,
	 * sowie einer Anpassungsstufe, die bearbeitet werden soll.
	 * 
	 * @generated
	 */
	public static PBuilder from(IP product,
			InMemoryRuntimeRepository runtimeRepository,
			PAnpStufe currentGeneration) {
		return new PBuilder((P) product, runtimeRepository, currentGeneration);
	}

	/**
	 * Erzeugt ein neues PBuilder mit P und einem Runtime Repository. Erzeugt
	 * ein neues {0} mit {1} das im gegeben Runtime Repository liegt, sowie der
	 * neusten Anpassungsstufe. *
	 * 
	 * @generated
	 */
	public static PBuilder from(IP product,
			InMemoryRuntimeRepository runtimeRepository) {
		return new PBuilder((P) product, runtimeRepository,
				(PAnpStufe) runtimeRepository
						.getLatestProductComponentGeneration(product));

	}

	/**
	 * Eine statische Klasse, die eine statische Methode bereitstellt um
	 * PBuilder zu erzeugen.
	 * 
	 * @generated
	 */
	public static class Factory {
		/**
		 * Erzeugt eine neue Instanz von P mit einem
		 * {@link InMemoryRuntimeRepository}, ID, kindID und versionID. Das
		 * Wirksamkeitsdatum von der neuen Instanz wird auf 1813/1/17 gesetzt.
		 * Eine neue Anpassungsstufe wird ebenfalls erzeugt zu dem Datum. *
		 * 
		 * @generated
		 */
		public PBuilder with(InMemoryRuntimeRepository runtimeRepository,
				String id, String kindId, String versionId) {
			P product = new P(runtimeRepository, id, kindId, versionId);
			product.setValidFrom(new DateTime(1900, 1, 1));
			runtimeRepository.putProductComponent(product);

			PAnpStufe generation = new PAnpStufe(product);
			generation.setValidFrom(new DateTime(1900, 1, 1));
			runtimeRepository.putProductCmptGeneration(generation);

			return new PBuilder(product, runtimeRepository, generation);

		}

		/**
		 * Erzeugt eine neue Instanz von P mit einem
		 * {@link InMemoryRuntimeRepository}, ID, kindID und versionID. Eine
		 * neue Anpassungsstufe wird ebenfalls erzeugt zu dem Datum. *
		 * 
		 * @generated
		 */
		public PBuilder with(InMemoryRuntimeRepository runtimeRepository,
				String id, String kindId, String versionId, DateTime validFrom) {
			P product = new P(runtimeRepository, id, kindId, versionId);
			product.setValidFrom(validFrom);
			runtimeRepository.putProductComponent(product);

			PAnpStufe generation = new PAnpStufe(product);
			generation.setValidFrom(validFrom);
			runtimeRepository.putProductCmptGeneration(generation);

			return new PBuilder(product, runtimeRepository, generation);

		}

		/**
		 * Erzeugt eine neue Instanz von P mit dem ID von einem existierenden
		 * Produktbaustein.
		 * 
		 * @generated
		 */
		public PBuilder with(InMemoryRuntimeRepository runtimeRepository,
				String prodCmptId) {
			P product = (P) runtimeRepository.getProductComponent(prodCmptId);

			if (product == null) {
				throw new RuntimeException(
						"Keinen Productbaustein gefunden mit dem gegebenden ID!");
			} else {
				return PBuilder.from(product, runtimeRepository);
			}
		}
	}
}
