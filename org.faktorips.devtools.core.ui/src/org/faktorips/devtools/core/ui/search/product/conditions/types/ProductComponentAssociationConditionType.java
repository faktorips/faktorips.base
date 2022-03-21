/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.IProductComponent;

/**
 * A condition for {@link IAssociation IAssociations} of a {@link IProductComponent}.
 * <p>
 * The condition tests, whether the association of a IProductComponent (within a
 * {@link IProductCmptGeneration}) references another given {@link IProductComponent}.
 * <p>
 * The ProductAttributeConditionType uses the {@link ReferenceSearchOperatorType
 * ReferenceSearchOperatorTypes}.
 * 
 * @author dicker
 */
public class ProductComponentAssociationConditionType extends AbstractConditionType {

    @Override
    public List<IIpsElement> getSearchableElements(IProductCmptType productCmptType) {
        return new ArrayList<>(productCmptType.findAllAssociations(productCmptType.getIpsProject()));
    }

    @Override
    public List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement searchableElement) {
        return Arrays.asList(ReferenceSearchOperatorType.values());
    }

    @Override
    public ValueDatatype getValueDatatype(IIpsElement elementPart) {
        return new StringDatatype();
    }

    @Override
    public IValueSet getValueSet(IIpsElement elementPart) {
        throw new IllegalStateException("This Condition doesn't allow calling getValueSet"); //$NON-NLS-1$
    }

    @Override
    public Collection<?> getAllowedValues(IIpsElement elementPart) {
        Set<String> allowedValues = new HashSet<>();
        IProductCmptTypeAssociation productCmptTypeAssociation = (IProductCmptTypeAssociation)elementPart;

        try {
            IProductCmptType productCmptType = productCmptTypeAssociation.findTargetProductCmptType(elementPart
                    .getIpsProject());

            for (IIpsSrcFile srcFile : elementPart.getIpsProject().findAllProductCmptSrcFiles(productCmptType, true)) {
                IIpsObject obj = srcFile.getAdapter(IIpsObject.class);
                allowedValues.add(obj.getQualifiedName());
            }
            IIpsProject[] ipsProjects = IIpsModel.get().getIpsProjects();

            for (IIpsProject productIpsProject : ipsProjects) {
                IIpsSrcFile[] srcFiles = productIpsProject.findAllProductCmptSrcFiles(productCmptType, true);
                for (IIpsSrcFile srcFile : srcFiles) {
                    IIpsObject obj = srcFile.getAdapter(IIpsObject.class);
                    allowedValues.add(obj.getQualifiedName());
                }
            }

        } catch (IpsException e) {
            // TODO Exception Handling
            throw new RuntimeException(e);
        }
        return allowedValues;
    }

    @Override
    public boolean hasValueSet() {
        return false;
    }

    @Override
    public IOperandProvider createOperandProvider(IIpsElement elementPart) {
        return new ProductComponentAssociationOperandProvider((IProductCmptTypeAssociation)elementPart);
    }

    @Override
    public String getName() {
        return Messages.ProductComponentAssociationCondition_association;
    }

    @Override
    public boolean isArgumentIpsObject() {
        return true;
    }

    private static final class ProductComponentAssociationOperandProvider implements IOperandProvider {

        private final IProductCmptTypeAssociation productCmptTypeAssociation;

        public ProductComponentAssociationOperandProvider(IProductCmptTypeAssociation productCmptTypeAssociation) {
            this.productCmptTypeAssociation = productCmptTypeAssociation;
        }

        @Override
        public Object getSearchOperand(IProductPartsContainer linkContainer) {
            List<String> targetNames = new ArrayList<>();

            List<IProductCmptLink> links = linkContainer.getProductParts(IProductCmptLink.class);
            for (IProductCmptLink link : links) {
                try {
                    boolean linkOfAssociation = link.isLinkOfAssociation(productCmptTypeAssociation,
                            linkContainer.getIpsProject());
                    if (linkOfAssociation) {
                        targetNames.add(link.getTarget());
                    }
                } catch (IpsException e) {
                    // TODO handle exception
                    e.printStackTrace();
                }
            }

            return targetNames;
        }
    }
}
