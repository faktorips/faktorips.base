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

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.caching.AbstractComputable;
import org.faktorips.runtime.caching.Memoizer;
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

    private static final Memoizer<Class<? extends ITable>, TableModel> TABLE_MODEL_CACHE = new Memoizer<Class<? extends ITable>, TableModel>(
            new AbstractComputable<Class<? extends ITable>, TableModel>(TableModel.class) {

                @Override
                public TableModel compute(Class<? extends ITable> tableObjectClass) {
                    return new TableModel(tableObjectClass);
                }
            });

    private static final Memoizer<AnnotatedType, IProductModel> PRODUCT_MODEL_CACHE = new Memoizer<AnnotatedType, IProductModel>(
            new AbstractComputable<AnnotatedType, IProductModel>(IProductModel.class) {
                @Override
                public IProductModel compute(AnnotatedType annotatedModelType) {
                    if (annotatedModelType.is(IpsProductCmptType.class)) {
                        String name = annotatedModelType.get(IpsProductCmptType.class).name();
                        return new ProductModel(name, annotatedModelType);
                    } else {
                        throw new IllegalArgumentException("The class " + getAnnotatedClass(annotatedModelType)
                                + " is not annotated as product component type.");
                    }
                }
            });

    private static final Memoizer<AnnotatedType, IPolicyModel> POLICY_MODEL_CACHE = new Memoizer<AnnotatedType, IPolicyModel>(
            new AbstractComputable<AnnotatedType, IPolicyModel>(IPolicyModel.class) {
                @Override
                public IPolicyModel compute(AnnotatedType annotatedModelType) {
                    if (annotatedModelType.is(IpsPolicyCmptType.class)) {
                        String name = annotatedModelType.get(IpsPolicyCmptType.class).name();
                        return new PolicyModel(name, annotatedModelType);
                    } else {
                        throw new IllegalArgumentException("The class " + getAnnotatedClass(annotatedModelType)
                                + " is not annotated as policy component type.");
                    }
                }
            });

    private Models() {
        // prevent default constructor
    }

    private static String getAnnotatedClass(AnnotatedType annotatedModelType) {
        return annotatedModelType.getPublishedInterface() != null ? annotatedModelType.getPublishedInterface()
                .getCanonicalName() : annotatedModelType.getImplementationClass().getCanonicalName();
    }

    private static <K, V> V get(Memoizer<K, V> memoizer, K key) {
        try {
            return memoizer.compute(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * @return a {@link TableModel} describing the type and columns of the given {@link ITable}
     *         class
     */
    public static TableModel getTableModel(Class<? extends ITable> tableObjectClass) {
        return get(TABLE_MODEL_CACHE, tableObjectClass);
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
     * @param productModelClass The class that may be a the implementation or the published
     *            interface of an {@link IProductComponent}.
     * @return <code>true</code> if the given class is a product model class
     */
    public static boolean isProductModel(Class<?> productModelClass) {
        return AnnotatedType.from(productModelClass).is(IpsProductCmptType.class);
    }

    /**
     * @param productModelClass The generated class for a product component type, may be either an
     *            implementation class of a published interface.
     * @return the product model object for the given product model class
     * @throws IllegalArgumentException if the given class is not properly annotated for a product
     *             model
     */
    public static IProductModel getProductModel(Class<? extends IProductComponent> productModelClass) {
        return get(PRODUCT_MODEL_CACHE, AnnotatedType.from(productModelClass));
    }

    /**
     * @return the product model object for the given product component
     * @throws IllegalArgumentException if the class of the given product component is not properly
     *             annotated for a product model
     */
    public static IProductModel getProductModel(IProductComponent productComponent) {
        return getProductModel(productComponent.getClass());
    }

    /**
     * Returns whether the given class is a generated policy component type and could be given to
     * {@link #getPolicyModel(Class)} as argument without getting an
     * {@link IllegalArgumentException}.
     * 
     * @param policyModelClass The class that may be a the implementation or the published interface
     *            of an {@link IModelObject}.
     * @return <code>true</code> if the given class is a policy model class
     */
    public static boolean isPolicyModel(Class<?> policyModelClass) {
        return AnnotatedType.from(policyModelClass).is(IpsPolicyCmptType.class);
    }

    /**
     * @param policyModelClass The generated class for a policy component type, may be either an
     *            implementation class of a published interface.
     * @return the policy model object for the given policy model class
     * @throws IllegalArgumentException if the given class is not properly annotated for a policy
     *             model
     */
    public static IPolicyModel getPolicyModel(Class<? extends IModelObject> policyModelClass) {
        return get(POLICY_MODEL_CACHE, AnnotatedType.from(policyModelClass));
    }

    /**
     * @return the policy model object for the given model object
     * @throws IllegalArgumentException if the class of the model object is not properly annotated
     *             for a policy model
     */
    public static IPolicyModel getPolicyModel(IModelObject modelObject) {
        return getPolicyModel(modelObject.getClass());
    }

    /**
     * @return the model object for the given policy or product model class. This is either the
     *         implementation or the published interface of a product component type or policy
     *         component type.
     * @throws IllegalArgumentException if the given class is not properly annotated for a model
     *             type
     */
    public static IModelType getModelType(Class<?> modelObjectClass) {
        AnnotatedType annotatedModelType = AnnotatedType.from(modelObjectClass);
        if (annotatedModelType.is(IpsProductCmptType.class)) {
            return getProductModel(modelObjectClass.asSubclass(IProductComponent.class));
        } else if (annotatedModelType.is(IpsPolicyCmptType.class)) {
            return getPolicyModel(modelObjectClass.asSubclass(IModelObject.class));
        } else {
            throw new IllegalArgumentException("The given " + modelObjectClass
                    + " is not annotated as product or policy component type.");
        }
    }

}
