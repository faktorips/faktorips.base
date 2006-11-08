/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.ITableStructureUsage;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmptGenerationPolicyCmptTypeDelta;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.ITableContentUsage;

/**
 * Content provider to represent differnces between product components and policy component types.
 * 
 * @author Thorsten Guenther
 */
final class ProductCmptDeltaContentProvider implements ITreeContentProvider {
	private IProductCmptGenerationPolicyCmptTypeDelta in;

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IProductCmptGenerationPolicyCmptTypeDelta) {
			in = (IProductCmptGenerationPolicyCmptTypeDelta)newInput;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements(Object inputElement) {
		if (!(inputElement instanceof IProductCmptGenerationPolicyCmptTypeDelta)) {
			return new Object[0];
		}
		
		IProductCmptGenerationPolicyCmptTypeDelta in = (IProductCmptGenerationPolicyCmptTypeDelta)inputElement;
		
		ArrayList result = new ArrayList();
	
		if (in.getAttributesWithMissingConfigElements().length > 0) {
			result.add(ProductCmptDeltaType.MISSING_CFGELEMENT);
		}
		if (in.getConfigElementsWithMissingAttributes().length > 0) {
			result.add(ProductCmptDeltaType.MISSING_ATTRIBUTE);
		}
		if (in.getElementsWithValueSetMismatch().length > 0) {
			result.add(ProductCmptDeltaType.VALUESET_MISMATCH);
		}
		if (in.getRelationsWithMissingPcTypeRelations().length > 0) {
			result.add(ProductCmptDeltaType.RELATION_MISMATCH);
		}
		if (in.getTypeMismatchElements().length > 0) {
			result.add(ProductCmptDeltaType.CFGELEMENT_TYPE_MISMATCH);
		}
        if (in.getTableStructureUsagesWithMissingContentUsages().length > 0) {
            result.add(ProductCmptDeltaType.MISSING_CONTENTUSAGE);
        }
        if (in.getTableContentUsagesWithMissingStructureUsages().length > 0) {
            result.add(ProductCmptDeltaType.MISSING_STRUCTUREUSAGE);
        }
		return result.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren(Object element) {
		return element instanceof ProductCmptDeltaType;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(Object element) {
		if (element instanceof ProductCmptDeltaDetail) {
			return ((ProductCmptDeltaDetail)element).getType();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof ProductCmptDeltaType)) {
			return new Object[0];
		}
	
		ArrayList result = new ArrayList();
		
		if (parentElement == ProductCmptDeltaType.CFGELEMENT_TYPE_MISMATCH) {
			IConfigElement[] elems = in.getTypeMismatchElements();
			String message = Messages.ProductCmptDeltaContentProvider_msgTypeMismatch;
			for (int i = 0; i < elems.length; i++) {
				try {
					IAttribute attr = elems[i].findPcTypeAttribute(); 
					String[] params = {elems[i].getType().getName(), attr.getConfigElementType().getName(), elems[i].getName()};
					
					result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.CFGELEMENT_TYPE_MISMATCH, NLS.bind(message, params)));
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
		}
		else if (parentElement == ProductCmptDeltaType.MISSING_ATTRIBUTE) {
			IConfigElement[] elems = in.getConfigElementsWithMissingAttributes();
			String message = Messages.ProductCmptDeltaContentProvider_msgNoAttribute;
			for (int i = 0; i < elems.length; i++) {
				result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.MISSING_ATTRIBUTE, NLS.bind(message, elems[i].getName())));
			}
		}
		else if (parentElement == ProductCmptDeltaType.MISSING_CFGELEMENT) {
			IAttribute[] attrs = in.getAttributesWithMissingConfigElements();
			String message = Messages.ProductCmptDeltaContentProvider_msgNoConfigElement;
			for (int i = 0; i < attrs.length; i++) {
				result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.MISSING_CFGELEMENT, NLS.bind(message, attrs[i].getName())));
			}
		}
		else if (parentElement == ProductCmptDeltaType.RELATION_MISMATCH) {
			IProductCmptRelation[] rels = in.getRelationsWithMissingPcTypeRelations();
			String message = Messages.ProductCmptDeltaContentProvider_msgNoRelation;
			for (int i = 0; i < rels.length; i++) {
				result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.RELATION_MISMATCH, NLS.bind(message, rels[i].getProductCmptTypeRelation())));
			}
		}
		else if (parentElement == ProductCmptDeltaType.VALUESET_MISMATCH) {
			IConfigElement[] elems = in.getElementsWithValueSetMismatch();
			String valueMismatch = Messages.ProductCmptDeltaContentProvider_msgValuesetNotSubset; 
			String typeMismatch = Messages.ProductCmptDeltaContentProvider_msgValuesetMismatch; 
			for (int i = 0; i < elems.length; i++) {
				try {
					IValueSet cfgValueSet = elems[i].getValueSet();
					IValueSet attrValueSet = elems[i].findPcTypeAttribute().getValueSet();
					
					ValueSetType cfgType = cfgValueSet.getValueSetType();
					ValueSetType attrType = attrValueSet.getValueSetType();
					
					if (attrType != ValueSetType.ALL_VALUES && attrType != cfgType) {
						String[] params = {attrType.getName(), cfgType.getName(), elems[i].getName()};
						result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.VALUESET_MISMATCH, NLS.bind(typeMismatch, params)));
					}
					else {
						result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.VALUESET_MISMATCH, NLS.bind(valueMismatch, elems[i].getName())));
					}
					
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
		}
        else if (parentElement == ProductCmptDeltaType.MISSING_CONTENTUSAGE) {
            ITableStructureUsage[] tsus = in.getTableStructureUsagesWithMissingContentUsages();
            String text = Messages.ProductCmptDeltaContentProvider_msgMissingContentUsage;
            for (int i = 0; i < tsus.length; i++) {
                result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.MISSING_CONTENTUSAGE, NLS.bind(text, tsus[i]
                        .getRoleName())));
            }
        }
        else if (parentElement == ProductCmptDeltaType.MISSING_STRUCTUREUSAGE) {
            ITableContentUsage[] tcus = in.getTableContentUsagesWithMissingStructureUsages();
            String text = Messages.ProductCmptDeltaContentProvider_msgMissingStructureUsage;
            for (int i = 0; i < tcus.length; i++) {
                result.add(new ProductCmptDeltaDetail(ProductCmptDeltaType.MISSING_STRUCTUREUSAGE, NLS.bind(text,
                        tcus[i].getStructureUsage(), tcus[i].getTableContentName())));
            }
        }
				
		return result.toArray();
	}
}