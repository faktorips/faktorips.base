/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model;

import org.faktorips.sample.model.internal.V;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.sample.model.internal.P;
import org.faktorips.sample.model.internal.PAnpStufe;
import java.util.Calendar;

/**
 * Diese Klasse stellt Methoden bereit, mit denen eine Instanz von V erstellt
 * und bearbeitet werden kann.
 * 
 * @generated
 */
public class VBuilder {

	/**
	 * @generated
	 */
	private final V v;

	/**
	 * @generated
	 */
	private IRuntimeRepository runtimeRepository;

	/**
	 * Erzeugt eine neue Instanz von VBuilder mit einem Runtime Repository.
	 * Dieser Konstruktor ist nur für den internen Gebrauch!
	 * 
	 * @generated
	 */
	protected VBuilder(V policy, IRuntimeRepository runtimeRepository) {
		this.v = policy;
		this.runtimeRepository = runtimeRepository;
	}

	/**
	 * @generated
	 */
	public VBuilder setRepository(IRuntimeRepository runtimeRepository) {
		this.runtimeRepository = runtimeRepository;
		return this;
	}

	/**
	 * Setzt den Wert des Attributs a.
	 * 
	 * @generated
	 */
	public VBuilder a(String a) {
		v.setA(a);
		return this;
	}

	/**
	 * Gibt die Instanz von V zurück, die von diesem Builder gebaut wird.
	 * 
	 * @generated
	 */
	public IV get() {
		return v;

	}

	/**
	 * @generated
	 */
	public IRuntimeRepository getRepository() {
		return runtimeRepository;
	}

	/**
	 * 
	 * 
	 * @generated
	 */
	public static Class<?> getPolicyClass() {
		return V.class;
	}

	/**
	 * Erzeugt ein neues VBuilder mit einer Instanz von V.
	 * 
	 * @generated
	 */
	public static VBuilder from(IV policy) {
		return new VBuilder((V) policy, null);
	}

	/**
	 * Erzeugt ein neues VBuilder mit einer Instanz von V und einem Runtime
	 * Repository.
	 * 
	 * @generated
	 */
	public static VBuilder from(IV policy, IRuntimeRepository runtimeRepository) {
		return new VBuilder((V) policy, runtimeRepository);
	}

	/**
	 * Diese statische Klasse stellt Methoden bereit, mit denen VBuilders
	 * erzeugt werden können.
	 * 
	 * @generated
	 */
	public static class Factory {

		/**
		 * Erzeugt eine neue Instanz von VBuilder von einer neuen
		 * Vertragsinstanz. Runtime Repository wird null gesetzt.
		 * 
		 * @generated
		 */
		public VBuilder with() {
			return VBuilder.from(new V(), null);
		}

		/**
		 * Erzeugt eine neue Instanz von VBuilder von einer neuen
		 * Vertragsinstanz. Runtime Repository wird null gesetzt. Das Runtime
		 * Repository wird gebraucht, wenn Ziele der Assoziationen durch
		 * Produkte konfiguriert werden. Diese muessen entsprechend in diesem
		 * Runtime Repository liegen.
		 * 
		 * @generated
		 */
		public VBuilder with(IRuntimeRepository runtimeRepository) {
			return VBuilder.from(new V(), runtimeRepository);
		}

		/**
		 * Erzeugt eine neue Instanz von VBuilder mit einer neuen
		 * Vertragsinstanz, die von dem gegeben Produktbaustein erzeugt wird.
		 * 
		 * @generated
		 */
		public VBuilder with(P productCmpt) {
			return VBuilder.from(new V(productCmpt),
					productCmpt.getRepository());
		}

		/**
		 * Erzeugt eine neue Instanz von VBuilder mit einer neuen
		 * Vertragsinstanz. Diese wird vom Produktbaustein mit dem gegebenen ID
		 * erzeugt, der die Vertragsklasse konfiguriert. Die neueste
		 * Anpassungsstufe wird dabei benutzt. *
		 * 
		 * @generated
		 */
		public VBuilder with(IRuntimeRepository runtimeRepository,
				String productCmptId) {
			P product = (P) runtimeRepository
					.getProductComponent(productCmptId);
			if (product == null) {
				throw new RuntimeException(
						"Keinen Productbaustein gefunden mit dem gegebenden ID!");
			} else {
				PAnpStufe generation = (PAnpStufe) product
						.getLatestProductComponentGeneration();
				if (generation == null) {
					throw new RuntimeException(
							"Keine Anpassungsstufe gefunden, die zu dem gegebenen Datum gültig ist!");
				}
				V policy = (V) generation.createV();

				policy.initialize();
				return VBuilder.from(policy, runtimeRepository);
			}
		}

		/**
		 * Erzeugt eine neue Instanz von VBuilder mit einer neuen
		 * Vertragsinstanz. Diese wird von der Anpassungsstufe eines
		 * Produktbausteins erzeugt, die am gegebenen Datum gültig ist.
		 * 
		 * @generated
		 */
		public VBuilder with(IRuntimeRepository runtimeRepository,
				String productCmptId, Calendar validityDate) {
			P product = (P) runtimeRepository
					.getProductComponent(productCmptId);
			if (product == null) {
				throw new RuntimeException(
						"Keinen Productbaustein gefunden mit dem gegebenden ID!");
			}
			PAnpStufe generation = (PAnpStufe) product
					.getGenerationBase(validityDate);
			if (generation == null) {
				throw new RuntimeException(
						"Keine Anpassungsstufe gefunden, die zu dem gegebenen Datum gültig ist!");
			}
			V policy = (V) generation.createV();
			policy.initialize();
			return VBuilder.from(policy, runtimeRepository);
		}
	}
}
