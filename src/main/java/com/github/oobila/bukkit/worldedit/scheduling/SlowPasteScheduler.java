package com.github.oobila.bukkit.worldedit.scheduling;

import com.github.oobila.bukkit.common.scheduling.AsyncJob;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import lombok.SneakyThrows;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

public class SlowPasteScheduler extends AsyncJob {

    private final BlockVector3 pastePosition;
    private final World world;
    private final int maxBlockBatchCount;
    private final ClipboardHolder clipboardHolder;
    private final Clipboard clipboard;
    private final boolean ignoreAir;

    public SlowPasteScheduler(BlockVector3 pastePosition, World world, int maxBlockBatchCount,
                              ClipboardHolder clipboardHolder, boolean ignoreAir) {
        this.pastePosition = pastePosition;
        this.world = world;
        this.maxBlockBatchCount = maxBlockBatchCount;
        this.clipboardHolder = clipboardHolder;
        this.clipboard = clipboardHolder.getClipboard();
        this.ignoreAir = ignoreAir;
    }

    @SneakyThrows
    @Override
    public void run() {
        BlockVector3 min = clipboard.getMinimumPoint();
        BlockVector3 max = clipboard.getMaximumPoint();
        WorldEditBlockPlacer worldEditBlockPlacer = new WorldEditBlockPlacer(world);

        Vector3 transformDiff = calculateTransformDiff(
                clipboard.getMaximumPoint(), clipboard.getMinimumPoint(), clipboardHolder.getTransform());

        clipboard.getEntities().forEach(entity ->
            world.createEntity(entity.getLocation(), (BaseEntity) entity)
        );

        int i = 0;
        for(int y = min.y(); y <= max.y(); y++){
            for(int x = min.x(); x <= max.x(); x++){
                for(int z = min.z(); z <= max.z(); z++){
                    if(i >= maxBlockBatchCount){
                        addChildJob(worldEditBlockPlacer);
                        worldEditBlockPlacer = new WorldEditBlockPlacer(world);
                        i = 0;
                    }

                    BaseBlock baseBlock = clipboard.getFullBlock(BlockVector3.at(x, y, z));
                    baseBlock = BlockTransformExtent.transform(baseBlock, clipboardHolder.getTransform());
                    baseBlock = transformNBT(baseBlock, clipboardHolder.getTransform());

                    Vector3 transformedVector = clipboardHolder.getTransform()
                            .apply(Vector3.at(x, y, z))
                            .add(transformDiff);
                    BlockVector3 transformedBlockVector = BlockVector3.at(
                            transformedVector.x(),
                            transformedVector.y(),
                            transformedVector.z())
                            .subtract(clipboard.getMinimumPoint())
                            .add(pastePosition);
                    if(!(ignoreAir && baseBlock.getBlockType().getMaterial().isAir())){
                        worldEditBlockPlacer.addBlock(baseBlock, transformedBlockVector);
                        i++;
                    }
                }
            }
        }
        addChildJob(worldEditBlockPlacer);
    }

    private BaseBlock transformNBT(BaseBlock state, Transform transform) {
        //copied from WE ExtentBlockCopy class
        LinCompoundTag tag = state.getNbt();
        if (tag != null) {
            LinTag<?> rotTag = tag.value().get("Rot");
            if (rotTag != null && rotTag.value() instanceof Number number) {
                int rot = number.intValue();
                Direction direction = MCDirections.fromRotation(rot);
                if (direction != null) {
                    Vector3 vector = transform.apply(direction.toVector()).subtract(transform.apply(Vector3.ZERO)).normalize();
                    Direction newDirection = Direction.findClosest(
                            vector,
                            Direction.Flag.CARDINAL | Direction.Flag.ORDINAL | Direction.Flag.SECONDARY_ORDINAL
                    );
                    if (newDirection != null) {
                        return state.toBaseBlock(
                                tag.toBuilder()
                                        .putByte("Rot", (byte) MCDirections.toRotation(newDirection))
                                        .build()
                        );
                    }
                }
            }
        }
        return state;
    }

    private static Vector3 calculateTransformDiff(BlockVector3 p0, BlockVector3 p1, Transform transform) {
        Vector3 mid = getMid(p0.toVector3(), p1.toVector3());
        Vector3 transformMid = transform.apply(mid);
        return mid.subtract(transformMid);
    }

    private static Vector3 getMid(Vector3 p0, Vector3 p1){
        return (p0.add(p1))
                .divide(2);
    }
}
