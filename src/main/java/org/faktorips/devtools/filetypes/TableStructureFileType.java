/*******************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.com
 *
 * All Rights Reserved - Alle Rechte vorbehalten.
 ******************************************************/
package org.faktorips.devtools.filetypes;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TableStructureFileType extends LanguageFileType {

    public static final LanguageFileType INSTANCE = new TableStructureFileType();
    private static final Icon ICON = IconLoader.getIcon("/icons/filetypes/TableStructure.svg", TableStructureFileType.class);
    private static final String FILE_EXTENSION = "ipstablestructure";

    public TableStructureFileType() {
        super(XMLLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "TableStructure";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Table Structure";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public @Nullable Icon getIcon() {
        return ICON;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return getName();
    }
}
