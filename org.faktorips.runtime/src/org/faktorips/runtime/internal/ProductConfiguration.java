/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IRuntimeRepositoryLookup;
import org.w3c.dom.Element;

/**
 * Manages a product component and the corresponding generation for use with configurable model
 * objects. Used by all configurable policy component classes to manage their product configuration.
 * 
 * @serial This class is serialized using by writing/reading the product component runtime ID and
 *         the effective date of the current generation. To deserialize the product product
 *         component we also serialize the {@link IRuntimeRepositoryLookup} that is provided by the
 *         {@link IRuntimeRepository} of the current product component. The serialization would
 *         throw an {@link IllegalStateException} if the runtime repository of the current product
 *         component has no {@link IRuntimeRepositoryLookup} configured.
 * 
 */
public class ProductConfiguration implements Serializable {

    static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2579369976921239373L;

    private static final String XML_ATTRIBUTE_PRODUCT_CMPT = "productCmpt";

    /** The product component this configurable model object is based on. */
    private transient IProductComponent productCmpt;

    private transient IProductComponentGeneration productCmptGeneration;

    public ProductConfiguration() {
        super();
    }

    public ProductConfiguration(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
    }

    /**
     * Sets the new product component.
     */
    public void setProductComponent(IProductComponent productCmpt) {
        this.productCmpt = productCmpt;
        this.productCmptGeneration = null;
    }

    /**
     * Sets the product component generation <strong>null</strong>.
     */
    public void resetProductCmptGeneration() {
        productCmptGeneration = null;
    }

    /**
     * Returns the product component that configures a policy component.
     */
    public IProductComponent getProductComponent() {
        return productCmpt;
    }

    /**
     * Returns the product component generation that configures a policy component.
     */
    public IProductComponentGeneration getProductCmptGeneration(Calendar effectiveFrom) {
        if (productCmpt == null) {
            return null;
        } else {
            return getProductCmptGenerationInternal(effectiveFrom);
        }
    }

    private IProductComponentGeneration getProductCmptGenerationInternal(Calendar effectiveFrom) {
        if (productCmptGeneration == null) {
            productCmptGeneration = loadProductCmptGeneration(effectiveFrom);
        }
        return productCmptGeneration;
    }

    /**
     * Gets the product component generation valid from the given date.
     */
    private IProductComponentGeneration loadProductCmptGeneration(Calendar effectiveFrom) {
        return productCmpt.getGenerationBase(effectiveFrom);
    }

    /**
     * Sets the new product component generation. Also changes the product component. If the
     * argument is <code>null</code> however, both product component and product component
     * generation are set to <code>null</code>.
     */
    public void setProductCmptGeneration(IProductComponentGeneration newGeneration) {
        if (newGeneration == null) {
            setProductComponent(null);
        } else {
            updateProductCmptAndGeneration(newGeneration);
        }
    }

    private void updateProductCmptAndGeneration(IProductComponentGeneration newGeneration) {
        setProductComponent(newGeneration.getProductComponent());
        productCmptGeneration = newGeneration;
    }

    /**
     * Copies the product component and product component generation from the other object.
     */
    public void copy(ProductConfiguration otherObject) {
        this.productCmpt = otherObject.productCmpt;
        this.productCmptGeneration = otherObject.productCmptGeneration;
    }

    /**
     * Loads the product component this {@link ProductConfiguration} manages. Does nothing, if no
     * product component qualified name can be found in the XML element.
     * 
     * @param objectEl the XML element containing the product component qualified name
     * @param productRepository the {@link IRuntimeRepository} to load the product component from
     */
    public void initFromXml(Element objectEl, IRuntimeRepository productRepository) {
        String productCmptId = objectEl.getAttribute(XML_ATTRIBUTE_PRODUCT_CMPT);
        if (!IpsStringUtils.isEmpty(productCmptId)) {
            IProductComponent existingProductCmpt = productRepository.getExistingProductComponent(productCmptId);
            setProductComponent(existingProductCmpt);
        }
    }

    /**
     * Serialize this {@link ProductConfiguration} instance.
     * 
     * @throws IllegalStateException if there is no {@link IRuntimeRepositoryLookup} configured in
     *             the product component's runtime repository.
     * 
     * @serialData First the runtime ID of the product component followed by the effective date of
     *             the current generation. At least the {@link IRuntimeRepositoryLookup} that is
     *             retrieved by the {@link IRuntimeRepository} of the product component.
     * 
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(getRuntimeId());
        s.writeObject(getValidFrom());
        s.writeObject(getRepositoryLookup());
    }

    private String getRuntimeId() {
        if (productCmpt != null) {
            return productCmpt.getId();
        } else {
            return null;
        }
    }

    private Date getValidFrom() {
        if (productCmptGeneration != null) {
            return productCmptGeneration.getValidFrom(TIME_ZONE);
        } else {
            return null;
        }
    }

    private IRuntimeRepositoryLookup getRepositoryLookup() {
        if (productCmpt != null) {
            IRuntimeRepositoryLookup runtimeRepositoryLookup = productCmpt.getRepository().getRuntimeRepositoryLookup();
            checkRuntimeRepositoryLookup(runtimeRepositoryLookup);
            return runtimeRepositoryLookup;
        } else {
            return null;
        }
    }

    private void checkRuntimeRepositoryLookup(IRuntimeRepositoryLookup runtimeRepositoryLookup) {
        if (runtimeRepositoryLookup == null) {
            throw new IllegalStateException(
                    "For serialization of policy component classes you need to set a IRuntimeRepositoryLookup in your runtime repository.");
        }
    }

    /**
     * Reads a serialized instance of {@link ProductConfiguration}.
     * 
     * @serialData First the runtime ID of the product component followed by the effective date of
     *             the current generation. At least the {@link IRuntimeRepositoryLookup} that is
     *             retrieved by the {@link IRuntimeRepository} of the product component.
     * 
     */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        String runtimeId = (String)s.readObject();
        Date validFromDate = (Date)s.readObject();
        IRuntimeRepositoryLookup runtimeRepositoryLookup = (IRuntimeRepositoryLookup)s.readObject();
        if (runtimeRepositoryLookup != null) {
            initProductConfiguration(runtimeId, validFromDate, runtimeRepositoryLookup);
        }
    }

    private void initProductConfiguration(String runtimeId,
            Date validFromDate,
            IRuntimeRepositoryLookup runtimeRepositoryLookup) {
        IRuntimeRepository runtimeRepository = runtimeRepositoryLookup.getRuntimeRepository();
        initProductCmpt(runtimeId, runtimeRepository);
        initGeneration(runtimeId, validFromDate, runtimeRepository);
    }

    private void initProductCmpt(String runtimeId, IRuntimeRepository runtimeRepository) {
        if (runtimeId != null) {
            productCmpt = runtimeRepository.getProductComponent(runtimeId);
        }
    }

    private void initGeneration(String runtimeId, Date validFromDate, IRuntimeRepository runtimeRepository) {
        if (validFromDate != null) {
            Calendar validFrom = Calendar.getInstance(TIME_ZONE);
            validFrom.setTime(validFromDate);
            productCmptGeneration = runtimeRepository.getProductComponentGeneration(runtimeId, validFrom);
        }
    }

}
