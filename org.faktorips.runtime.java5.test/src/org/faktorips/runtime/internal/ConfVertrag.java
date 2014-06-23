package org.faktorips.runtime.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.faktorips.runtime.IDeltaComputationOptions;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectDelta;
import org.faktorips.runtime.IModelObjectVisitor;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.jaxb.ModelObjectConfigurationXmlAdapter;
import org.w3c.dom.Element;

@XmlRootElement(name = "ConfVertrag")
public class ConfVertrag extends AbstractModelObject implements IConfVertrag {

    @XmlJavaTypeAdapter(value = ModelObjectConfigurationXmlAdapter.class)
    @XmlAttribute(name = "product-component.id")
    private final ModelObjectConfiguration modelObjectConfiguration;

    public ConfVertrag() {
        super();
        modelObjectConfiguration = new ModelObjectConfiguration();
    }

    public ConfVertrag(IProductComponent productComponent) {
        super();
        modelObjectConfiguration = new ModelObjectConfiguration(productComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductComponent getProductComponent() {
        return modelObjectConfiguration.getProductComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductComponentGeneration getProductCmptGeneration() {
        return modelObjectConfiguration.getProductCmptGeneration(getEffectiveFromAsCalendar());
    }

    @Override
    public Calendar getEffectiveFromAsCalendar() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
    }

    /**
     * Sets the new product component.
     */
    public void setProductComponent(IProductComponent productComponent) {
        modelObjectConfiguration.setProductComponent(productComponent);
    }

    /**
     * TODO
     */
    public void setProductCmptGeneration(IProductComponentGeneration productCmptGeneration) {
        modelObjectConfiguration.setProductCmptGeneration(productCmptGeneration);
    }

    /**
     * This method is called when the effective from date has changed, so that the reference to the
     * product component generation can be cleared. If this policy component contains child
     * components, this method will also clear the reference to their product component generations.
     * <p>
     * The product component generation is cleared if and only if there is a new effective from
     * date. If {@link #getEffectiveFromAsCalendar()} returns <code>null</code> the product
     * component generation is not reset, for example if this model object was removed from its
     * parent.
     * <p>
     * Clients may change the behavior of resetting the product component by overwriting
     * {@link #resetProductCmptGenerationAfterEffectiveFromHasChanged()} instead of this method.
     */
    public void effectiveFromHasChanged() {
        if (getEffectiveFromAsCalendar() != null) {
            resetProductCmptGenerationAfterEffectiveFromHasChanged();
        }
    }

    protected void resetProductCmptGenerationAfterEffectiveFromHasChanged() {
        modelObjectConfiguration.setProductComponent(null);
    }

    @Override
    protected void initFromXml(Element objectEl,
            boolean initWithProductDefaultsBeforeReadingXmlData,
            IRuntimeRepository productRepository,
            IObjectReferenceStore store,
            XmlCallback xmlCallback,
            String currPath) {
        modelObjectConfiguration.initFromXml(objectEl, productRepository);
        if (initWithProductDefaultsBeforeReadingXmlData) {
            initialize();
        }
        super.initFromXml(objectEl, initWithProductDefaultsBeforeReadingXmlData, productRepository, store, xmlCallback,
                currPath);
    }

    @Override
    public IModelObjectDelta computeDelta(IModelObject otherObject, IDeltaComputationOptions options) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IModelObject newCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean accept(IModelObjectVisitor visitor) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener, boolean propagateEventsFromChildren) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasListeners(String propertyName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void notifyChangeListeners(PropertyChangeEvent event) {
        // TODO Auto-generated method stub

    }
}
