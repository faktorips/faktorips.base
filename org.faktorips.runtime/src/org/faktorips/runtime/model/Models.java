/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.annotation.AnnotatedType;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.table.TableModel;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.internal.PolicyModel;
import org.faktorips.runtime.modeltype.internal.ProductModel;

/**
 * Repository of Faktor-IPS model information. This class should be used to obtain model instances
 * from runtime classes or their instances instead of using the constructors. By caching model
 * information, this class operates more efficiently if model information is retrieved repeatedly.
 */
public class Models {

    private static final Map<Class<?>, TableModel> TABLE_MODEL_CACHE = new HashMap<Class<?>, TableModel>();

    private Models() {
        // prevent default constructor
    }

    /**
     * @return a {@link TableModel} describing the type and columns of the given {@link ITable}
     *         class
     */
    public static TableModel getTableModel(Class<? extends ITable> tableObjectClass) {
        TableModel tm = TABLE_MODEL_CACHE.get(tableObjectClass);
        if (tm == null) {
            synchronized (TABLE_MODEL_CACHE) {
                tm = TABLE_MODEL_CACHE.get(tableObjectClass);
                if (tm == null) {
                    tm = new TableModel(tableObjectClass);
                    TABLE_MODEL_CACHE.put(tableObjectClass, tm);
                }
            }
        }
        return tm;
    }

    /**
     * @return a {@link TableModel} describing the type and columns of the given {@link ITable}
     */
    public static TableModel getTableModel(ITable table) {
        return getTableModel(table.getClass());
    }

    /**
     * Returns whether the given class is a generated product type and could be given to
     * {@link #getProductModel(Class)} as argument without getting an
     * {@link IllegalArgumentException}.
     * 
     * @param productModelClass The class that may be a product model class
     * @return <code>true</code> if the given class is a product model class
     */
    public static boolean isProductModel(Class<?> productModelClass) {
        return AnnotatedType.from(productModelClass).is(IpsProductCmptType.class);
    }

    /**
     * Retrieves the product model object for the given product component class.
     * 
     * @param productComponentClass The qualified class name of the generated class of model type
     *            (interface or implementation)
     * 
     * @throws IllegalArgumentException if the given class is not proper annotated for a product
     *             component model
     */
    public static IProductModel getProductModel(Class<? extends IProductComponent> productComponentClass) {
        AnnotatedType annotatedModelType = AnnotatedType.from(productComponentClass);
        return getProductModel(productComponentClass, annotatedModelType);
    }

    private static IProductModel getProductModel(Class<? extends IProductComponent> productComponentClass,
            AnnotatedType annotatedModelType) {
        if (annotatedModelType.is(IpsProductCmptType.class)) {
            String name = annotatedModelType.get(IpsProductCmptType.class).name();
            return new ProductModel(name, annotatedModelType);
        } else {
            throw new IllegalArgumentException("The class " + productComponentClass.getCanonicalName()
                    + " is not annotated as product component type.");
        }
    }

    /**
     * Returns whether the given class is a generated policy type and could be given to
     * {@link #getPolicyModel(Class)} as argument without getting an
     * {@link IllegalArgumentException}.
     * 
     * @param modelObjectClass The class that may be a policy model class
     * @return <code>true</code> if the given class is a policy model class
     */
    public static boolean isPolicyModel(Class<?> modelObjectClass) {
        return AnnotatedType.from(modelObjectClass).is(IpsPolicyCmptType.class);
    }

    /**
     * Retrieves the policy model object for the given product component class.
     * 
     * @param modelObjectClass The qualified class name of the generated class of model type
     *            (interface or implementation)
     * 
     * @throws IllegalArgumentException if the given class is not proper annotated for a policy
     *             component model
     */
    public static IPolicyModel getPolicyModel(Class<? extends IModelObject> modelObjectClass) {
        AnnotatedType annotatedModelType = AnnotatedType.from(modelObjectClass);
        return getPolicyModel(modelObjectClass, annotatedModelType);
    }

    private static IPolicyModel getPolicyModel(Class<? extends IModelObject> modelObjectClass,
            AnnotatedType annotatedModelType) {
        if (annotatedModelType.is(IpsPolicyCmptType.class)) {
            String name = annotatedModelType.get(IpsPolicyCmptType.class).name();
            return new PolicyModel(name, annotatedModelType);
        } else {
            throw new IllegalArgumentException("The class " + modelObjectClass.getCanonicalName()
                    + " is not annotated as policy component type.");
        }
    }

    /**
     * Retrieves the model object for the given product component class. This is either a product
     * component type or policy component type.
     * 
     * @param modelObjectClass The qualified class name of the generated class of model type
     *            (interface or implementation)
     * 
     * @throws IllegalArgumentException if the given class is not proper annotated for a model type
     */
    public static IModelType getModelType(Class<?> modelObjectClass) {
        AnnotatedType annotatedModelType = AnnotatedType.from(modelObjectClass);
        if (annotatedModelType.is(IpsProductCmptType.class)) {
            return getProductModel(modelObjectClass.asSubclass(IProductComponent.class), annotatedModelType);
        } else if (annotatedModelType.is(IpsPolicyCmptType.class)) {
            return getPolicyModel(modelObjectClass.asSubclass(IModelObject.class), annotatedModelType);
        } else {
            throw new IllegalArgumentException("The given class is not annotated as product or policy component type.");
        }
    }

}
