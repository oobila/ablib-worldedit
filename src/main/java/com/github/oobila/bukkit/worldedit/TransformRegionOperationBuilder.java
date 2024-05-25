package com.github.oobila.bukkit.worldedit;

import com.github.oobila.bukkit.worldedit.util.WorldEditUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;

public class TransformRegionOperationBuilder {

    private final Region region;
    private AffineTransform transform = new AffineTransform();

    public TransformRegionOperationBuilder(Region region) {
        this.region = region;
    }

    public TransformRegionOperationBuilder rotateX(double x) {
        transform = transform.rotateX(x);
        return this;
    }

    public TransformRegionOperationBuilder rotateY(double y) {
        transform = transform.rotateY(y);
        return this;
    }

    public TransformRegionOperationBuilder rotateZ(double z) {
        transform = transform.rotateZ(z);
        return this;
    }

    public TransformRegionOperationBuilder flipX() {
        //TODO
        return this;
    }

    public TransformRegionOperationBuilder flipY() {
        //TODO
        return this;
    }

    public TransformRegionOperationBuilder flipZ() {
        //TODO
        return this;
    }

    public void apply() throws WorldEditException {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld())) {
            //copy
            Clipboard clipboard = new BlockArrayClipboard(region);
            ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
            copy.setCopyingEntities(true);
            copy.setRemovingEntities(true);
            Operations.complete(copy);

            //rotate
            ClipboardHolder holder = new ClipboardHolder(clipboard);
            holder.setTransform(holder.getTransform().combine(transform));

            //paste
            Operation operation = holder
                    .createPaste(editSession)
                    .to(transformedRegion(region, transform).getMinimumPoint())
                    .ignoreAirBlocks(false)
                    .copyEntities(true)
                    .build();
            Operations.complete(operation);
        }
    }

    private Region transformedRegion(Region region, AffineTransform transform) {
        Vector3 transformDiff = calculateTransformDiff(region, transform);
        BlockVector3 transformBV3 = WorldEditUtil.toBV3(transformDiff);
        return new CuboidRegion(
                region.getWorld(),
                region.getMinimumPoint().add(transformBV3),
                region.getMaximumPoint().add(transformBV3));
    }

    private Vector3 calculateTransformDiff(Region region, Transform transform) {
        Vector3 translate = region.getMinimumPoint().toVector3();
        Vector3 mid = getMid(
                region.getMinimumPoint().toVector3().subtract(translate),
                region.getMaximumPoint().toVector3().subtract(translate)
        );
        Vector3 transformMid = transform.apply(mid);
        return mid.subtract(transformMid);
    }

    private Vector3 getMid(Vector3 p0, Vector3 p1){
        return (p0.add(p1))
                .divide(2);
    }
}
