package com.github.oobila.bukkit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unused")
public class SchematicLoadOperationBuilder {

    private final Clipboard clipboard;
    private boolean affectsEntities;
    private boolean ignoreAir;
    private Transform transform;

    public SchematicLoadOperationBuilder(File file) throws IOException, WorldEditException {
        this(new FileInputStream(file));
    }

    public SchematicLoadOperationBuilder(InputStream inputStream) throws IOException, WorldEditException {
        this(inputStream, null);
    }

    public SchematicLoadOperationBuilder(InputStream inputStream, PaletteReplacer paletteReplacer) throws IOException, WorldEditException {
        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getReader(inputStream)) {
            clipboard = reader.read();
            if (paletteReplacer != null) {
                paletteReplacer.update(clipboard);
            }
        }
    }

    public SchematicLoadOperationBuilder(Clipboard clipboard) throws WorldEditException {
        this(clipboard, null);
    }

    public SchematicLoadOperationBuilder(Clipboard clipboard, PaletteReplacer paletteReplacer) throws WorldEditException {
        this.clipboard = clipboard;
        if (paletteReplacer != null) {
            paletteReplacer.update(clipboard);
        }
    }

    public SchematicLoadOperationBuilder affectsEntities(boolean affectsEntities) {
        this.affectsEntities = affectsEntities;
        return this;
    }

    public SchematicLoadOperationBuilder ignoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
        return this;
    }

    public SchematicLoadOperationBuilder transform(Transform transform) {
        this.transform = transform;
        return this;
    }

    public Clipboard clipboard() {
        return clipboard;
    }

    public void paste(Region region) throws WorldEditException {
        paste(BukkitAdapter.adapt(BukkitAdapter.adapt(region.getWorld()), region.getMinimumPoint()));
    }

    public void paste(Location location) throws WorldEditException {
        BlockVector3 blockVector3 = BukkitAdapter.asBlockVector(location);
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
//            editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
            ForwardExtentCopy operation = (ForwardExtentCopy) new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(blockVector3)
                    .copyEntities(affectsEntities)
                    .ignoreAirBlocks(ignoreAir)
                    .ignoreAirBlocks(false)
                    .copyBiomes(false)
                    .build();
            if (transform != null) {
                operation.setTransform(transform);
            }
            Operations.complete(operation);
        }
    }

}
