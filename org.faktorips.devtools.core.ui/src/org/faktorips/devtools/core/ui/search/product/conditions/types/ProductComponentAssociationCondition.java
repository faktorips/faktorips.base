/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.valueset.IValueSet;

public class ProductComponentAssociationCondition extends AbstractCondition {

    private final class ProductComponentAssociationOperandProvider implements IOperandProvider {

        private final IProductCmptTypeAssociation productCmptTypeAssociation;

        public ProductComponentAssociationOperandProvider(IProductCmptTypeAssociation productCmptTypeAssociation) {
            this.productCmptTypeAssociation = productCmptTypeAssociation;
        }

        @Override
        public Object getSearchOperand(IProductCmptGeneration productComponentGeneration) {
            List<String> targetNames = new ArrayList<String>();

            IProductCmptLink[] links = productComponentGeneration.getLinks();
            for (IProductCmptLink link : links) {
                try {
                    boolean linkOfAssociation = link.isLinkOfAssociation(productCmptTypeAssociation,
                            productComponentGeneration.getIpsProject());
                    if (linkOfAssociation) {
                        targetNames.add(link.getTarget());
                    }
                } catch (CoreException e) {
                    // FIXME handle exception
                    e.printStackTrace();
                }
            }

            return targetNames;
        }
    }

    @Override
    public List<IIpsElement> getSearchableElements(IProductCmptType productCmptType) {
        try {
            return new ArrayList<IIpsElement>(productCmptType.findAllAssociations(productCmptType.getIpsProject()));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public List<? extends ISearchOperatorType> getSearchOperatorTypes(IIpsElement elementPart) {
        return Arrays.asList(ReferenceSearchOperatorType.values());
    }

    @Override
    public ValueDatatype getValueDatatype(IIpsElement elementPart) {
        return new StringDatatype();
    }

    @Override
    public IValueSet getValueSet(IIpsElement elementPart) {
        throw new IllegalStateException("This Condition doesn't allow calling getValueSet");
    }

    @Override
    public Collection<?> getAllowedValues(IIpsElement elementPart) {
        Set<String> allowedValues = new HashSet<String>();
        IProductCmptTypeAssociation productCmptTypeAssociation = (IProductCmptTypeAssociation)elementPart;

        try {
            IProductCmptType productCmptType = productCmptTypeAssociation.findTargetProductCmptType(elementPart
                    .getIpsProject());

            for (IIpsSrcFile srcFile : elementPart.getIpsProject().findAllProductCmptSrcFiles(productCmptType, true)) {
                IIpsObject obj = (IIpsObject)srcFile.getAdapter(IIpsObject.class);
                allowedValues.add(obj.getQualifiedName());
            }

            IIpsProject[] ipsProductDefinitionProjects = IpsPlugin.getDefault().getIpsModel()
                    .getIpsProductDefinitionProjects();

            for (IIpsProject productIpsProject : ipsProductDefinitionProjects) {
                IIpsSrcFile[] srcFiles = productIpsProject.findAllProductCmptSrcFiles(productCmptType, true);
                for (IIpsSrcFile srcFile : srcFiles) {
                    IIpsObject obj = (IIpsObject)srcFile.getAdapter(IIpsObject.class);
                    allowedValues.add(obj.getQualifiedName());
                }
            }

        } catch (CoreException e) {
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
        return "Association";
    }

    @Override
    public String getNoSearchableElementsMessage(IProductCmptType productCmptType) {
        return "NIX GEFUNDEN";
    }

}
