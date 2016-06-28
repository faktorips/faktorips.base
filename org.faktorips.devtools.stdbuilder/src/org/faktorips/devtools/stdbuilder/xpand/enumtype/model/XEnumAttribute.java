/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.enumtype.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.InternationalStringDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;

public class XEnumAttribute extends AbstractGeneratorModelNode {

    public XEnumAttribute(IEnumAttribute enumAttribute, GeneratorModelContext context, ModelService modelService) {
        super(enumAttribute, context, modelService);
    }

    protected IEnumAttribute getEnumAttribute() {
        return (IEnumAttribute)getIpsObjectPartContainer();
    }

    public boolean isUnique() {
        return getEnumAttribute().isUnique();
    }

    public boolean isDisplayName() {
        return getEnumAttribute().isUsedAsNameInFaktorIpsUi();
    }

    public boolean isIdentifier() {
        return getEnumAttribute().isIdentifier();
    }

    public boolean isLiteralName() {
        return getEnumAttribute().isEnumLiteralNameAttribute();
    }

    public boolean isMultilingualSupported() {
        return getEnumAttribute().isMultilingualSupported();
    }

    public boolean isMultilingual() {
        return getEnumAttribute().isMultilingual();
    }

    protected String getMethodNameGetter() {
        Datatype datatype = getDatatypeHelper(false).getDatatype();
        return getJavaNamingConvention().getGetterMethodName(getName(), datatype);
    }

    private DatatypeHelper getDatatypeHelper(boolean mapMultilingual) {
        IEnumAttribute enumAttribute = getEnumAttribute();
        try {
            if (enumAttribute == null) {
                return getIpsProject().getDatatypeHelper(Datatype.STRING);
            } else if (mapMultilingual && enumAttribute.isMultilingual()) {
                return new InternationalStringDatatypeHelper(true);
            } else {
                ValueDatatype datatype = enumAttribute.findDatatype(getIpsProject());
                return getDatatypeHelper(datatype);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    protected DatatypeHelper getDatatypeHelper(ValueDatatype datatype) {
        return getIpsProject().getDatatypeHelper(datatype);
    }
}
