/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.caching.Memoizer;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.faktorips.runtime.model.annotation.IpsTableStructure;
import org.faktorips.runtime.model.enumtype.EnumType;
import org.faktorips.runtime.model.table.TableStructure;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ProductCmptType;
import org.faktorips.runtime.model.type.Type;

/**
 * Repository of Faktor-IPS model information. This class should be used to obtain model instances
 * from runtime classes or their instances instead of using the constructors. By caching model
 * information, this class operates more efficiently if model information is retrieved repeatedly.
 */
@UtilityClass
public enum IpsModel {
    /* no instances */;

    private static final Memoizer<Class<? extends ITable<?>>, TableStructure> TABLE_MODEL_CACHE = Memoizer
            .of(TableStructure.class, tableObjectClass -> {
                if (tableObjectClass.isAnnotationPresent(IpsTableStructure.class)) {
                    return new TableStructure(tableObjectClass);
                } else {
                    throw new IllegalArgumentException(
                            "The class " + tableObjectClass.getName() + "is not annotated as IpsTableStructure.");
                }
            });

    private static final Memoizer<Class<?>, EnumType> ENUM_MODEL_CACHE = Memoizer.of(EnumType.class,
            enumObjectClass -> {
                if (enumObjectClass.isAnnotationPresent(IpsEnumType.class)) {
                    return new EnumType(enumObjectClass);
                } else {
                    throw new IllegalArgumentException(
                            "The class " + enumObjectClass.getName() + " is not annotated as IpsEnumType.");
                }
            });

    private static final Memoizer<AnnotatedDeclaration, ProductCmptType> PRODUCT_MODEL_CACHE = Memoizer
            .of(ProductCmptType.class, annotatedDeclaration -> {
                if (annotatedDeclaration.is(IpsProductCmptType.class)) {
                    String name = annotatedDeclaration.get(IpsProductCmptType.class).name();
                    return new ProductCmptType(name, annotatedDeclaration);
                } else {
                    throw new IllegalArgumentException("The class " + annotatedDeclaration.getDeclarationClassName()
                            + " is not annotated as product component type.");
                }
            });

    private static final Memoizer<AnnotatedDeclaration, PolicyCmptType> POLICY_MODEL_CACHE = Memoizer
            .of(PolicyCmptType.class, annotatedModelType -> {
                if (annotatedModelType.is(IpsPolicyCmptType.class)) {
                    String name = annotatedModelType.get(IpsPolicyCmptType.class).name();
                    return new PolicyCmptType(name, annotatedModelType);
                } else {
                    throw new IllegalArgumentException("The class " + annotatedModelType.getDeclarationClassName()
                            + " is not annotated as policy component type.");
                }
            });

    private static <K, V> V get(Memoizer<K, V> memoizer, K key) {
        try {
            return memoizer.compute(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * @param tableObjectClass a generated subclass of {@link ITable}
     * @return a {@link TableStructure} describing the type and columns of the given {@link ITable}
     *         class
     */
    public static TableStructure getTableStructure(Class<? extends ITable<?>> tableObjectClass) {
        return get(TABLE_MODEL_CACHE, tableObjectClass);
    }

    /**
     * @return a {@link TableStructure} describing the type and columns of the given {@link ITable}
     */
    @SuppressWarnings("unchecked")
    public static TableStructure getTableStructure(ITable<?> table) {
        return getTableStructure((Class<? extends ITable<?>>)table.getClass());
    }

    /**
     * Returns whether the given class is a generated product type and can be given to
     * {@link #getProductCmptType(Class)} as an argument without getting an
     * {@link IllegalArgumentException}.
     * 
     * @param productModelClass the class to check
     * @return <code>true</code> if the given class is a product model class
     */
    public static boolean isProductCmptType(Class<?> productModelClass) {
        return AnnotatedDeclaration.from(productModelClass).is(IpsProductCmptType.class);
    }

    /**
     * @param productModelClass The generated class for a product component type, may be either an
     *            implementation class of a published interface.
     * @return the product model object for the given product model class
     * @throws IllegalArgumentException if the given class is not a valid model type
     * 
     * @see #isProductCmptType(Class)
     */
    public static ProductCmptType getProductCmptType(Class<? extends IProductComponent> productModelClass) {
        return get(PRODUCT_MODEL_CACHE, AnnotatedDeclaration.from(productModelClass));
    }

    /**
     * @return the product model object for the given product component
     * @throws IllegalArgumentException if the class of the given product component is not properly
     *             annotated for a product model
     */
    public static ProductCmptType getProductCmptType(IProductComponent productComponent) {
        return getProductCmptType(productComponent.getClass());
    }

    /**
     * Returns whether the given class is a generated policy type and can be given to
     * {@link #getPolicyCmptType(Class)} as an argument without getting an
     * {@link IllegalArgumentException}.
     * 
     * @param policyModelClass the class to check
     * @return <code>true</code> if the given class is a policy model class
     */
    public static boolean isPolicyCmptType(Class<?> policyModelClass) {
        return AnnotatedDeclaration.from(policyModelClass).is(IpsPolicyCmptType.class);
    }

    /**
     * @param policyModelClass The generated class for a policy component type, may be either an
     *            implementation class of a published interface.
     * @return the policy model object for the given policy model class
     * @throws IllegalArgumentException if the given class is not a valid model type
     * 
     * @see #isPolicyCmptType(Class)
     */
    public static PolicyCmptType getPolicyCmptType(Class<? extends IModelObject> policyModelClass) {
        return get(POLICY_MODEL_CACHE, AnnotatedDeclaration.from(policyModelClass));
    }

    /**
     * @return the policy model object for the given model object
     * @throws IllegalArgumentException if the class of the model object is not properly annotated
     *             for a policy model
     */
    public static PolicyCmptType getPolicyCmptType(IModelObject modelObject) {
        return getPolicyCmptType(modelObject.getClass());
    }

    /**
     * @return the model object for the given policy or product model class. This is either the
     *         implementation or the published interface of a product component type or policy
     *         component type.
     * @throws IllegalArgumentException if the given class is not properly annotated for a model
     *             type
     */
    public static Type getType(Class<?> modelObjectClass) {
        AnnotatedDeclaration annotatedModelType = AnnotatedDeclaration.from(modelObjectClass);
        if (annotatedModelType.is(IpsProductCmptType.class)) {
            return getProductCmptType(modelObjectClass.asSubclass(IProductComponent.class));
        } else if (annotatedModelType.is(IpsPolicyCmptType.class)) {
            return getPolicyCmptType(modelObjectClass.asSubclass(IModelObject.class));
        } else {
            throw new IllegalArgumentException(
                    "The given " + modelObjectClass + " is not annotated as product or policy component type.");
        }
    }

    /**
     * Returns whether the given class is a generated enum type and can be given to
     * {@link #getEnumType(Class)} as an argument without getting an
     * {@link IllegalArgumentException}.
     * 
     * @param enumObjectClass the class to check
     * @return <code>true</code> if the given class is an enum model class
     */
    public static boolean isEnumType(Class<?> enumObjectClass) {
        return AnnotatedDeclaration.from(enumObjectClass).is(IpsEnumType.class);
    }

    /**
     * @param enumObjectClass a generated Faktor-IPS enum class
     * @return an {@link EnumType} describing the attributes of the given Faktor-IPS enum
     * @throws IllegalArgumentException if the given class is not a valid model type
     * 
     * @see #isEnumType(Class)
     */
    public static EnumType getEnumType(Class<?> enumObjectClass) {
        return get(ENUM_MODEL_CACHE, enumObjectClass);
    }

    /**
     * @return an {@link EnumType} describing the attributes of the given Faktor-IPS enum.
     * @throws IllegalArgumentException if the given object's class is not properly annotated for a
     *             model type
     */
    public static EnumType getEnumType(Object enumInstance) {
        if (enumInstance instanceof Enum) {
            return getEnumType(((Enum<?>)enumInstance).getDeclaringClass());
        } else {
            return getEnumType(enumInstance.getClass());
        }
    }

}
