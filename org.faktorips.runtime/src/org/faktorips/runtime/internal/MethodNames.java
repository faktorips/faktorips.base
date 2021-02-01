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

import org.faktorips.runtime.internal.delta.ChildDeltaCreator;

/**
 * Gives access to the method names defined in the runtime that the generated code refers to, e.g.
 * by implementing a method with the given name. The reference to the actual method is documented in
 * the Javadoc "see" tags.
 * <p>
 * The constants are used by the code generator.
 * 
 * @author Jan Ortmann
 */
public enum MethodNames {
    /* no instances */;

    /**
     * @see org.faktorips.runtime.IConfigurableModelObject#getEffectiveFromAsCalendar()
     */
    public static final String GET_EFFECTIVE_FROM_AS_CALENDAR = "getEffectiveFromAsCalendar";

    /**
     * Method is generated in the first product configured policy class in hierarchy.
     */
    public static final String EFFECTIVE_FROM_HAS_CHANGED = "effectiveFromHasChanged";

    /**
     * @see org.faktorips.runtime.IProductComponent#getId()
     */
    public static final String GET_PRODUCT_COMPONENT_ID = "getId";

    /**
     * @see org.faktorips.runtime.IConfigurableModelObject#getProductComponent()
     */
    public static final String GET_PRODUCT_COMPONENT = "getProductComponent";

    /**
     * Method is generated in the first product configured policy class in hierarchy.
     */
    public static final String SET_PRODUCT_COMPONENT = "setProductComponent";

    /**
     * Method is generated in the first product configured policy class in hierarchy.
     */
    public static final String SET_PRODUCT_CMPT_GENERATION = "setProductCmptGeneration";

    /**
     * @see org.faktorips.runtime.IRuntimeRepository#getExistingProductComponent(String)
     */
    public static final String GET_EXISTING_PRODUCT_COMPONENT = "getExistingProductComponent";

    /**
     * @see org.faktorips.runtime.IProductComponent#createPolicyComponent()
     */
    public static final String CREATE_POLICY_COMPONENT = "createPolicyComponent";

    /**
     * @see ProductComponent#getRepository()
     * @see ProductComponentGeneration#getRepository()
     * @see org.faktorips.runtime.test.IpsTestCase2#getRepository()
     */
    public static final String GET_REPOSITORY = "getRepository";

    /**
     * Use this instead of {@link #GET_REPOSITORY} when the method is in this class. This is
     * important if the method is generated for formulas. If the formula is evaluated e.g. by
     * groovy, we convert the keyword 'this' to a special variable to call the method on the correct
     * object.
     * 
     * @see #GET_REPOSITORY
     */
    public static final String GET_THIS_REPOSITORY = "this." + GET_REPOSITORY;

    /**
     * @see org.faktorips.runtime.IRuntimeRepository#isModifiable()
     */
    public static final String IS_MODIFIABLE = "isModifiable";

    /**
     * @see org.faktorips.runtime.IDependantObject#getParentModelObject()
     */
    public static final String GET_PARENT = "getParentModelObject";

    /**
     * @see AbstractModelObject#removeChildModelObjectInternal(org.faktorips.runtime.IModelObject)
     */
    public static final String REMOVE_CHILD_MODEL_OBJECT_INTERNAL = "removeChildModelObjectInternal";

    /**
     * @see org.faktorips.runtime.ITimedConfigurableModelObject#getProductCmptGeneration()
     */
    public static final String GET_PRODUCT_CMPT_GENERATION = "getProductCmptGeneration";

    /**
     * Method is generated in the first product configured policy class in hierarchy.
     */
    public static final String COPY_PRODUCT_CMPT_AND_GENERATION_INTERNAL = "copyProductCmptAndGenerationInternal";

    /**
     * @see org.faktorips.runtime.IRuntimeRepository#getExistingProductComponentGeneration(String,
     *      java.util.Calendar)
     */
    public static final String GET_EXISTING_PRODUCT_COMPONENT_GENERATION = "getExistingProductComponentGeneration";

    /**
     * @see AbstractModelObject#initPropertiesFromXml(java.util.Map,
     *      org.faktorips.runtime.IRuntimeRepository)
     */
    public static final String INIT_PROPERTIES_FROM_XML = "initPropertiesFromXml";

    /**
     * @see AbstractModelObject#createChildFromXml(org.w3c.dom.Element)
     */
    public static final String CREATE_CHILD_FROM_XML = "createChildFromXml";

    /**
     * @see AbstractModelObject#createUnresolvedReference(Object, String, String)
     */
    public static final String CREATE_UNRESOLVED_REFERENCE = "createUnresolvedReference";

    /**
     * notifyChangeListeners(PropertyChangeEvent)
     */
    public static final String NOTIFIY_CHANGE_LISTENERS = "notifyChangeListeners";

    /**
     * @see ProductComponentGeneration#getValidFrom(java.util.TimeZone)
     */
    public static final String GET_VALID_FROM = "getValidFrom";

    /**
     * @see org.faktorips.runtime.IDeltaSupport#computeDelta(org.faktorips.runtime.IModelObject,
     *      org.faktorips.runtime.IDeltaComputationOptions)
     */
    public static final String COMPUTE_DELTA = "computeDelta";

    /**
     * @see org.faktorips.runtime.ICopySupport#newCopy
     */
    public static final String NEW_COPY = "newCopy";

    /**
     * @see org.faktorips.runtime.internal.ModelObjectDelta#checkPropertyChange(String, Object,
     *      Object, org.faktorips.runtime.IDeltaComputationOptions)
     * @see org.faktorips.runtime.internal.ModelObjectDelta#checkPropertyChange(String, int, int,
     *      org.faktorips.runtime.IDeltaComputationOptions)
     * @see org.faktorips.runtime.internal.ModelObjectDelta#checkPropertyChange(String, boolean,
     *      boolean, org.faktorips.runtime.IDeltaComputationOptions)
     * @see org.faktorips.runtime.internal.ModelObjectDelta#checkPropertyChange(String, double,
     *      double, org.faktorips.runtime.IDeltaComputationOptions)
     * @see org.faktorips.runtime.internal.ModelObjectDelta#checkPropertyChange(String, float,
     *      float, org.faktorips.runtime.IDeltaComputationOptions)
     * @see org.faktorips.runtime.internal.ModelObjectDelta#checkPropertyChange(String, char, char,
     *      org.faktorips.runtime.IDeltaComputationOptions)
     */
    public static final String MODELOBJECTDELTA_CHECK_PROPERTY_CHANGE = "checkPropertyChange";

    /**
     * @see ChildDeltaCreator
     */
    public static final String MODELOBJECTDELTA_CREATE_CHILD_DELTAS = "createChildDeltas";

    /**
     * @see ModelObjectDelta#newEmptyDelta(org.faktorips.runtime.IModelObject,
     *      org.faktorips.runtime.IModelObject)
     */
    public static final String MODELOBJECTDELTA_NEW_EMPTY_DELTA = "newEmptyDelta";

    /**
     * @see ModelObjectDelta#newDelta(org.faktorips.runtime.IModelObject,
     *      org.faktorips.runtime.IModelObject, org.faktorips.runtime.IDeltaComputationOptions)
     */
    public static final String MODELOBJECTDELTA_NEW_DELTA = "newDelta";

    /**
     * @see org.faktorips.runtime.IVisitorSupport#accept
     */
    public static final String ACCEPT_VISITOR = "accept";

    /**
     * @see org.faktorips.runtime.IModelObjectVisitor#visit(org.faktorips.runtime.IModelObject)
     */
    public static final String VISITOR_VISIT = "visit";

    /**
     * @see org.faktorips.runtime.IProductComponent#isChangingOverTime()
     */
    public static final String IS_CHANGING_OVER_TIME = "isChangingOverTime";

    public static final String METHOD_NEW_COPY = "newCopyInternal";

    public static final String METHOD_COPY_ASSOCIATIONS = "copyAssociationsInternal";

    public static final String VALIDATION_CONTEXT_GET_LOCALE = "getLocale";

    public static final String MESSAGE_HELPER_GET_MESSAGE = "getMessage";
}
