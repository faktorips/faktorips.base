/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.testrepository;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IObjectReferenceStore;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.ModelObjectConfiguration;
import org.faktorips.runtime.internal.XmlCallback;
import org.w3c.dom.Element;

/**
 * @author Jan Ortmann
 */
public class PnCPolicy extends AbstractModelObject implements IConfigurableModelObject {

    private final ModelObjectConfiguration modelObjectConfiguration;
    private final Calendar effectiveFrom = new GregorianCalendar();

    public PnCPolicy() {
        super();
        modelObjectConfiguration = new ModelObjectConfiguration();
    }

    protected PnCPolicy(PnCProduct pc) {
        super();
        modelObjectConfiguration = new ModelObjectConfiguration(pc);
    }

    @Override
    public MessageList validate(IValidationContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IProductComponent getProductComponent() {
        return modelObjectConfiguration.getProductComponent();
    }

    @Override
    public IProductComponentGeneration getProductCmptGeneration() {
        return modelObjectConfiguration.getProductCmptGeneration(getEffectiveFromAsCalendar());
    }

    /**
     * Sets the new product component.
     */
    public void setProductComponent(IProductComponent productComponent) {
        modelObjectConfiguration.setProductComponent(productComponent);
    }

    /**
     * Sets the new product component generation.
     */
    public void setProductCmptGeneration(IProductComponentGeneration productCmptGeneration) {
        modelObjectConfiguration.setProductCmptGeneration(productCmptGeneration);
    }

    @Override
    public Calendar getEffectiveFromAsCalendar() {
        return effectiveFrom;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub
    }

    public void effectiveFromHasChanged() {
        if (getEffectiveFromAsCalendar() != null) {
            resetProductCmptGenerationAfterEffectiveFromHasChanged();
        }
    }

    protected void resetProductCmptGenerationAfterEffectiveFromHasChanged() {
        modelObjectConfiguration.resetProductCmptGeneration();
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
