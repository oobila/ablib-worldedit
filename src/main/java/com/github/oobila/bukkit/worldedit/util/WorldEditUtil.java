package com.github.oobila.bukkit.worldedit.util;

import com.github.oobila.bukkit.chat.Message;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorldEditUtil {

    public static final Object SCHEMATIC_FILE_EXTENSION = ".schem";

    public static BlockVector3 toBV3(Vector3 vector3) {
        return BlockVector3.at(
                vector3.getX(),
                vector3.getY(),
                vector3.getZ());
    }

    public static BoundingBox toBoundingBox(Region region) {
        return new BoundingBox(
                region.getMinimumPoint().getX(),
                region.getMinimumPoint().getY(),
                region.getMinimumPoint().getZ(),
                region.getMaximumPoint().getX(),
                region.getMaximumPoint().getY(),
                region.getMaximumPoint().getZ()
        );
    }

    public static Region getRegion(Player player) {
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
        try {
            return localSession.getSelection();
        } catch (IncompleteRegionException e) {
            new Message("Clipboard is empty").send(player);
            return null;
        }
    }

    public static boolean worldEditIsAvailable(World world) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        if (worldEdit == null) {
            return false;
        }

        try (EditSession editSession = worldEdit.newEditSession(BukkitAdapter.adapt(world))) {
            BlockVector3 bv3 = BlockVector3.at(0, 320, 0);
            editSession.setBlock(bv3, editSession.getBlock(bv3));
            editSession.commit();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static String getSelectionAsLuaSchematic(Player player) {
        char[] characterList = "0abcdefghijklmno".toCharArray();
        List<Material> materialSet = new ArrayList<>();
        materialSet.add(Material.AIR);
        StringBuilder dataStringBuilder = new StringBuilder();
        Region region = getRegion(player);
        World world = player.getWorld();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        for(int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material material = block.getType();
                    if (!materialSet.contains(material)) {
                        if (materialSet.size() == 15) {
                            throw new RuntimeException("too many materials in lua schematic operation");
                        }
                        materialSet.add(material);
                    }
                    dataStringBuilder.append(characterList[materialSet.indexOf(material)]);
                }
            }
        }
        String data = dataStringBuilder.toString();
        String materials = materialSet.stream().map(Enum::name).collect(Collectors.joining(","));
        String template = "schematic = { height=%s, length=%s, width=%s, data=%s, materials={%s} }";
        return String.format(template, region.getHeight(), region.getLength(), region.getWidth(), data, materials);
    }
}
