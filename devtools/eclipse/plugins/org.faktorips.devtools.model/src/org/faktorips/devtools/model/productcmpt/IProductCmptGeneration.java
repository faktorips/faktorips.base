/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.List;

import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;

public interface IProductCmptGeneration extends IIpsObjectGeneration, IPropertyValueContainer,
        IProductCmptLinkContainer, IValidationRuleConfigContainer {

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "PRODUCTCMPTGEN-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the template for the product this generation is for
     * could not be found.
     */
    String MSGCODE_NO_TEMPLATE = MSGCODE_PREFIX + "NoTemplate"; //$NON-NLS-1$

    /**
     * Returns the product component this generation belongs to.
     */
    @Override
    IProductCmpt getProductCmpt();

    /**
     * Searches the product component type this product component generation is based on.
     * 
     * @param ipsProject The IPS project which search path is used to search the type.
     * 
     * @return The product component type this product component generation is based on or
     *             {@code null} if the product component type can't be found.
     * 
     * @throws NullPointerException if ipsProject is {@code null}.
     */
    @Override
    IProductCmptType findProductCmptType(IIpsProject ipsProject);

    /**
     * Returns the number of attribute values defined in the generation.
     */
    int getNumOfAttributeValues();

    /**
     * Returns the attribute values defined in the generation. Returns an empty array if the
     * generation hasn't got an attribute value.
     */
    IAttributeValue[] getAttributeValues();

    /**
     * Returns the attribute value for the given attribute name. Returns {@code null} if this
     * container has no value for the given attribute. Returns {@code null} if attribute is
     * {@code null}.
     */
    IAttributeValue getAttributeValue(String attribute);

    /**
     * Creates a new attribute value.
     */
    IAttributeValue newAttributeValue();

    /**
     * Creates a new attribute value for the given product component attribute and sets the value to
     * the default value defined in the attribute. If attribute is {@code null} the value is still
     * created but no reference to the attribute is set.
     */
    IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute);

    /**
     * Returns the default value configuration elements.
     */
    IConfiguredDefault[] getConfiguredDefaults();

    /**
     * Returns the value set configuration elements.
     */
    IConfiguredValueSet[] getConfiguredValueSets();

    /**
     * Returns the default value configuration element that corresponds to the attribute with the
     * given name. Returns {@code null} if no such element exists.
     */
    IConfiguredDefault getConfiguredDefault(String attributeName);

    /**
     * Returns the value set configuration element that corresponds to the attribute with the given
     * name. Returns {@code null} if no such element exists.
     */
    IConfiguredValueSet getConfiguredValueSet(String attributeName);

    /**
     * Returns the number of configuration elements.
     */
    int getNumOfConfigElements();

    /**
     * Returns the product component's relations to other product components.
     * 
     * Use {@link #getLinksAsList()} instead
     */
    IProductCmptLink[] getLinks();

    /**
     * Returns the links that are instances of the given product component type association or an
     * empty array if no such link is found. Use {@link #getLinksAsList(String)} instead
     * 
     * @param association The name (=target role singular) of an association.
     * @throws IllegalArgumentException if type relation is null.
     */
    IProductCmptLink[] getLinks(String association);

    /**
     * Returns a list containing all links defined in the product component and this product
     * component generation.
     * 
     */
    List<IProductCmptLink> getLinksIncludingProductCmpt();

    /**
     * Returns a new table content usage.
     */
    ITableContentUsage newTableContentUsage();

    /**
     * Returns a new table content usage that is based on the table structure usage.
     */
    ITableContentUsage newTableContentUsage(ITableStructureUsage structureUsage);

    /**
     * Returns the number of used table contents.
     */
    int getNumOfTableContentUsages();

    /**
     * @param rolename The role name for the required content usage.
     * @return The table content usage for the table structure usage with the given role name.
     */
    ITableContentUsage getTableContentUsage(String rolename);

    /**
     * @return All table content usages defined by this generation.
     */
    ITableContentUsage[] getTableContentUsages();

    /**
     * Returns the number of formulas defined in the generation.
     */
    int getNumOfFormulas();

    /**
     * Returns the formulas defined in the generation. Returns an empty array if the generation
     * hasn't got a formula.
     */
    IFormula[] getFormulas();

    /**
     * Returns the formula with given name or {@code null} if no such formula is found. Returns
     * {@code null} if formulaName is {@code null}.
     */
    IFormula getFormula(String formulaName);

    /**
     * Creates a new formula.
     */
    IFormula newFormula();

    /**
     * Creates a new formula based on the given signature. If signature is {@code null} the formula
     * is still created, but no reference to a signature is set.
     */
    IFormula newFormula(IProductCmptTypeMethod signature);

    /**
     * Returns a list containing the property values of the given class defined in the product
     * component and this product component generation.
     */
    <T extends IPropertyValue> List<T> getPropertyValuesIncludingProductCmpt(Class<T> type);

    /**
     * Returns the generation of the template that is used by this generation if this generation's
     * product component has specified a template. Returns {@code null} if no template is specified
     * or the specified template was not found.
     * 
     * @see IProductCmpt#getTemplate()
     * @see IProductCmpt#setTemplate(String)
     * 
     * @param ipsProject The project that should be used to search for the template
     * @return The generation of the specified template of this generation
     */
    @Override
    IProductCmptGeneration findTemplate(IIpsProject ipsProject);

}
