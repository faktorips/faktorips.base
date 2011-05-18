/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IAssociation;

public interface IProductCmptGeneration extends IIpsObjectGeneration, IPropertyValueContainer {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PRODUCTCMPTGEN-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the generation contains less relations of a specific
     * relation type than required by the relation type. E.g. a motor product must contain at least
     * one collision coverage type, but the motor product does not contain a relation to a collision
     * coverage type.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the generation and the second one the relation type as string. In both
     * cases the property part of the ObjectProperty is empty.
     */
    public final static String MSGCODE_NOT_ENOUGH_RELATIONS = MSGCODE_PREFIX + "NotEnoughRelations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the generation contains more relations of a specific
     * relation type than specified by the relation type. E.g. a motor product can contain only one
     * collision coverage type, but the motor product contains two relations to a collision coverage
     * type.
     * <p>
     * Note that the message returned by the validate method contains two (Invalid)ObjectProperties.
     * The first one contains the generation and the second one the relation type as string. In both
     * cases the property part of the ObjectProperty is empty.
     */
    public final static String MSGCODE_TOO_MANY_RELATIONS = MSGCODE_PREFIX + "ToManyRelations"; //$NON-NLS-1$

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
     * Validation message code to indicate that more than one relation of a specific type have the
     * same target.
     */
    public final static String MSGCODE_DUPLICATE_RELATION_TARGET = MSGCODE_PREFIX + "DuplicateRelationTarget"; //$NON-NLS-1$

    /**
     * Validation message code to identify the message that informs about a link to a product
     * component that doesn't have an effective date that is before or equal to the effective date
     * of the referencing product component generation.
     */
    public final static String MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE = MSGCODE_PREFIX + "LinksWithWrongEffectivDate"; //$NON-NLS-1$

    /**
     * Returns the product component this generation belongs to.
     */
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
     * Sorts the generation's properties according to the order defined in the model. If the product
     * component type isn't found, the generation remains unchanged.
     * 
     * @param ipsProject The ips project which search path is used to search the type.
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public void sortPropertiesAccordingToModel(IIpsProject ipsProject) throws CoreException;

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
     * Returns the attribute value for the given attribute name. Returns <code>null</code> if the
     * generation has no value for the given attribute. Returns <code>null</code> if attribute is
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
     */
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
     */
    public IProductCmptLink[] getLinks();

    /**
     * Returns the links that are instances of the given product component type association or an
     * empty array if no such link is found.
     * 
     * @param association The name (=target role singular) of an association.
     * @throws IllegalArgumentException if type relation is null.
     */
    public IProductCmptLink[] getLinks(String association);

    /**
     * Returns the number of relations.
     */
    public int getNumOfLinks();

    /**
     * Creates a new link that is an instance of the product component type association identified
     * by the given association name.
     * 
     * @throws NullPointerException if associationName is <code>null</code>.
     */
    public IProductCmptLink newLink(String associationName);

    /**
     * Creates a new link that is an instance of the product component type association.
     * 
     * @throws NullPointerException if association is <code>null</code>.
     */
    public IProductCmptLink newLink(IProductCmptTypeAssociation association);

    /**
     * Creates a new link that is an instance of the given association. The new link is placed
     * before the given one.
     */
    public IProductCmptLink newLink(String association, IProductCmptLink insertBefore);

    /**
     * Checks whether a new link as instance of the given product component type association and the
     * given target will be valid.
     * 
     * @param ipsProject The project which ips object path is used for the search. This is not
     *            necessarily the project this component is part of.
     * 
     * @return <code>true</code> if a new relation with the given values will be valid,
     *         <code>false</code> otherwise.
     * 
     * @throws CoreException if a problem occur during the search of the type hierarchy.
     */
    public boolean canCreateValidLink(IProductCmpt target, IAssociation association, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Moves the link given with parameter <code>toMove</code> before or after the specified target
     * link. If the target belongs to another association, the association of the toMove link will
     * change, too.
     * <p>
     * With the boolean parameter before you could specify to move the link before or after the
     * target link.
     * 
     * @param toMove the link you want to move
     * @param target target link you want to move the <code>toMove</code>-Link
     * @param before true for moving <code>toMove</code> in front of target, false to move it behind
     *            target
     * 
     * @return The method returns true if the link could be moved and returns false if toMove or
     *         target is null one of the links was not part of this generation yet.
     * 
     */
    public boolean moveLink(IProductCmptLink toMove, IProductCmptLink target, boolean before);

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
     * Creates a new validation rule.
     */
    public IValidationRuleConfig newValidationRuleConfig();

    /**
     * Creates a new validation rule that configures the given {@link IValidationRule}. If signature
     * is <code>null</code> the validation rule configuration is still created, but no reference to
     * an {@link IValidationRule} is set.
     */
    public IValidationRuleConfig newValidationRuleConfig(IValidationRule ruleToBeConfigured);

}
