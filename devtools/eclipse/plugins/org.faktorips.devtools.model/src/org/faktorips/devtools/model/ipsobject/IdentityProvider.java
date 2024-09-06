/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import org.w3c.dom.Element;

/**
 * A provider for {@link Identifier Identifiers} used in an extensionpoint, so it can be used in
 * other Faktor-IPS plugins. The identifiers are used to compare {@link IIpsObjectPart
 * IIpsObjectParts} based on different criteria as the id, e.g.: the name.
 */
public interface IdentityProvider {

    /**
     * Creates an {@link Identifier} for an specific {@link IIpsObjectPart}.
     *
     * @param xmlTag the xml-tag of an ips-object-part, e.g.: label or description
     * @return an identifier using other criteria as the uuid of the {@link IIpsObjectPart}, e.g.:
     *             the name
     */
    Identifier getIdentity(Element xmlTag);

    /**
     * Creates an {@link Identifier} for an specific {@link IIpsObjectPart}.
     *
     * @param part the {@link IIpsObjectPart}, e.g.: {@link ILabel} or {@link IDescription}
     * @return an identifier using other criteria as the uuid of the {@link IIpsObjectPart}, e.g.:
     *             the name
     */
    Identifier getIdentity(IIpsObjectPart part);
}
