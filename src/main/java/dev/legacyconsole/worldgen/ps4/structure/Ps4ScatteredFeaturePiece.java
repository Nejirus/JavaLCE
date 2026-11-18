// Shared block-placement expressions adapted to the PS4/LCE piece algorithms.
// Generation, random streams, finite limits, template data and persistence are
// controlled by this port; see WORLDGEN-PS4-REFERENCE.json for provenance.
package dev.legacyconsole.worldgen.ps4.structure;
import net.minecraft.world.level.levelgen.structure.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public abstract class Ps4ScatteredFeaturePiece
extends StructurePiece {
    protected final int width;
    protected final int height;
    protected final int depth;
    protected int heightPosition = -1;

    protected Ps4ScatteredFeaturePiece(StructurePieceType structurePieceType, int n, int n2, int n3, int n4, int n5, int n6, Direction direction) {
        super(structurePieceType, 0, StructurePiece.makeBoundingBox(n, n2, n3, direction, n4, n5, n6));
        this.width = n4;
        this.height = n5;
        this.depth = n6;
        this.setOrientation(direction);
    }

    protected Ps4ScatteredFeaturePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
        super(structurePieceType, compoundTag);
        this.width = compoundTag.getInt("Width");
        this.height = compoundTag.getInt("Height");
        this.depth = compoundTag.getInt("Depth");
        this.heightPosition = compoundTag.getInt("HPos");
    }

    @Override
    protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
        compoundTag.putInt("Width", this.width);
        compoundTag.putInt("Height", this.height);
        compoundTag.putInt("Depth", this.depth);
        compoundTag.putInt("HPos", this.heightPosition);
    }

    protected boolean updateAverageGroundHeight(LevelAccessor levelAccessor, BoundingBox boundingBox, int n) {
        if (this.heightPosition >= 0) {
            return true;
        }
        int n2 = 0;
        int n3 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = this.boundingBox.minZ(); i <= this.boundingBox.maxZ(); ++i) {
            for (int j = this.boundingBox.minX(); j <= this.boundingBox.maxX(); ++j) {
                mutableBlockPos.set(j, 64, i);
                if (!boundingBox.isInside(mutableBlockPos)) continue;
                n2 += levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos).getY();
                ++n3;
            }
        }
        if (n3 == 0) {
            return false;
        }
        this.heightPosition = n2 / n3;
        this.boundingBox.move(0, this.heightPosition - this.boundingBox.minY() + n, 0);
        return true;
    }
}

