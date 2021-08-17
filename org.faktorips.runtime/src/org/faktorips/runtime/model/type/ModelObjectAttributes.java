/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IModelObjectVisitor;
import org.faktorips.runtime.IVisitorSupport;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.validation.Relevance;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.faktorips.values.NullObject;
import org.faktorips.values.NullObjectSupport;

/**
 * Creates and modifies {@link ModelObjectAttribute ModelObjectAttributes}.
 *
 * @since 21.6
 */
@UtilityClass
public class ModelObjectAttributes {

    private static final Predicate<ModelObjectAttribute> IS_IRRELEVANT = ModelObjectAttribute::isIrrelevant;
    private static final Predicate<ModelObjectAttribute> IS_NOT_EMPTY = ModelObjectAttribute::isValuePresent;

    /**
     * The {@link Predicate} used in {@link #resetIrrelevantAttributes(IModelObject)}. Can be
     * combined via {@link Predicate#and(Predicate) and}/{@link Predicate#or(Predicate) or} with
     * other predicates to refine the criteria passed to
     * {@link #resetAttributes(IModelObject, Predicate)}.
     */
    public static final Predicate<ModelObjectAttribute> IS_IRRELEVANT_BUT_NOT_EMPTY = IS_NOT_EMPTY.and(IS_IRRELEVANT);

    private ModelObjectAttributes() {
        // util
    }

    /**
     * Collects all {@link ModelObjectAttribute ModelObjectAttributes} of the given model object.
     */
    public static List<ModelObjectAttribute> of(IModelObject modelObject) {
        return IpsModel.getPolicyCmptType(modelObject).getModelObjectAttributes(modelObject);
    }

    /**
     * Collects all {@link ModelObjectAttribute ModelObjectAttributes} of the given model object and
     * its children.
     */
    public static List<ModelObjectAttribute> ofIncludingChildren(IModelObject modelObject) {
        List<ModelObjectAttribute> modelObjectAttributes = new ArrayList<>();
        IModelObjectVisitor propertyCollectorVisitor = mo -> {
            modelObjectAttributes.addAll(ModelObjectAttributes.of(mo));
            return true;
        };
        IVisitorSupport.orGenericVisitorSupport(modelObject).accept(propertyCollectorVisitor);
        return modelObjectAttributes;
    }

    /**
     * Clears {@link Relevance#IRRELEVANT irrelevant} attributes of a {@link IModelObject} and its
     * dependent model-objects.
     * <p>
     * If an attribute implements {@link NullObjectSupport} the {@link NullObject} is used,
     * String-attributes are set to an empty string. Otherwise {@code null} is used to clear the
     * attribute.
     * 
     * <p>
     * For each cleared attribute a {@link ModelObjectAttribute} is returned. It's
     * {@link ModelObjectAttribute#toObjectProperty()} method may be used to generate {@link Message
     * Messages} about the change.
     * 
     * @param modelObject the object to clear
     * @return a list of {@link ModelObjectAttribute ModelObjectAttributes} that were cleared
     */
    public static List<ModelObjectAttribute> resetIrrelevantAttributes(IModelObject modelObject) {
        return resetAttributes(modelObject, IS_IRRELEVANT_BUT_NOT_EMPTY);
    }

    /**
     * Clears attributes of a {@link IModelObject} and its dependent model-objects that match the
     * given {@link Predicate}.
     * <p>
     * If an attribute uses one of the {@link NullObjectSupport}-datatypes {@link Decimal} and
     * {@link Money}, the {@link NullObject} is used, {@link String}-attributes are set to an empty
     * string. Otherwise {@code null} is used to clear the attribute.
     * 
     * <p>
     * For each cleared attribute a {@link ModelObjectAttribute} is returned. It's
     * {@link ModelObjectAttribute#toObjectProperty()} method may be used to generate {@link Message
     * Messages} about the change.
     * 
     * @param modelObject the object to clear
     * @param shouldReset a Predicate deciding which attributes to reset.
     * @return a list of {@link ModelObjectAttribute ModelObjectAttributes} that were cleared
     * 
     * @see #IS_IRRELEVANT_BUT_NOT_EMPTY
     */
    public static List<ModelObjectAttribute> resetAttributes(IModelObject modelObject,
            Predicate<ModelObjectAttribute> shouldReset) {
        return ModelObjectAttributes.ofIncludingChildren(modelObject).stream()
                .filter(shouldReset::test)
                .map(ModelObjectAttribute::removeValue)
                .collect(Collectors.toList());
    }
}
