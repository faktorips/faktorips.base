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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class Formula extends Expression implements IFormula {
    private IpsObjectPartCollection<IFormulaTestCase> testcases = new IpsObjectPartCollection<IFormulaTestCase>(this,
            FormulaTestCase.class, IFormulaTestCase.class, FormulaTestCase.TAG_NAME);

    public Formula(IPropertyValueContainer parent, String id) {
        super(parent, id);
    }

    public Formula(IPropertyValueContainer parent, String id, String formulaSignature) {
        super(parent, id, formulaSignature);
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public String getName() {
        return getFormulaSignature();
    }

    @Override
    public String getPropertyName() {
        return getFormulaSignature();
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findFormulaSignature(ipsProject);
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION;
    }

    @Override
    public String getPropertyValue() {
        return getExpression();
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration() {
        return (IProductCmptGeneration)getParent();
    }

    @Override
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) {
        try {
            return getProductCmptGeneration().getProductCmpt().findProductCmptType(ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public IFormulaTestCase newFormulaTestCase() {
        return testcases.newPart();
    }

    @Override
    public IFormulaTestCase getFormulaTestCase(String name) {
        return testcases.getPartByName(name);
    }

    @Override
    public IFormulaTestCase[] getFormulaTestCases() {
        return testcases.toArray(new IFormulaTestCase[testcases.size()]);
    }

    @Override
    public int[] moveFormulaTestCases(int[] indexes, boolean up) {
        return testcases.moveParts(indexes, up);
    }

    @Override
    public void removeFormulaTestCase(IFormulaTestCase formulaTest) {
        testcases.removePart(formulaTest);
    }

    @Override
    protected ITableContentUsage[] getTableContentUsages() {
        IProductCmptGeneration gen = getProductCmptGeneration();
        return gen.getTableContentUsages();
    }

    @Override
    protected void collectEnumTypesFromAttributes(Map<String, EnumDatatype> enumTypes) {
        try {
            IIpsProject ipsProject = getIpsProject();
            IProductCmptType productCmptType = getProductCmptGeneration().getProductCmpt().findProductCmptType(
                    ipsProject);
            if (productCmptType != null) {
                List<IAttribute> attributes = productCmptType.findAllAttributes(ipsProject);
                for (IAttribute attribute : attributes) {
                    Datatype datatype = attribute.findDatatype(ipsProject);
                    if (datatype instanceof EnumDatatype) {
                        enumTypes.put(datatype.getName(), (EnumDatatype)datatype);
                    }
                }
            }
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject) {
        if (StringUtils.isEmpty(getFormulaSignature())) {
            return null;
        }
        IProductCmptType type = findProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        try {
            return type.findFormulaSignature(getFormulaSignature(), ipsProject);
        } catch (final CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<IAttribute> findMatchingProductCmptTypeAttributes() {
        IProductCmptType productCmptType = findProductCmptType(getIpsProject());
        if (productCmptType != null) {
            try {
                return productCmptType.findAllAttributes(getIpsProject());
            } catch (final CoreException e) {
                throw new CoreRuntimeException(e.getMessage(), e);
            }
        }
        return Collections.emptyList();
    }

}
