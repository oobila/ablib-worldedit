package com.github.oobila.bukkit.worldedit.util;

import com.github.oobila.bukkit.chat.Message;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorldEditUtil {

    public static final Object SCHEMATIC_FILE_EXTENSION = ".schem";

    public static BlockVector3 toBV3(Vector3 vector3) {
        return BlockVector3.at(
                vector3.x(),
                vector3.y(),
                vector3.z());
    }

    public static BoundingBox toBoundingBox(Region region) {
        return new BoundingBox(
                region.getMinimumPoint().x(),
                region.getMinimumPoint().y(),
                region.getMinimumPoint().z(),
                region.getMaximumPoint().x(),
                region.getMaximumPoint().y(),
                region.getMaximumPoint().z()
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

    public static String getSelectionAsLuaSchematic(Player player) throws WorldEditLibException {
        char[] characterList = "0abcdefghijklmno".toCharArray();
        List<Material> materialSet = new ArrayList<>();
        materialSet.add(Material.AIR);
        StringBuilder dataStringBuilder = new StringBuilder();
        Region region = getRegion(player);
        World world = player.getWorld();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        for(int y = min.y(); y <= max.y(); y++) {
            for (int x = min.x(); x <= max.x(); x++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material material = block.getType();
                    if (!materialSet.contains(material)) {
                        if (materialSet.size() == 15) {
                            throw new WorldEditLibException("too many materials in lua schematic operation");
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

    public static void editSessionTransaction(World world, WorldEditConsumer<EditSession> transaction) throws WorldEditException {
        editSessionTransaction(BukkitAdapter.adapt(world), transaction);
    }

    public static void editSessionTransaction(com.sk89q.worldedit.world.World world, WorldEditConsumer<EditSession> transaction) throws WorldEditException {
        WorldEdit worldEdit = WorldEdit.getInstance();
        try (EditSession editSession = worldEdit.newEditSession(world)) {
            editSession.setSideEffectApplier(new SideEffectSet(Map.of(SideEffect.NEIGHBORS, SideEffect.State.OFF)));
            transaction.accept(editSession);
        } catch (WorldEditException e) {
            throw new WorldEditLibException(e);
        }
    }

    public interface WorldEditConsumer<T> {
        void accept(T t) throws WorldEditException;
    }
}
