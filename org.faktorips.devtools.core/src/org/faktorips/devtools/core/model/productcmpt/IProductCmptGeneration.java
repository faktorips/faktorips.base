/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public final static String MSGCODE_PREFIX = "PRODUCTCMPTGEN-"; //$NON-NLS-1$

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
    public final static String MSGCODE_NOT_ENOUGH_RELATIONS = MSGCODE_PREFIX + "NotEnoughRelations"; //$NON-NLS-1$

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
    public final static String MSGCODE_TOO_MANY_RELATIONS = MSGCODE_PREFIX + "ToManyRelations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that two or more relations of a specific type have the
     * same target.
     * 
     * @deprecated As of 3.8. Use
     *             {@link IProductCmptLinkContainer#MSGCODE_DUPLICATE_RELATION_TARGET} instead.
     */
    @Deprecated
    public final static String MSGCODE_DUPLICATE_RELATION_TARGET = MSGCODE_PREFIX + "DuplicateRelationTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the template for the product this generation is for
     * could not be found.
     */
    public final static String MSGCODE_NO_TEMPLATE = MSGCODE_PREFIX + "NoTemplate"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product component type for this generation
     * contains an attribute that has no corresponding configuration element configured in this
     * generation.
     */
    public final static String MSGCODE_ATTRIBUTE_WITH_MISSING_CONFIG_ELEMENT = MSGCODE_PREFIX
            + "AttributeWithMissingConfigElement"; //$NON-NLS-1$

    /**
     * Validation message code to identify the message that informs about a link to a product
     * component that doesn't have an effective date that is before or equal to the effective date
     * of the referencing product component generation.
     */
    public final static String MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE = MSGCODE_PREFIX + "LinksWithWrongEffectivDate"; //$NON-NLS-1$

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
     *         <code>null</code> if the product component type can't be found.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
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
     * Returns the attribute value for the given attribute name. Returns <code>null</code> if this
     * container has no value for the given attribute. Returns <code>null</code> if attribute is
     * <code>null</code>.
     */
    public IAttributeValue getAttributeValue(String attribute);

    /**
     * Creates a new attribute value.
     */
    public IAttributeValue newAttributeValue();

    /**
     * Creates a new attribute value for the given product component attribute and sets the value to
     * the default value defined in the attribute. If attribute is <code>null</code> the value is
     * still created but no reference to the attribute is set.
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
     * Returns <code>null</code> if no such element exists.
     */
    public IConfigElement getConfigElement(String attributeName);

    /**
     * Creates a new configuration element.
     */
    public IConfigElement newConfigElement();

    /**
     * Creates a new configuration element for the given attribute. If attribute is
     * <code>null</code> no reference to an attribute is set, but the new config element is still
     * created.
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
     * Returns the formula with given name or <code>null</code> if no such formula is found. Returns
     * <code>null</code> if formulaName is <code>null</code>.
     */
    public IFormula getFormula(String formulaName);

    /**
     * Creates a new formula.
     */
    public IFormula newFormula();

    /**
     * Creates a new formula based on the given signature. If signature is <code>null</code> the
     * formula is still created, but no reference to a signature is set.
     */
    public IFormula newFormula(IProductCmptTypeMethod signature);

    /**
     * Returns the number of validation rules defined (or configured respectively) in this
     * generation.
     */
    public int getNumOfValidationRules();

    /**
     * Returns the validation with the given name if defined in this generation. Returns <null> no
     * validation rule with the given name can be found or if the given name is <code>null</code>.
     */
    public IValidationRuleConfig getValidationRuleConfig(String validationRuleName);

    /**
     * Returns the validation rules defined in this generation. Returns an empty array if this
     * generation does not configure any validation rules.
     */
    public List<IValidationRuleConfig> getValidationRuleConfigs();

    /**
     * Creates a new validation rule that configures the given {@link IValidationRule}. If signature
     * is <code>null</code> the validation rule configuration is still created, but no reference to
     * an {@link IValidationRule} is set.
     */
    public IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured);

}
