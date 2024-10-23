/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.List;
import java.util.Optional;

import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;

/**
 * An extension of the {@link IRuntimeRepository} that allows to put files in the repository.
 *
 * @since 24.1
 */
public interface IModifiableRuntimeRepository extends IRuntimeRepository {

    /**
     * Puts the runtimeObject into the repository.
     */
    <T extends IRuntimeObject> void putCustomRuntimeObject(Class<T> type,
            String ipsObjectQualifiedName,
            T runtimeObject);

    /**
     * removes the runtimeObject from the repository.
     *
     * @param type the Java class representing the runtime object
     * @param ipsObjectQualifiedName the qualified name of the runtime object
     *
     * @return whether the given runtime object was part of this repository
     *
     * @since 25.1
     */
    <T> boolean removeCustomRuntimeObject(Class<T> type, String ipsObjectQualifiedName);

    /**
     * Puts the given enum values and description in the repository replacing all existing values
     * for the given enumType.
     *
     * @param enumTypeClass the Java class representing the enumeration type
     * @param enumValues the value of the enumeration type as list
     * @param description the description of the enumeration
     */
    <T> void putEnumValues(Class<T> enumTypeClass, List<T> enumValues, InternationalString description);

    /**
     * Puts the given enum values in the repository replacing all existing values for the given
     * enumType, removing any description.
     *
     * @see #putEnumValues(Class, List, InternationalString)
     *
     * @param enumTypeClass The Java class representing the enumeration type.
     * @param enumValues The value of the enumeration type as list.
     */
    default <T> void putEnumValues(Class<T> enumTypeClass, List<T> enumValues) {
        putEnumValues(enumTypeClass, enumValues, DefaultInternationalString.EMPTY);
    }

    /**
     * Puts the test case into the repository.
     */
    void putIpsTestCase(IpsTestCaseBase test);

    /**
     * Puts the product component generation and its product component into the repository. If the
     * repository already contains a generation with the same id, the new component replaces the old
     * one. The same applies for the product component.
     *
     * @throws IllegalRepositoryModificationException if this repository does not allows to modify
     *             its contents.
     * @throws NullPointerException if generation is {@code null}
     *
     * @see IRuntimeRepository#isModifiable()
     */
    void putProductCmptGeneration(IProductComponentGeneration generation);

    /**
     * Puts the product component into the repository. If the repository already contains a
     * component with the same id, the new component replaces the old one.
     *
     * @throws NullPointerException if productCmpt is {@code null}.
     *
     * @see IRuntimeRepository#isModifiable()
     */
    void putProductComponent(IProductComponent productCmpt);

    /**
     * Removes the product component from the repository.
     *
     * @return whether the given product component was part of this repository
     *
     * @throws NullPointerException if productCmpt is {@code null}.
     * @throws IllegalArgumentException if productCmpt has no ID.
     *
     * @see IRuntimeRepository#isModifiable()
     * @since 24.7
     */
    boolean removeProductComponent(IProductComponent productCmpt);

    /**
     * Puts the table into the repository. Replaces any table instance of the same class or any of
     * its superclasses. The latter check is needed to replace tables with mock implementations.
     *
     * @return an {@link Optional} containing the old single content table if a single content table
     *             of the same class has been replaced, or an empty {@link Optional} if a
     *             multi-content table has been added.
     *
     * @throws NullPointerException if table is {@code null} or has no {@link Table#getName() name}.
     */
    Optional<ITable<?>> putTable(ITable<?> table);

    /**
     * Removes the table from the repository.
     *
     * @return whether the given table component was part of this repository
     *
     * @throws NullPointerException if table is {@code null}.
     * @throws IllegalArgumentException if table has no name.
     */
    boolean removeTable(ITable<?> table);

    /**
     * Removes the given product component generation from the list of generations
     *
     * @param productCmptGeneration the product component generation to be removed;
     * @return {@code true} if the product component generation was successfully removed,
     *             {@code false} if the generation was not found in the list of generations.
     * @throws NullPointerException if {@code productCmptGen} is {@code null}.
     * @throws IllegalArgumentException if the associated product component of the generation has no
     *             valid ID, or if no generations are found for the product component.
     * 
     * @since 25.1
     */
    boolean removeProductCmptGeneration(IProductComponentGeneration productCmptGeneration);

    /**
     * Removes the specified IPS test case.
     *
     * @param test the IPS test case to be removed. It must not be {@code null}.
     *
     * @return {@code true} if the test case was successfully removed, {@code false} if no test case
     *             with the given test case's qualified name exists.
     *
     * @throws NullPointerException if {@code test} is {@code null}.
     * @throws IllegalArgumentException if the given test case has no qualified name.
     *
     * @since 25.1
     */
    boolean removeIpsTestCase(IpsTestCaseBase test);

}
