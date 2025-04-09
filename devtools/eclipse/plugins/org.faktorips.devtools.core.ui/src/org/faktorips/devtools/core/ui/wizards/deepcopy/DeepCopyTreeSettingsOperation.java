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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
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
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptLink;
import org.faktorips.devtools.model.internal.productcmpt.TableContentUsage;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

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
        Map<IProductCmpt, List<TreeStatus>> jsonTree = new TreeMap<>((o1, o2) -> {
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.getQualifiedName().compareTo(o2.getQualifiedName());
        });

        for (Entry<IProductCmpt, Map<IIpsObjectPart, LinkStatus>> entry1 : presentationModel.getTreeStatus()
                .getTreeStatus()
                .entrySet()) {
            IProductCmpt productCmpt = entry1.getKey();
            for (Entry<IIpsObjectPart, LinkStatus> entry2 : entry1.getValue().entrySet()) {
                TreeStatus e = map(entry2);
                jsonTree.computeIfAbsent(productCmpt, $ -> new ArrayList<>()).add(e);
            }
        }
        jsonTree.values().stream().forEach(Collections::sort);
        return Collections.unmodifiableMap(jsonTree);
    }

    private TreeStatus map(Entry<IIpsObjectPart, LinkStatus> entry2) {
        IIpsObjectPart ipsObjPart = entry2.getKey();
        LinkStatus linkstatus = entry2.getValue();
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

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        // do nothing
    }

    private static final class IProductCmptAdapter extends TypeAdapter<IProductCmpt> {
        @Override
        public IProductCmpt read(JsonReader arg0) throws IOException {
            return null;
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
                e.printStackTrace();
            }
        }
    }

    public static class DeepCopyTreeLoadSettingsOperation extends DeepCopyTreeSettingsOperation {

        public DeepCopyTreeLoadSettingsOperation(DeepCopyPresentationModel deepCopyTreeStatus) {
            super(SWT.OPEN, deepCopyTreeStatus);
        }

        @Override
        public void performOperation(String filePath, Gson gson) {
            // TODO Auto-generated method stub

        }
    }
}
