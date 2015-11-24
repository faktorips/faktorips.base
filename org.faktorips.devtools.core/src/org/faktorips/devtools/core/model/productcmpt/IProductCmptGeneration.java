/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;

public interface IProductCmptGeneration extends IIpsObjectGeneration, IPropertyValueContainer,
IProductCmptLinkContainer {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "PRODUCTCMPTGEN-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the generation contains less relations of a specific
     * relation type than required by the relation type. E.g. a motor product must contain at least
     * one relation to a collision coverage component, but it does not.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the generation and the second one the relation type as string. In both
     * cases the property part of the ObjectProperty is empty.
     * 
     * @deprecated As of 3.8. Use {@link IProductCmptLinkContainer#MSGCODE_NOT_ENOUGH_RELATIONS}
     *             instead.
     */
    @Deprecated
    public static final String MSGCODE_NOT_ENOUGH_RELATIONS = MSGCODE_PREFIX + "NotEnoughRelations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the generation contains more relations of a specific
     * relation type than specified by the relation type. E.g. a motor product can contain at most
     * one relation to a collision coverage component, but contains two (or more) relations to
     * collision coverage components.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the generation and the second one the relation type as string. In both
     * cases the property part of the ObjectProperty is empty.
     * 
     * @deprecated As of 3.8. Use {@link IProductCmptLinkContainer#MSGCODE_TOO_MANY_RELATIONS}
     *             instead.
     */
    @Deprecated
    public static final String MSGCODE_TOO_MANY_RELATIONS = MSGCODE_PREFIX + "ToManyRelations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that two or more relations of a specific type have the
     * same target.
     * 
     * @deprecated As of 3.8. Use
     *             {@link IProductCmptLinkContainer#MSGCODE_DUPLICATE_RELATION_TARGET} instead.
     */
    @Deprecated
    public static final String MSGCODE_DUPLICATE_RELATION_TARGET = MSGCODE_PREFIX + "DuplicateRelationTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the template for the product this generation is for
     * could not be found.
     */
    public static final String MSGCODE_NO_TEMPLATE = MSGCODE_PREFIX + "NoTemplate"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component type for this generation
     * contains an attribute, method or tableStructureUsage that has no corresponding property
     * configured in this generation.
     */
    public static final String MSGCODE_PROPERTY_NOT_CONFIGURED = MSGCODE_PREFIX + "PropertyNotConfigured"; //$NON-NLS-1$

    /**
     * Validation message code to identify the message that informs about a link to a product
     * component that doesn't have an effective date that is before or equal to the effective date
     * of the referencing product component generation.
     */
    public static final String MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE = MSGCODE_PREFIX + "LinksWithWrongEffectivDate"; //$NON-NLS-1$

    /**
     * Returns the product component this generation belongs to.
     */
    @Override
    public IProductCmpt getProductCmpt();

    /**
     * Searches the product component type this product component generation is based on.
     * 
     * @param ipsProject The IPS project which search path is used to search the type.
     * 
     * @return The product component type this product component generation is based on or
     *         {@code null} if the product component type can't be found.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is {@code null}.
     */
    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the number of attribute values defined in the generation.
     */
    public int getNumOfAttributeValues();

    /**
     * Returns the attribute values defined in the generation. Returns an empty array if the
     * generation hasn't got an attribute value.
     */
    public IAttributeValue[] getAttributeValues();

    /**
     * Returns the attribute value for the given attribute name. Returns {@code null} if this
     * container has no value for the given attribute. Returns {@code null} if attribute is
     * {@code null}.
     */
    public IAttributeValue getAttributeValue(String attribute);

    /**
     * Creates a new attribute value.
     */
    public IAttributeValue newAttributeValue();

    /**
     * Creates a new attribute value for the given product component attribute and sets the value to
     * the default value defined in the attribute. If attribute is {@code null} the value is still
     * created but no reference to the attribute is set.
     */
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute);

    /**
     * Creates a new attribute value for the given product component attribute and sets the value.
     * 
     * @deprecated as of 3.4. Use {@link #newAttributeValue(IProductCmptTypeAttribute)} instead.
     */
    @Deprecated
    public IAttributeValue newAttributeValue(IProductCmptTypeAttribute attribute, String value);

    /**
     * Returns the configuration elements.
     */
    public IConfigElement[] getConfigElements();

    /**
     * Returns the configuration element that corresponds to the attribute with the given name.
     * Returns {@code null} if no such element exists.
     */
    public IConfigElement getConfigElement(String attributeName);

    /**
     * Creates a new configuration element.
     */
    public IConfigElement newConfigElement();

    /**
     * Creates a new configuration element for the given attribute. If attribute is {@code null} no
     * reference to an attribute is set, but the new config element is still created.
     */
    public IConfigElement newConfigElement(IPolicyCmptTypeAttribute attribute);

    /**
     * Returns the number of configuration elements.
     */
    public int getNumOfConfigElements();

    /**
     * Returns the product component's relations to other product components.
     * 
     * Use {@link #getLinksAsList()} instead
     */
    public IProductCmptLink[] getLinks();

    /**
     * Returns the links that are instances of the given product component type association or an
     * empty array if no such link is found. Use {@link #getLinksAsList(String)} instead
     * 
     * @param association The name (=target role singular) of an association.
     * @throws IllegalArgumentException if type relation is null.
     */
    public IProductCmptLink[] getLinks(String association);

    /**
     * Returns a list containing all links defined in the product component and this product
     * component generation.
     * 
     */
    public List<IProductCmptLink> getLinksIncludingProductCmpt();

    /**
     * Returns a new table content usage.
     */
    public ITableContentUsage newTableContentUsage();

    /**
     * Returns a new table content usage that is based on the table structure usage.
     */
    public ITableContentUsage newTableContentUsage(ITableStructureUsage structureUsage);

    /**
     * Returns the number of used table contents.
     */
    public int getNumOfTableContentUsages();

    /**
     * @param rolename The role name for the required content usage.
     * @return The table content usage for the table structure usage with the given role name.
     */
    public ITableContentUsage getTableContentUsage(String rolename);

    /**
     * @return All table content usages defined by this generation.
     */
    public ITableContentUsage[] getTableContentUsages();

    /**
     * Returns the number of formulas defined in the generation.
     */
    public int getNumOfFormulas();

    /**
     * Returns the formulas defined in the generation. Returns an empty array if the generation
     * hasn't got a formula.
     */
    public IFormula[] getFormulas();

    /**
     * Returns the formula with given name or {@code null} if no such formula is found. Returns
     * {@code null</code> if formulaName is <code>null}.
     */
    public IFormula getFormula(String formulaName);

    /**
     * Creates a new formula.
     */
    public IFormula newFormula();

    /**
     * Creates a new formula based on the given signature. If signature is {@code null} the formula
     * is still created, but no reference to a signature is set.
     */
    public IFormula newFormula(IProductCmptTypeMethod signature);

    /**
     * Returns the number of validation rules defined (or configured respectively) in this
     * generation.
     */
    public int getNumOfValidationRules();

    /**
     * Returns the validation with the given name if defined in this generation. Returns <null> no
     * validation rule with the given name can be found or if the given name is {@code null}.
     */
    public IValidationRuleConfig getValidationRuleConfig(String validationRuleName);

    /**
     * Returns the validation rules defined in this generation. Returns an empty array if this
     * generation does not configure any validation rules.
     */
    public List<IValidationRuleConfig> getValidationRuleConfigs();

    /**
     * Creates a new validation rule that configures the given {@link IValidationRule}. If signature
     * is {@code null} the validation rule configuration is still created, but no reference to an
     * {@link IValidationRule} is set.
     */
    public IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured);

    /**
     * Returns a list containing the property values of the given class defined in the product
     * component and this product component generation.
     */
    public <T extends IPropertyValue> List<T> getPropertyValuesIncludingProductCmpt(Class<T> type);

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
