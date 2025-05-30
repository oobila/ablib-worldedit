package com.github.oobila.bukkit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import lombok.NoArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@NoArgsConstructor
@SuppressWarnings("unused")
public class SchematicSaveOperationBuilder {

    private EditSession editSession;
    private Region region;
    private boolean affectsEntities;
    private boolean removeEntities;
    private Transform transform;

    public SchematicSaveOperationBuilder(Player player) throws IncompleteRegionException {
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
        editSession = localSession.createEditSession(new BukkitPlayer(player));
        editSession.setSideEffectApplier(new SideEffectSet(Map.of(SideEffect.NEIGHBORS, SideEffect.State.OFF)));
        region = localSession.getSelection();
    }

    public SchematicSaveOperationBuilder(Region region) {
        editSession = WorldEdit.getInstance().newEditSession(region.getWorld());
        editSession.setSideEffectApplier(new SideEffectSet(Map.of(SideEffect.NEIGHBORS, SideEffect.State.OFF)));
        this.region = region;
    }

    public SchematicSaveOperationBuilder(Clipboard clipboard) {
        editSession = WorldEdit.getInstance().newEditSession(clipboard.getRegion().getWorld());
        editSession.setSideEffectApplier(new SideEffectSet(Map.of(SideEffect.NEIGHBORS, SideEffect.State.OFF)));
        this.region = clipboard.getRegion();
    }

    public SchematicSaveOperationBuilder affectsEntities(boolean affectsEntities) {
        this.affectsEntities = affectsEntities;
        return this;
    }

    public SchematicSaveOperationBuilder removeEntities(boolean removeEntities) {
        this.removeEntities = removeEntities;
        return this;
    }

    public SchematicSaveOperationBuilder transform(Transform transform) {
        this.transform = transform;
        return this;
    }

    public void save(File file) throws IOException, WorldEditException {
        file.getParentFile().mkdirs();
        save(new FileOutputStream(file));
    }

    public void save(OutputStream outputStream) throws IOException, WorldEditException {
        Clipboard clipboard = buildClipboard();
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(outputStream)){
            writer.write(clipboard);
        }
    }

    public Clipboard buildClipboard() throws WorldEditException {
        Clipboard clipboard = new BlockArrayClipboard(region);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setCopyingEntities(affectsEntities);
        copy.setRemovingEntities(removeEntities);
        copy.setCopyingBiomes(false);
        if (transform != null) {
            copy.setTransform(transform);
        }
        if (affectsEntities || removeEntities) {
            for (BlockVector2 bv2 : region.getChunks()) {
                Chunk chunk = BukkitAdapter.adapt(region.getWorld()).getChunkAt(bv2.x(), bv2.z());
                chunk.load();
            }
        }
        Operations.complete(copy);
        return clipboard;
    }

}
