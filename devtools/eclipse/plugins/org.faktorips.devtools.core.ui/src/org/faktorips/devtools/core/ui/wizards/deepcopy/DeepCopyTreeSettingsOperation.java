/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.internal.productcmpt.TableContentUsage;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;

public abstract class DeepCopyTreeSettingsOperation implements SelectionListener {

    private static final Type GSON_TYPE = new TypeToken<Map<IProductCmpt, List<TreeStatus>>>() {
        // no content needed
    }.getType();

    private final int operation;
    private final DeepCopyPresentationModel presentationModel;

    public DeepCopyTreeSettingsOperation(int operation, DeepCopyPresentationModel presentationModel) {
        this.operation = operation;
        this.presentationModel = presentationModel;
    }

    /**
     * Write or Load the JSON file with the structured settings.
     *
     * @param filePath the path to the file, selected in the file dialog
     * @param gson the GSON de-serializer
     */
    public abstract void performOperation(String filePath, Gson gson);

    @Override
    public void widgetSelected(SelectionEvent e) {
        Shell shell = e.display.getActiveShell();
        FileDialog dialog = new FileDialog(shell, operation);
        dialog.setFilterNames(new String[] {
                "Deep Copy Settings", "All Files (*.*)" });
        dialog.setFilterExtensions(new String[] {
                "*.json", "*.*" });
        String filePath = dialog.open();

        performOperation(filePath, createGson());
    }

    protected Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(IProductCmpt.class, new IProductCmptAdapter())
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();
    }

    public Map<IProductCmpt, List<TreeStatus>> status() {
        Map<IProductCmpt, List<TreeStatus>> jsonTree = new TreeMap<>(
                nullsFirst(comparing(IProductCmpt::getQualifiedName)));

        for (Entry<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> entryByProduct : presentationModel.getTreeStatus()
                .getTreeStatus()
                .entrySet()) {
            IProductCmpt productCmpt = entryByProduct.getKey();
            for (Entry<IIpsObjectPart, LinkStatus> entryByPart : entryByProduct.getValue().entrySet()) {
                TreeStatus e = map(entryByPart);
                jsonTree.computeIfAbsent(productCmpt, $ -> new ArrayList<>()).add(e);
            }
        }
        jsonTree.values().stream().forEach(Collections::sort);
        return Collections.unmodifiableMap(jsonTree);
    }

    private TreeStatus map(Entry<IIpsObjectPart, LinkStatus> statusPerIpsObj) {
        IIpsObjectPart ipsObjPart = statusPerIpsObj.getKey();
        LinkStatus linkstatus = statusPerIpsObj.getValue();
        String association = switch (ipsObjPart) {
            case ProductCmptLink l -> l.getAssociation();
            case TableContentUsage t -> t.getStructureUsage();
            case null, default -> null;
        };
        String type = ipsObjPart == null ? null : ipsObjPart.getClass().getName();
        // if ipsObjPart == null then target is the root of tree
        String target = ipsObjPart == null ? linkstatus.getTarget().getQualifiedName() : ipsObjPart.getName();

        return new TreeStatus(type,
                association,
                target,
                linkstatus.isChecked(),
                linkstatus.getCopyOrLink());
    }

    protected void map(Map<IProductCmpt, List<TreeStatus>> fromJson) {
        Map<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> treeStatus = new HashMap<>();
        IProductCmptReference root = presentationModel.getStructure().getRoot();

        for (Entry<IProductCmpt, List<TreeStatus>> entry : fromJson.entrySet()) {
            IProductCmpt productCmpt = entry.getKey();
            Map<IIpsObjectPart, LinkStatus> linkStatus = treeStatus.computeIfAbsent(productCmpt, $ -> new HashMap<>());
            for (TreeStatus jsonStatus : entry.getValue()) {
                if (productCmpt == null) {
                    // is root
                    linkStatus.put(root.getWrapped(),
                            new LinkStatus(root.getWrapped(),
                                    root.getWrappedIpsObject(),
                                    jsonStatus.checked(),
                                    jsonStatus.copyOrLink()));
                    continue;
                }
                IProductCmptStructureReference ref;
                if (ProductCmptLink.class.getName().equals(jsonStatus.linkType())) {
                    ref = findProductCmptRefByKindId(jsonStatus.target());
                } else {
                    ref = findTblUsageRefByKindId(jsonStatus.target());
                }
                IIpsObjectPart ipsObjectPart = ref == null ? null : ref.getWrapped();
                linkStatus.put(ipsObjectPart,
                        new LinkStatus(ipsObjectPart,
                                ref == null ? null : ref.getWrappedIpsObject(),
                                jsonStatus.checked(),
                                jsonStatus.copyOrLink()));
            }
        }
        presentationModel.getTreeStatus().setTreeStatus(treeStatus);
        presentationModel.getTreeStatus().updatedTreeStatus();
    }

    private IProductCmptStructureTblUsageReference findTblUsageRefByKindId(String tableContentName) {
        String kindId;
        if (tableContentName.matches("(?:\\w+\\.)*\\w+ \\d{4}-\\d{2}")) {
            kindId = tableContentName.substring(0, tableContentName.lastIndexOf(' '));
        } else {
            // Table must not follow the kindId versionId theme
            kindId = tableContentName;
        }
        List<IProductCmptStructureTblUsageReference> filter = presentationModel.getStructure().toSet(false).stream()
                .filter(IProductCmptStructureTblUsageReference.class::isInstance)
                .map(IProductCmptStructureTblUsageReference.class::cast)
                .filter(l -> l.getTableContentUsage().getName().startsWith(kindId)).toList();
        IProductCmptStructureTblUsageReference possibleTblRef = null;
        for (IProductCmptStructureTblUsageReference tblRef : filter) {
            if (tblRef.getTableContentUsage().getName().equals(tableContentName)) {
                return tblRef;
            }
            possibleTblRef = tblRef;
        }
        return possibleTblRef;
    }

    private IProductCmptReference findProductCmptRefByKindId(String qualifiedName) {
        String kindId = qualifiedName.substring(0, qualifiedName.lastIndexOf(' '));
        List<IProductCmptReference> possibleProductCmpts = presentationModel.getStructure().toSet(true).stream()
                .filter(IProductCmptReference.class::isInstance)
                .map(IProductCmptReference.class::cast)
                .filter(p -> p.getProductCmpt().getQualifiedName().startsWith(kindId))
                .toList();
        IProductCmptReference possibleProductCmpt = null;
        for (IProductCmptReference productCmpt : possibleProductCmpts) {
            if (productCmpt.getProductCmpt().getQualifiedName().equals(qualifiedName)) {
                return productCmpt;
            }
            possibleProductCmpt = productCmpt;
        }
        return possibleProductCmpt;
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // do nothing
    }

    /**
     * Saves the selections from the deep copy tree in a json file.
     */
    public static class DeepCopyTreeSaveSettingsOperation extends DeepCopyTreeSettingsOperation {

        public DeepCopyTreeSaveSettingsOperation(DeepCopyPresentationModel deepCopyTreeStatus) {
            super(SWT.SAVE, deepCopyTreeStatus);
        }

        @Override
        public void performOperation(String filePath, Gson gson) {
            try (FileWriter writer = new FileWriter(new File(filePath))) {
                gson.toJson(status(),
                        GSON_TYPE,
                        writer);
            } catch (IOException e) {
                throw new IpsException("Error while saving deep copy tree settings.", e);
            }
        }
    }

    /**
     * Loads the selections for the deep copy tree from a json file.
     */
    public static class DeepCopyTreeLoadSettingsOperation extends DeepCopyTreeSettingsOperation {

        public DeepCopyTreeLoadSettingsOperation(DeepCopyPresentationModel deepCopyTreeStatus) {
            super(SWT.OPEN, deepCopyTreeStatus);
        }

        @Override
        public void performOperation(String filePath, Gson gson) {
            try (FileReader reader = new FileReader(new File(filePath))) {
                Map<IProductCmpt, List<TreeStatus>> fromJson = gson.fromJson(reader, GSON_TYPE);
                map(fromJson);
            } catch (IOException e) {
                throw new IpsException("Error while reading deep copy tree settings.", e);
            }

        }
    }

    private final class IProductCmptAdapter extends TypeAdapter<IProductCmpt> {
        @Override
        public IProductCmpt read(JsonReader reader) throws IOException {
            String qualifiedName = reader.nextString();
            if ("null".equals(qualifiedName)) {
                return null;
            }
            IProductCmptReference ref = findProductCmptRefByKindId(qualifiedName);
            return ref == null ? null : ref.getProductCmpt();
        }

        @Override
        public void write(JsonWriter writer, IProductCmpt productCmpt) throws IOException {
            if (productCmpt != null) {
                writer.value(productCmpt.getQualifiedName());
            }
        }
    }

    public static record TreeStatus(String linkType, String association, String target, boolean checked,
            CopyOrLink copyOrLink) implements Comparable<TreeStatus> {

        @Override
        public int compareTo(TreeStatus o) {
            return target.compareTo(o.target());
        }
    }
}
