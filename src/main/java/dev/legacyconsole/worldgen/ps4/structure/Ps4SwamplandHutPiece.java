// Shared block-placement expressions adapted to the PS4/LCE piece algorithms.
// Generation, random streams, finite limits, template data and persistence are
// controlled by this port; see WORLDGEN-PS4-REFERENCE.json for provenance.
package dev.legacyconsole.worldgen.ps4.structure;
import net.minecraft.world.level.levelgen.structure.*;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class Ps4SwamplandHutPiece
extends Ps4ScatteredFeaturePiece {
    private boolean spawnedWitch;
    private boolean spawnedCat;

    public Ps4SwamplandHutPiece(Random random, int n, int n2) {
        super(Ps4PieceTypes.SWAMPLAND_HUT, n, 64, n2, 7, 7, 9, Ps4SwamplandHutPiece.getRandomHorizontalDirection(random));
    }

    public Ps4SwamplandHutPiece(ServerLevel serverLevel, CompoundTag compoundTag) {
        super(Ps4PieceTypes.SWAMPLAND_HUT, compoundTag);
        this.spawnedWitch = compoundTag.getBoolean("Witch");
        this.spawnedCat = compoundTag.getBoolean("Cat");
    }

    @Override
    protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
        super.addAdditionalSaveData(serverLevel, compoundTag);
        compoundTag.putBoolean("Witch", this.spawnedWitch);
        compoundTag.putBoolean("Cat", this.spawnedCat);
    }

    @Override
    public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos;
        if (!this.updateAverageGroundHeight(worldGenLevel, boundingBox, 0)) {
            return false;
        }
        this.generateBox(worldGenLevel, boundingBox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        this.generateBox(worldGenLevel, boundingBox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
        this.placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 1, 3, 4, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 3, 4, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), 5, 3, 5, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, boundingBox);
        this.placeBlock(worldGenLevel, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, boundingBox);
        BlockState blockState = (BlockState)Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState blockState2 = (BlockState)Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
        BlockState blockState3 = (BlockState)Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
        BlockState blockState4 = (BlockState)Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        this.generateBox(worldGenLevel, boundingBox, 0, 4, 1, 6, 4, 1, blockState, blockState, false);
        this.generateBox(worldGenLevel, boundingBox, 0, 4, 2, 0, 4, 7, blockState2, blockState2, false);
        this.generateBox(worldGenLevel, boundingBox, 6, 4, 2, 6, 4, 7, blockState3, blockState3, false);
        this.generateBox(worldGenLevel, boundingBox, 0, 4, 8, 6, 4, 8, blockState4, blockState4, false);
        this.placeBlock(worldGenLevel, (BlockState)blockState.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, boundingBox);
        this.placeBlock(worldGenLevel, (BlockState)blockState.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, boundingBox);
        this.placeBlock(worldGenLevel, (BlockState)blockState4.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, boundingBox);
        this.placeBlock(worldGenLevel, (BlockState)blockState4.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, boundingBox);
        for (int i = 2; i <= 7; i += 5) {
            for (int j = 1; j <= 5; j += 4) {
                this.fillColumnDown(worldGenLevel, Blocks.OAK_LOG.defaultBlockState(), j, -1, i, boundingBox);
            }
        }
        if (!this.spawnedWitch && boundingBox.isInside(mutableBlockPos = this.getWorldPos(2, 2, 5))) {
            this.spawnedWitch = true;
            Witch witch = EntityType.WITCH.create(worldGenLevel.getLevel());
            witch.setPersistenceRequired();
            witch.moveTo((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5, 0.0f, 0.0f);
            witch.finalizeSpawn(worldGenLevel, worldGenLevel.getCurrentDifficultyAt(mutableBlockPos), MobSpawnType.STRUCTURE, null, null);
            worldGenLevel.addFreshEntityWithPassengers(witch);
        }
        this.spawnCat(worldGenLevel, boundingBox);
        return true;
    }

    private void spawnCat(ServerLevelAccessor serverLevelAccessor, BoundingBox boundingBox) {
        BlockPos.MutableBlockPos mutableBlockPos;
        if (!this.spawnedCat && boundingBox.isInside(mutableBlockPos = this.getWorldPos(2, 2, 5))) {
            this.spawnedCat = true;
            Cat cat = EntityType.CAT.create(serverLevelAccessor.getLevel());
            cat.setPersistenceRequired();
            cat.moveTo((double)mutableBlockPos.getX() + 0.5, mutableBlockPos.getY(), (double)mutableBlockPos.getZ() + 0.5, 0.0f, 0.0f);
            cat.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mutableBlockPos), MobSpawnType.STRUCTURE, null, null);
            serverLevelAccessor.addFreshEntityWithPassengers(cat);
        }
    }
}

