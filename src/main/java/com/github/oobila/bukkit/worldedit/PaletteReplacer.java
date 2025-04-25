package com.github.oobila.bukkit.worldedit;

import com.github.oobila.bukkit.worldedit.util.UpdateBlock;
import com.github.oobila.bukkit.worldedit.util.WorldEditLibException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class PaletteReplacer {

    private static final Map<Material, Material> replacementMap = new EnumMap<>(Material.class);

    public PaletteReplacer(Material... materials) throws WorldEditException {
        if (materials.length % 2 != 0) {
            throw new WorldEditLibException("material list needs to be provided in pairs!");
        }
        for (int i = 0; i < materials.length; i += 2) {
            replacementMap.put(materials[i], materials[i + 1]);
        }
    }

    public PaletteReplacer replace(Material o, Material n) {
        replacementMap.put(o, n);
        return this;
    }

    public PaletteReplacer replace(List<Material> o, Material n) {
        o.forEach(material -> replacementMap.put(material, n));
        return this;
    }

    public void update(Clipboard clipboard) throws WorldEditException {
        for (BlockVector3 blockVector3 : clipboard.getRegion()) {
            BlockState blockState = clipboard.getBlock(blockVector3);
            Material material = BukkitAdapter.adapt(blockState.getBlockType());
            if (replacementMap.containsKey(material)) {
                BaseBlock baseBlock = new UpdateBlock(BukkitAdapter.adapt(Material.STONE.createBlockData()));
                clipboard.setBlock(blockVector3, baseBlock);
            }
        }
    }
}
