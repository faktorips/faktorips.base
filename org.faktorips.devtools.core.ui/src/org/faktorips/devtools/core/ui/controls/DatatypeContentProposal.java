/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.faktorips.datatype.Datatype;

/**
 * A content proposal for <code>Datatype</code>. The label and image of these proposals could be
 * provided by the <code>DatatypeContentProposalProvider</code>.
 * 
 * @see IContentProposal
 * 
 * @author hbaagil
 */

public class DatatypeContentProposal implements IContentProposal {

    private final Datatype datatype;

    public DatatypeContentProposal(Datatype dataType) {
        this.datatype = dataType;
    }

    public Datatype getDataype() {
        return datatype;
    }

    @Override
    public String getContent() {
        return datatype.getQualifiedName();
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    @Override
    public String getLabel() {
        return datatype.getName();
    }

    @Override
    public String getDescription() {
        return StringUtils.EMPTY;
    }

}
