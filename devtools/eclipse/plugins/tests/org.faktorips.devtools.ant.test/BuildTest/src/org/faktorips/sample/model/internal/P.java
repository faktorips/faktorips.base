/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model.internal;

import org.faktorips.sample.model.IP;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.sample.model.IPAnpStufe;
import java.util.Calendar;
import org.w3c.dom.Element;
import java.util.Map;
import org.faktorips.sample.model.IV;
import org.faktorips.sample.model.PBuilder;
import org.faktorips.runtime.InMemoryRuntimeRepository;

/**
 * Implementierung von IP.
 * 
 * @generated
 */
public class P extends ProductComponent implements IP {

	/**
	 * Erzeugt eine neue Instanz von P.
	 * 
	 * @generated
	 */
	public P(IRuntimeRepository repository, String id, String kindId,
			String versionId) {
		super(repository, id, kindId, versionId);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IPAnpStufe getPAnpStufe(Calendar wirksamkeitsdatum) {
		return (IPAnpStufe) getRepository().getProductComponentGeneration(
				getId(), wirksamkeitsdatum);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public boolean isChangingOverTime() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	protected void doInitPropertiesFromXml(Map<String, Element> configMap) {
		super.doInitPropertiesFromXml(configMap);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IV createV() {
		V policy = new V(this);
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
	 * Erzeugt ein neues PBuilder um dieses Produkt zu bearbeiten.
	 * 
	 * @generated
	 */
	@Override
	public PBuilder builder() {
		return PBuilder.from(this,
				(InMemoryRuntimeRepository) this.getRepository());
	}

}
