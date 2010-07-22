/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
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
public interface IProductCmptNamingStrategy {

    /**
     * Name of XML tags representing a product component naming strategy.
     */
    public final static String XML_TAG_NAME = "ProductCmptNamingStrategy"; //$NON-NLS-1$

    /**
     * Prefix for all message codes for classes implementing the interface.
     */
    public final static String MSGCODE_PREFIX = "ProductCmptNamingStrategy-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the kindId is empty.
     */
    public final static String MSGCODE_KIND_ID_IS_EMPTY = MSGCODE_PREFIX + "KindIdIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name contains illegal characters.
     */
    public final static String MSGCODE_ILLEGAL_VERSION_ID = MSGCODE_PREFIX + "IllegalVersionId"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the version separator is missing.
     */
    public final static String MSGCODE_MISSING_VERSION_SEPARATOR = MSGCODE_PREFIX + "VersionSeparatorIsMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name contains illegal characters.
     */
    public final static String MSGCODE_ILLEGAL_CHARACTERS = MSGCODE_PREFIX + "IllegalCharacters"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the runtime id has a valid format.
     */
    public final static String MSGCODE_INVALID_RUNTIME_ID_FORMAT = MSGCODE_PREFIX + "InvalidRuntimeIdFormat"; //$NON-NLS-1$

    /**
     * Sets the IPSproject this strategy belongs to. Is called when the strategy is instantiated.
     * Should never be called by clients.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     */
    public void setIpsProject(IIpsProject project);

    /**
     * Returns the IPS project the strategy belongs to.
     */
    public IIpsProject getIpsProject();

    /**
     * Implementations of this interface are provided as extension. The method returns the id of
     * this extension.
     */
    public String getExtensionId();

    /**
     * Returns <code>true</code> if this strategy distinguishes between the kind id and a version
     * id, otherwise <code>false</code>.
     */
    public boolean supportsVersionId();

    /**
     * Returns the unqualified product component name defined by the kind id and the version id.
     * Returns <code>null</code> if kind id and version id are <code>null</code>. If only of the two
     * arguments is <code>null</code>, the method returns the other.
     */
    public String getProductCmptName(String kindId, String versionId);

    /**
     * Returns the product component name's kind id, that is the name without the version id.
     * Returns <code>null</code> if productCmptName is <code>null</code>.
     * 
     * @param productCmptName The unqualified product component name.
     * @throws IllegalArgumentException if the constant part can't be extracted from the name.
     */
    public String getKindId(String productCmptName);

    /**
     * Returns the version id included in the product component name.
     * 
     * @param productCmptName The unqualified product component name.
     * @throws IllegalArgumentException if the versionId can't be extracted from the unqualified
     *             name.
     */
    public String getVersionId(String productCmptName);

    /**
     * Returns the next version id.
     */
    public String getNextVersionId(IProductCmpt productCmpt);

    /**
     * Returns the name that is constructed from the given product component's name's constant part
     * and the next version id.
     */
    public String getNextName(IProductCmpt productCmpt);

    /**
     * Checks if the version id and the constant part can be derived from the given product
     * component name and if they have the correct format. The strategy has to ensure that if two
     * names would result in the same Java identifier only one of them is considered as valid by the
     * validate() method.
     */
    public MessageList validate(String name);

    /**
     * Checks if the version id has the correct format.
     * 
     * @throws NullPointerException if versionId is <code>null</code>.
     */
    public MessageList validateVersionId(String versionId);

    /**
     * Checks if the kindId has the correct format.
     * 
     * @throws NullPointerException if kindId is <code>null</code>.
     */
    public MessageList validateKindId(String kindId);

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
    public String getJavaClassIdentifier(String name);

    /**
     * Initializes the strategy with the data from the xml element. This method must be able to read
     * those elements created by the toXml() method. The element's node name is expected to be the
     * name defined in <code>XML_TAG_NAME</code>.
     * <p>
     * Concrete classes implementing this interface use their own tag name for an element that is
     * nested inside the given element. E.g.
     * 
     * <pre>
     *     <ProductCmptNamingStrategy>
     *         <NoVersionIdProductCmptNamingStrategy/>
     *     </ProductCmptNamingStrategy>
     * </pre>
     */
    public void initFromXml(Element el);

    /**
     * Creates an XML element representation of this strategy. The element's node name is defined in
     * <code>XML_TAG_NAME</code>.
     * 
     * @param doc The XML document to create new elements.
     */
    public Element toXml(Document doc);

    /**
     * Finds the runtime id to use for the given project and product component name. The result of
     * this method is not ensured to be the same for different calls. This method should only be
     * used to find the id for a new product component, but does not set the runtime id for the
     * product component.
     * 
     * @param project The project which will be used to evaluate the runtime id.
     * @param productCmptName The name of the new product component for which the runtime id will be
     *            returned.
     * @throws CoreException if an error occurs during evaluation.
     */
    public String getUniqueRuntimeId(IIpsProject project, String productCmptName) throws CoreException;

    /**
     * Compares the given runtime IDs. This method was introduced because a simple call to
     * <code>equals()</code> comparing the two runtime IDs returned by a call to
     * <code>getRuntimeId()</code> can not be used to decide whether the runtime IDs of two product
     * components are the same or not. This is because the strategy might also use the templates the
     * product component are based on to decide if the two have an identical id. E.g. a product 1
     * based on the template MotorProduct and a product 2 based on the template HomeProduct might
     * both have the same runtime id 42, but because they are based on different templates they are
     * considered as different.
     * 
     * @param runtimeId1 The first product component to check.
     * @param runtimeId2 The second product component to check.
     * @return <code>true</code> if the runtime IDs of both product components are the same,
     *         <code>false</code> otherwise.
     */
    public boolean sameRuntimeId(String runtimeId1, String runtimeId2);

    /**
     * Validates the given runtime id.
     */
    public MessageList validateRuntimeId(String runtimeId);

}
