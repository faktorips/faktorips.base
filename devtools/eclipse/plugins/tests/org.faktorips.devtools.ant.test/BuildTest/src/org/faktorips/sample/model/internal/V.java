/* BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
package org.faktorips.sample.model.internal;

import org.faktorips.sample.model.IV;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.ProductConfiguration;
import org.faktorips.sample.model.IP;
import org.faktorips.valueset.ValueSet;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.sample.model.IPAnpStufe;
import org.faktorips.runtime.IProductComponentGeneration;
import java.util.Calendar;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.internal.XmlCallback;
import org.w3c.dom.Element;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.internal.ModelObjectDelta;
import java.util.Map;
import java.util.HashMap;
import org.faktorips.runtime.IModelObjectVisitor;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.sample.model.VBuilder;

/**
 * Implementierung von IV.
 * 
 * @generated
 */
public class V extends AbstractModelObject implements IV {

	/**
	 * Membervariable fuer a.
	 * 
	 * @generated
	 */
	private String a = null;
	/**
	 * Haelt eine Referenz auf die aktuell eingestellte Produktkonfiguration.
	 * 
	 * @generated
	 */
	private ProductConfiguration productConfiguration;

	/**
	 * Erzeugt eine neue Instanz von V.
	 * 
	 * @generated
	 */
	public V() {
		super();
		productConfiguration = new ProductConfiguration();
	}

	/**
	 * Erzeugt eine neue Instanz von V.
	 * 
	 * @generated
	 */
	public V(IP productCmpt) {
		super();
		productConfiguration = new ProductConfiguration(productCmpt);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public ValueSet<String> getSetOfAllowedValuesForA(IValidationContext context) {
		return getPAnpStufe().getSetOfAllowedValuesForA(context);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public String getA() {
		return a;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public void setA(String newValue) {
		this.a = newValue;
	}

	/**
	 * Initialisiert Attribute mit ihren Vorgabewerten.
	 * 
	 * @restrainedmodifiable
	 */
	@Override
	public void initialize() {
		if (getPAnpStufe() != null) {
			setA(getPAnpStufe().getDefaultValueA());
		}
		// begin-user-code
		// end-user-code
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
	public void setP(IP p, boolean initPropertiesWithConfiguratedDefaults) {
		setProductComponent(p);
		if (initPropertiesWithConfiguratedDefaults) {
			initialize();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IProductComponent getProductComponent() {
		return productConfiguration.getProductComponent();
	}

	/**
	 * Setzt die aktuelle ProductComponent.
	 * 
	 * @generated
	 */
	public void setProductComponent(IProductComponent productComponent) {
		productConfiguration.setProductComponent(productComponent);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IPAnpStufe getPAnpStufe() {
		return (IPAnpStufe) getProductCmptGeneration();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IProductComponentGeneration getProductCmptGeneration() {
		return productConfiguration
				.getProductCmptGeneration(getEffectiveFromAsCalendar());
	}

	/**
	 * Setzt die aktuelle ProductComponentGeneration.
	 * 
	 * @generated
	 */
	public void setProductCmptGeneration(
			IProductComponentGeneration productComponentGeneration) {
		productConfiguration
				.setProductCmptGeneration(productComponentGeneration);
	}

	/**
	 * Diese Methode wird aufgerufen, wenn sich das Wirksamkeitsdatum aendert
	 * und somit die Referenz zur aktuellen Anpassungsstufe nicht mehr gilt.
	 * Wenn dieser Vertragsteil andere Kindkomponenten enthaelt, entfernt diese
	 * Methode ebenfalls die Referenz zur deren Anpassungsstufe.
	 * <p>
	 * Die Anpassungsstufe wird nur entfernt, wenn ein neues Wirksamkeitsdatum
	 * existiert. Wenn {@link #getEffectiveFromAsCalendar()} <code>null</code>
	 * zurueck liefert, wird die Anpassungsstuffe nicht entfernt. Z.B wenn
	 * dieses Model-Objekt von seinem Elternteil entfernt wurde.
	 * <p>
	 * Ableitungen koennen das Verhalten durch Ueberschreiben der Methode
	 * {@link #resetProductCmptGenerationAfterEffectiveFromHasChanged()}
	 * aendern.
	 * 
	 * @generated
	 */
	public void effectiveFromHasChanged() {
		if (getEffectiveFromAsCalendar() != null) {
			resetProductCmptGenerationAfterEffectiveFromHasChanged();
		}
	}

	/**
	 * Setzt die ProductComponentGeneration zurueck.
	 * <p>
	 * Die Methode kann ueberschrieben werden, um das Verhalten bei Aenderung
	 * des Wirksamkeitsdatums zu beeinflussen.
	 * 
	 * @generated
	 */
	protected void resetProductCmptGenerationAfterEffectiveFromHasChanged() {
		productConfiguration.resetProductCmptGeneration();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public Calendar getEffectiveFromAsCalendar() {
		// TODO Implementieren des Zugriffs auf das Wirksamkeitsdatum (wird
		// benoetigt um auf die gueltigen Produktdaten zuzugreifen).
		// Damit diese Methode bei erneutem Generieren nicht neu ueberschrieben
		// wird,
		// muss im Javadoc ein NOT hinter @generated geschrieben werden!
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	protected void initFromXml(Element objectEl,
			boolean initWithProductDefaultsBeforeReadingXmlData,
			IRuntimeRepository productRepository, IObjectReferenceStore store,
			XmlCallback xmlCallback, String currPath) {
		productConfiguration.initFromXml(objectEl, productRepository);
		if (initWithProductDefaultsBeforeReadingXmlData) {
			initialize();
		}
		super.initFromXml(objectEl,
				initWithProductDefaultsBeforeReadingXmlData, productRepository,
				store, xmlCallback, currPath);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	protected void initPropertiesFromXml(Map<String, String> propMap,
			IRuntimeRepository productRepository) {
		super.initPropertiesFromXml(propMap, productRepository);
		doInitA(propMap);
	}

	/**
	 * @generated
	 */
	private void doInitA(Map<String, String> propMap) {
		if (propMap.containsKey(PROPERTY_A)) {
			this.a = propMap.get(PROPERTY_A);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	protected AbstractModelObject createChildFromXml(Element childEl) {
		AbstractModelObject newChild = super.createChildFromXml(childEl);
		if (newChild != null) {
			return newChild;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IModelObjectDelta computeDelta(IModelObject otherObject,
			IDeltaComputationOptions options) {
		ModelObjectDelta delta = ModelObjectDelta.newDelta(this, otherObject,
				options);
		if (!V.class.isAssignableFrom(otherObject.getClass())) {
			return delta;
		}
		V otherV = (V) otherObject;
		delta.checkPropertyChange(IV.PROPERTY_A, a, otherV.a, options);
		return delta;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public IModelObject newCopy() {
		Map<IModelObject, IModelObject> copyMap = new HashMap<IModelObject, IModelObject>();
		V newCopy = (V) newCopyInternal(copyMap);
		copyAssociationsInternal(newCopy, copyMap);
		return newCopy;
	}

	/**
	 * Interne Kopiermethode mit einer {@link Map} der bisher kopierten
	 * Instanzen
	 * 
	 * @param copyMap
	 *            die Map enthaelt die bisher kopierten Instanzen.
	 * 
	 * @generated
	 */
	public IModelObject newCopyInternal(Map<IModelObject, IModelObject> copyMap) {
		V newCopy = (V) copyMap.get(this);
		if (newCopy == null) {
			newCopy = new V();
			newCopy.copyProductCmptAndGenerationInternal(this);
			copyProperties(newCopy, copyMap);
		}
		return newCopy;
	}

	/**
	 * Kopiert den Produktbaustein und die Generation aus dem referenzierten
	 * Objekt.
	 * 
	 * @generated
	 */
	protected void copyProductCmptAndGenerationInternal(V otherObject) {
		productConfiguration.copy(otherObject.productConfiguration);
	}

	/**
	 * Diese Methode setzt alle Werte in der Kopie (copy) auf die Werte aus
	 * diesem Objekt. Kopierte Assoziationen werden zur copyMap hinzugefügt.
	 * 
	 * @param copy
	 *            Das kopierte Object
	 * @param copyMap
	 *            Eine Map mit kopierten assoziierten Objekten
	 * 
	 * @generated
	 */
	protected void copyProperties(IModelObject copy,
			Map<IModelObject, IModelObject> copyMap) {
		V concreteCopy = (V) copy;
		concreteCopy.a = a;
	}

	/**
	 * Interne Methode zum setzen kopierter Assoziationen. Wenn das Ziel der
	 * Assoziation kopiert wurde, wird die Assoziation auf die neue Kopie
	 * gesetzt, ansonsten bleibt die Assoziation unveraendert. Die Methode ruft
	 * ausserdem {@link #copyAssociationsInternal(IModelObject, Map)} in allen
	 * durch Komposition verknuepften Instanzen auf.
	 * 
	 * @param abstractCopy
	 *            die Kopie dieser PolicyCmpt
	 * @param copyMap
	 *            die Map mit den kopierten Instanzen
	 * 
	 * @generated
	 */
	public void copyAssociationsInternal(IModelObject abstractCopy,
			Map<IModelObject, IModelObject> copyMap) {
		// Keine Implementierung notwendig.
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @generated
	 */
	@Override
	public boolean accept(IModelObjectVisitor visitor) {
		if (!visitor.visit(this)) {
			return false;
		}
		return true;
	}

	/**
	 * Validierung von Objekten der Klasse V. Gibt <code>true</code> zurueck,
	 * wenn dieses Objekt mit der Validierung fortfahren soll,
	 * <code>false</code> sonst.
	 * 
	 * @generated
	 */
	@Override
	public boolean validateSelf(MessageList ml, IValidationContext context) {
		if (!super.validateSelf(ml, context)) {
			return STOP_VALIDATION;
		}
		return CONTINUE_VALIDATION;
	}

	/**
	 * Validierung von abhaengigen Objekten fuer Instanzen der Klasse V.
	 * 
	 * @generated
	 */
	@Override
	public void validateDependants(MessageList ml, IValidationContext context) {
		super.validateDependants(ml, context);
	}

	/**
	 * Erzeugt ein neues VBuilder um diesen Vertrag zu bearbeiten.
	 * 
	 * @generated
	 */
	@Override
	public VBuilder builder() {
		return VBuilder.from(this, getProductComponent().getRepository());
	}

	/**
	 * Das Runtime Repository wird benutzt um konfigurierten Ziele von
	 * Assoziationen mit Hilfe eines bestehenden Produkts zu erstellen.
	 * 
	 * @generated
	 */
	@Override
	public VBuilder builder(IRuntimeRepository runtimeRepository) {
		return VBuilder.from(this, runtimeRepository);
	}
}
