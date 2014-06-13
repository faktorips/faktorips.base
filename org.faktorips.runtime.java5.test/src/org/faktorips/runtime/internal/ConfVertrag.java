package org.faktorips.runtime.internal;

import java.util.Calendar;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.jaxb.ModelObjectConfigurationXmlAdapter;
import org.w3c.dom.Element;

public class ConfVertrag extends AbstractModelObject implements IConfigurableModelObject {

    @XmlJavaTypeAdapter(value = ModelObjectConfigurationXmlAdapter.class)
    @XmlAttribute(name = "product-component.id")
    private ModelObjectConfiguration modelObjectConfiguration;

    public ConfVertrag() {
    }

    public ConfVertrag(ModelObjectConfiguration modelObjectConfiguration) {
        this.modelObjectConfiguration = modelObjectConfiguration;
    }

    @Override
    public IProductComponent getProductComponent() {
        return modelObjectConfiguration.getProductComponent();
    }

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

    @Override
    public void setProductComponent(IProductComponent productComponent) {
        modelObjectConfiguration.setProductComponent(productComponent);
    }

    @Override
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
    @Override
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

}
