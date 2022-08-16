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

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The product component name includes a kind id that is constant over different versions and a
 * version id.E.g. given "FullCoverage 2005-01-01", the kind id is "FullCoverage" the version id is
 * "2005-01-01".
 * <p>
 * The product component naming strategy defines how the name is constructed from a given kind id
 * and a version id and vice versa. It also defines how to derive the next version id.
 * <p>
 * Note that is also possible to define a naming strategy that does not distinguish between a kind
 * id and the version id. In this case the method <code>supportsVersionId()</code> must return
 * <code>false</code>.
 * 
 * @author Jan Ortmann
 */
public interface IProductDefObjectNamingStrategy {

    /**
     * Name of XML tags representing a product component naming strategy.
     */
    String XML_TAG_NAME = "ProductCmptNamingStrategy"; //$NON-NLS-1$

    /**
     * Prefix for all message codes for classes implementing the interface.
     */
    String MSGCODE_PREFIX = "ProductCmptNamingStrategy-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the kindId is empty.
     */
    String MSGCODE_KIND_ID_IS_EMPTY = MSGCODE_PREFIX + "KindIdIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name contains illegal characters.
     */
    String MSGCODE_ILLEGAL_VERSION_ID = MSGCODE_PREFIX + "IllegalVersionId"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version separator is missing.
     */
    String MSGCODE_MISSING_VERSION_SEPARATOR = MSGCODE_PREFIX + "VersionSeparatorIsMissing"; //$NON-NLS-1$

    /**
     * Sets the IPS project this strategy belongs to. Is called when the strategy is instantiated.
     * Should never be called by clients.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     */
    void setIpsProject(IIpsProject project);

    /**
     * Returns the IPS project the strategy belongs to.
     */
    IIpsProject getIpsProject();

    /**
     * Implementations of this interface are provided as extension. The method returns the id of
     * this extension.
     */
    String getExtensionId();

    /**
     * Returns <code>true</code> if this strategy distinguishes between the kind id and a version
     * id, otherwise <code>false</code>.
     */
    boolean supportsVersionId();

    /**
     * Returns the unqualified product component name defined by the kind id and the version id.
     * Returns <code>null</code> if kind id and version id are <code>null</code>. If only of the two
     * arguments is <code>null</code>, the method returns the other.
     */
    String getProductCmptName(String kindId, String versionId);

    /**
     * Returns the product component name's kind id, that is the name without the version id.
     * Returns <code>null</code> if productCmptName is <code>null</code>.
     * 
     * @param productCmptName The unqualified product component name.
     * @throws IllegalArgumentException if the constant part can't be extracted from the name.
     */
    String getKindId(String productCmptName);

    /**
     * Returns the version id included in the product component name.
     * 
     * @param productCmptName The unqualified product component name.
     * @throws IllegalArgumentException if the versionId can't be extracted from the unqualified
     *             name.
     */
    String getVersionId(String productCmptName);

    /**
     * Returns the next version id.
     */
    String getNextVersionId(IProductCmpt productCmpt);

    /**
     * Returns the name that is constructed from the given product component's name's constant part
     * and the next version id.
     */
    String getNextName(IProductCmpt productCmpt);

    /**
     * Checks if the version id and the constant part can be derived from the given product
     * component name and if they have the correct format. The strategy has to ensure that if two
     * names would result in the same Java identifier only one of them is considered as valid by the
     * validate() method.
     */
    MessageList validate(String name);

    /**
     * Checks if the version id has the correct format.
     * 
     * @throws NullPointerException if versionId is <code>null</code>.
     */
    MessageList validateVersionId(String versionId);

    /**
     * Checks if the kindId has the correct format.
     * 
     * @throws NullPointerException if kindId is <code>null</code>.
     */
    MessageList validateKindId(String kindId);

    /**
     * Returns a valid Java class identifier for the given name. For example - and . are not allowed
     * in Java identifiers but are typically used in the name for example to separate constant part
     * and version id.
     * <p>
     * The strategy has to ensure that if two names would result in the same Java identifier only
     * one of them is considered as valid by the validate() method.
     * <p>
     * A good strategy is for example to replace the character - by two underscores, . by three
     * underscores and to disallow names with two or more unseparated underscores.
     * <p>
     * Returns <code>null</code> if if name is <code>null</code>.
     * 
     * @throws IllegalArgumentException if the name can't be transformed to a valid Java class name
     *             as it contains special characters that can't be handled by this strategy.
     */
    String getJavaClassIdentifier(String name);

    /**
     * Initializes the strategy with the data from the XML element. This method must be able to read
     * those elements created by the toXml() method. The element's node name is expected to be the
     * name defined in <code>XML_TAG_NAME</code>.
     * <p>
     * Concrete classes implementing this interface use their own tag name for an element that is
     * nested inside the given element. E.g.
     * 
     * <pre>
     * {@code
     *  <ProductCmptNamingStrategy>
     *      <NoVersionIdProductCmptNamingStrategy/>
     *  </ProductCmptNamingStrategy>
     * }
     * </pre>
     */
    void initFromXml(Element el);

    /**
     * Creates an XML element representation of this strategy. The element's node name is defined in
     * <code>XML_TAG_NAME</code>.
     * 
     * @param doc The XML document to create new elements.
     */
    Element toXml(Document doc);

}
