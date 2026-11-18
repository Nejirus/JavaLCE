// Shared block-placement expressions adapted to the PS4/LCE piece algorithms.
// Generation, random streams, finite limits, template data and persistence are
// controlled by this port; see WORLDGEN-PS4-REFERENCE.json for provenance.
package dev.legacyconsole.worldgen.ps4.structure;
import net.minecraft.world.level.levelgen.structure.*;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ps4MineShaftPieces {
    static final Logger LOGGER = LogManager.getLogger();
    private static final int DEFAULT_SHAFT_WIDTH = 3;
    private static final int DEFAULT_SHAFT_HEIGHT = 3;
    private static final int DEFAULT_SHAFT_LENGTH = 5;
    private static final int MAX_PILLAR_HEIGHT = 20;
    private static final int MAX_CHAIN_HEIGHT = 50;
    private static final int MAX_DEPTH = 8;

    private static MineShaftPiece createRandomShaftPiece(StructurePieceAccessor structurePieceAccessor, Random random, int n, int n2, int n3, @Nullable Direction direction, int n4, MineshaftFeature.Type type) {
        int n5 = random.nextInt(100);
        if (n5 >= 80) {
            BoundingBox boundingBox = MineShaftCrossing.findCrossing(structurePieceAccessor, random, n, n2, n3, direction);
            if (boundingBox != null) {
                return new MineShaftCrossing(n4, boundingBox, direction, type);
            }
        } else if (n5 >= 70) {
            BoundingBox boundingBox = MineShaftStairs.findStairs(structurePieceAccessor, random, n, n2, n3, direction);
            if (boundingBox != null) {
                return new MineShaftStairs(n4, boundingBox, direction, type);
            }
        } else {
            BoundingBox boundingBox = MineShaftCorridor.findCorridorSize(structurePieceAccessor, random, n, n2, n3, direction);
            if (boundingBox != null) {
                return new MineShaftCorridor(n4, random, boundingBox, direction, type);
            }
        }
        return null;
    }

    static MineShaftPiece generateAndAddPiece(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random, int n, int n2, int n3, Direction direction, int n4) {
        if (n4 > 8) {
            return null;
        }
        if (Math.abs(n - structurePiece.getBoundingBox().minX()) > 80 || Math.abs(n3 - structurePiece.getBoundingBox().minZ()) > 80) {
            return null;
        }
        MineshaftFeature.Type type = ((MineShaftPiece)structurePiece).type;
        MineShaftPiece mineShaftPiece = Ps4MineShaftPieces.createRandomShaftPiece(structurePieceAccessor, random, n, n2, n3, direction, n4 + 1, type);
        if (mineShaftPiece != null) {
            structurePieceAccessor.addPiece(mineShaftPiece);
            mineShaftPiece.addChildren(structurePiece, structurePieceAccessor, random);
        }
        return mineShaftPiece;
    }

    public static class MineShaftCrossing
    extends MineShaftPiece {
        private final Direction direction;
        private final boolean isTwoFloored;

        public MineShaftCrossing(ServerLevel serverLevel, CompoundTag compoundTag) {
            super(Ps4PieceTypes.MINE_SHAFT_CROSSING, compoundTag);
            this.isTwoFloored = compoundTag.getBoolean("tf");
            this.direction = Direction.from2DDataValue(compoundTag.getInt("D"));
        }

        @Override
        protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
            super.addAdditionalSaveData(serverLevel, compoundTag);
            compoundTag.putBoolean("tf", this.isTwoFloored);
            compoundTag.putInt("D", this.direction.get2DDataValue());
        }

        public MineShaftCrossing(int n, BoundingBox boundingBox, @Nullable Direction direction, MineshaftFeature.Type type) {
            super(Ps4PieceTypes.MINE_SHAFT_CROSSING, n, type, boundingBox);
            this.direction = direction;
            this.isTwoFloored = boundingBox.getYSpan() > 3;
        }

        @Nullable
        public static BoundingBox findCrossing(StructurePieceAccessor structurePieceAccessor, Random random, int n, int n2, int n3, Direction direction) {
            int n4 = random.nextInt(4) == 0 ? 6 : 2;
            BoundingBox boundingBox = switch (direction) {
                default -> new BoundingBox(-1, 0, -4, 3, n4, 0);
                case SOUTH -> new BoundingBox(-1, 0, 0, 3, n4, 4);
                case WEST -> new BoundingBox(-4, 0, -1, 0, n4, 3);
                case EAST -> new BoundingBox(0, 0, -1, 4, n4, 3);
            };
            boundingBox.move(n, n2, n3);
            if (structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return boundingBox;
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            int n = this.getGenDepth();
            switch (this.direction) {
                default: {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, n);
                    break;
                }
                case SOUTH: {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, n);
                    break;
                }
                case WEST: {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, n);
                    break;
                }
                case EAST: {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, n);
                }
            }
            if (this.isTwoFloored) {
                if (random.nextBoolean()) {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() - 1, Direction.NORTH, n);
                }
                if (random.nextBoolean()) {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.WEST, n);
                }
                if (random.nextBoolean()) {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.EAST, n);
                }
                if (random.nextBoolean()) {
                    Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                }
            }
        }

        @Override
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            BlockState blockState = this.type.getPlanksState();
            if (this.isTwoFloored) {
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.maxY() - 2, this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.maxY() - 2, this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3, this.boundingBox.minZ() + 1, this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
            } else {
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
            }
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            int n = this.boundingBox.minY() - 1;
            for (int i = this.boundingBox.minX(); i <= this.boundingBox.maxX(); ++i) {
                for (int j = this.boundingBox.minZ(); j <= this.boundingBox.maxZ(); ++j) {
                    this.setPlanksBlock(worldGenLevel, boundingBox, blockState, i, n, j);
                }
            }
            return true;
        }

        private void placeSupportPillar(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4) {
            if (!this.getBlock(worldGenLevel, n, n4 + 1, n3, boundingBox).isAir()) {
                this.generateBox(worldGenLevel, boundingBox, n, n2, n3, n, n4, n3, this.type.getPlanksState(), CAVE_AIR, false);
            }
        }
    }

    public static class MineShaftStairs
    extends MineShaftPiece {
        public MineShaftStairs(int n, BoundingBox boundingBox, Direction direction, MineshaftFeature.Type type) {
            super(Ps4PieceTypes.MINE_SHAFT_STAIRS, n, type, boundingBox);
            this.setOrientation(direction);
        }

        public MineShaftStairs(ServerLevel serverLevel, CompoundTag compoundTag) {
            super(Ps4PieceTypes.MINE_SHAFT_STAIRS, compoundTag);
        }

        @Nullable
        public static BoundingBox findStairs(StructurePieceAccessor structurePieceAccessor, Random random, int n, int n2, int n3, Direction direction) {
            BoundingBox boundingBox = switch (direction) {
                default -> new BoundingBox(0, -5, -8, 2, 2, 0);
                case SOUTH -> new BoundingBox(0, -5, 0, 2, 2, 8);
                case WEST -> new BoundingBox(-8, -5, 0, 0, 2, 2);
                case EAST -> new BoundingBox(0, -5, 0, 8, 2, 2);
            };
            boundingBox.move(n, n2, n3);
            if (structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return boundingBox;
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            int n = this.getGenDepth();
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    default: {
                        Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                        break;
                    }
                    case SOUTH: {
                        Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                        break;
                    }
                    case WEST: {
                        Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.WEST, n);
                        break;
                    }
                    case EAST: {
                        Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.EAST, n);
                    }
                }
            }
        }

        @Override
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);
            for (int i = 0; i < 5; ++i) {
                this.generateBox(worldGenLevel, boundingBox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, CAVE_AIR, CAVE_AIR, false);
            }
            return true;
        }
    }

    public static class MineShaftCorridor
    extends MineShaftPiece {
        private final boolean hasRails;
        private final boolean spiderCorridor;
        private boolean hasPlacedSpider;
        private final int numSections;

        public MineShaftCorridor(ServerLevel serverLevel, CompoundTag compoundTag) {
            super(Ps4PieceTypes.MINE_SHAFT_CORRIDOR, compoundTag);
            this.hasRails = compoundTag.getBoolean("hr");
            this.spiderCorridor = compoundTag.getBoolean("sc");
            this.hasPlacedSpider = compoundTag.getBoolean("hps");
            this.numSections = compoundTag.getInt("Num");
        }

        @Override
        protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
            super.addAdditionalSaveData(serverLevel, compoundTag);
            compoundTag.putBoolean("hr", this.hasRails);
            compoundTag.putBoolean("sc", this.spiderCorridor);
            compoundTag.putBoolean("hps", this.hasPlacedSpider);
            compoundTag.putInt("Num", this.numSections);
        }

        public MineShaftCorridor(int n, Random random, BoundingBox boundingBox, Direction direction, MineshaftFeature.Type type) {
            super(Ps4PieceTypes.MINE_SHAFT_CORRIDOR, n, type, boundingBox);
            this.setOrientation(direction);
            this.hasRails = random.nextInt(3) == 0;
            this.spiderCorridor = !this.hasRails && random.nextInt(23) == 0;
            this.numSections = this.getOrientation().getAxis() == Direction.Axis.Z ? boundingBox.getZSpan() / 5 : boundingBox.getXSpan() / 5;
        }

        /*
         * Enabled aggressive block sorting
         */
        @Nullable
        public static BoundingBox findCorridorSize(StructurePieceAccessor structurePieceAccessor, Random random, int n, int n2, int n3, Direction direction) {
            int n4 = random.nextInt(3) + 2;
            while (n4 > 0) {
                int n5 = n4 * 5;
                BoundingBox boundingBox = switch (direction) {
                    default -> new BoundingBox(0, 0, -(n5 - 1), 2, 2, 0);
                    case SOUTH -> new BoundingBox(0, 0, 0, 2, 2, n5 - 1);
                    case WEST -> new BoundingBox(-(n5 - 1), 0, 0, 0, 2, 2);
                    case EAST -> new BoundingBox(0, 0, 0, n5 - 1, 2, 2);
                };
                boundingBox.move(n, n2, n3);
                if (structurePieceAccessor.findCollisionPiece(boundingBox) == null) {
                    return boundingBox;
                }
                --n4;
            }
            return null;
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            block24: {
                int n = this.getGenDepth();
                int n2 = random.nextInt(4);
                Direction direction = this.getOrientation();
                if (direction != null) {
                    switch (direction) {
                        default: {
                            if (n2 <= 1) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX(), this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.minZ() - 1, direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.minZ(), Direction.WEST, n);
                                break;
                            }
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.minZ(), Direction.EAST, n);
                            break;
                        }
                        case SOUTH: {
                            if (n2 <= 1) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX(), this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.maxZ() + 1, direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.maxZ() - 3, Direction.WEST, n);
                                break;
                            }
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.maxZ() - 3, Direction.EAST, n);
                            break;
                        }
                        case WEST: {
                            if (n2 <= 1) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.minZ(), direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX(), this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                                break;
                            }
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX(), this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                            break;
                        }
                        case EAST: {
                            if (n2 <= 1) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.minZ(), direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                                break;
                            }
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + random.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                        }
                    }
                }
                if (n >= 8) break block24;
                if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                    int n3 = this.boundingBox.minZ() + 3;
                    while (n3 + 3 <= this.boundingBox.maxZ()) {
                        int n4 = random.nextInt(5);
                        if (n4 == 0) {
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY(), n3, Direction.WEST, n + 1);
                        } else if (n4 == 1) {
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY(), n3, Direction.EAST, n + 1);
                        }
                        n3 += 5;
                    }
                } else {
                    int n5 = this.boundingBox.minX() + 3;
                    while (n5 + 3 <= this.boundingBox.maxX()) {
                        int n6 = random.nextInt(5);
                        if (n6 == 0) {
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, n5, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n + 1);
                        } else if (n6 == 1) {
                            Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, n5, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n + 1);
                        }
                        n5 += 5;
                    }
                }
            }
        }

        @Override
        protected boolean createChest(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, int n, int n2, int n3, ResourceLocation resourceLocation) {
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            if (boundingBox.isInside(mutableBlockPos) && worldGenLevel.getBlockState(mutableBlockPos).isAir() && !worldGenLevel.getBlockState(((BlockPos)mutableBlockPos).below()).isAir()) {
                BlockState blockState = (BlockState)Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, random.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
                this.placeBlock(worldGenLevel, blockState, n, n2, n3, boundingBox);
                MinecartChest minecartChest = new MinecartChest(worldGenLevel.getLevel(), (double)mutableBlockPos.getX() + 0.5, (double)mutableBlockPos.getY() + 0.5, (double)mutableBlockPos.getZ() + 0.5);
                minecartChest.setLootTable(resourceLocation, random.nextLong());
                worldGenLevel.addFreshEntity(minecartChest);
                return true;
            }
            return false;
        }

        @Override
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int n;
            int n2;
            int n3;
            if (this.edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            boolean bl = false;
            int n4 = 2;
            boolean bl2 = false;
            int n5 = 2;
            int n6 = this.numSections * 5 - 1;
            BlockState blockState = this.type.getPlanksState();
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 2, 1, n6, CAVE_AIR, CAVE_AIR, false);
            this.generateMaybeBox(worldGenLevel, boundingBox, random, 0.8f, 0, 2, 0, 2, 2, n6, CAVE_AIR, CAVE_AIR, false, false);
            if (this.spiderCorridor) {
                this.generateMaybeBox(worldGenLevel, boundingBox, random, 0.6f, 0, 0, 0, 2, 1, n6, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
            }
            for (n3 = 0; n3 < this.numSections; ++n3) {
                n2 = 2 + n3 * 5;
                this.placeSupport(worldGenLevel, boundingBox, 0, 0, n2, 2, 2, random);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.1f, 0, 2, n2 - 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.1f, 2, 2, n2 - 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.1f, 0, 2, n2 + 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.1f, 2, 2, n2 + 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.05f, 0, 2, n2 - 2);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.05f, 2, 2, n2 - 2);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.05f, 0, 2, n2 + 2);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, random, 0.05f, 2, 2, n2 + 2);
                if (random.nextInt(100) == 0) {
                    this.createChest(worldGenLevel, boundingBox, random, 2, 0, n2 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                }
                if (random.nextInt(100) == 0) {
                    this.createChest(worldGenLevel, boundingBox, random, 0, 0, n2 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                }
                if (!this.spiderCorridor || this.hasPlacedSpider) continue;
                n = 1;
                int n7 = n2 - 1 + random.nextInt(3);
                BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(1, 0, n7);
                if (!boundingBox.isInside(mutableBlockPos) || !this.isInterior(worldGenLevel, 1, 0, n7, boundingBox)) continue;
                this.hasPlacedSpider = true;
                worldGenLevel.setBlock(mutableBlockPos, Blocks.SPAWNER.defaultBlockState(), 2);
                BlockEntity blockEntity = worldGenLevel.getBlockEntity(mutableBlockPos);
                if (!(blockEntity instanceof SpawnerBlockEntity)) continue;
                ((SpawnerBlockEntity)blockEntity).getSpawner().setEntityId(EntityType.CAVE_SPIDER);
            }
            for (n3 = 0; n3 <= 2; ++n3) {
                for (n2 = 0; n2 <= n6; ++n2) {
                    this.setPlanksBlock(worldGenLevel, boundingBox, blockState, n3, -1, n2);
                }
            }
            if (this.hasRails) {
                BlockState blockState2 = (BlockState)Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);
                for (n = 0; n <= n6; ++n) {
                    BlockState blockState3 = this.getBlock(worldGenLevel, 1, -1, n, boundingBox);
                    if (blockState3.isAir() || !blockState3.isSolidRender(worldGenLevel, this.getWorldPos(1, -1, n))) continue;
                    float f = this.isInterior(worldGenLevel, 1, 0, n, boundingBox) ? 0.7f : 0.9f;
                    this.maybeGenerateBlock(worldGenLevel, boundingBox, random, f, 1, 0, n, blockState2);
                }
            }
            return true;
        }

        private void placeSupport(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, Random random) {
            if (!this.isSupportingBox(worldGenLevel, boundingBox, n, n5, n4, n3)) {
                return;
            }
            BlockState blockState = this.type.getPlanksState();
            BlockState blockState2 = this.type.getFenceState();
            this.generateBox(worldGenLevel, boundingBox, n, n2, n3, n, n4 - 1, n3, (BlockState)blockState2.setValue(FenceBlock.WEST, true), CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, n5, n2, n3, n5, n4 - 1, n3, (BlockState)blockState2.setValue(FenceBlock.EAST, true), CAVE_AIR, false);
            if (random.nextInt(4) == 0) {
                this.generateBox(worldGenLevel, boundingBox, n, n4, n3, n, n4, n3, blockState, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, n5, n4, n3, n5, n4, n3, blockState, CAVE_AIR, false);
            } else {
                this.generateBox(worldGenLevel, boundingBox, n, n4, n3, n5, n4, n3, blockState, CAVE_AIR, false);
                this.maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.05f, n + 1, n4, n3 - 1, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH));
                this.maybeGenerateBlock(worldGenLevel, boundingBox, random, 0.05f, n + 1, n4, n3 + 1, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH));
            }
        }

        private void maybePlaceCobWeb(WorldGenLevel worldGenLevel, BoundingBox boundingBox, Random random, float f, int n, int n2, int n3) {
            if (this.isInterior(worldGenLevel, n, n2, n3, boundingBox) && random.nextFloat() < f ) {
                this.placeBlock(worldGenLevel, Blocks.COBWEB.defaultBlockState(), n, n2, n3, boundingBox);
            }
        }

    }

    static abstract class MineShaftPiece
    extends StructurePiece {
        protected MineshaftFeature.Type type;

        public MineShaftPiece(StructurePieceType structurePieceType, int n, MineshaftFeature.Type type, BoundingBox boundingBox) {
            super(structurePieceType, n, boundingBox);
            this.type = type;
        }

        public MineShaftPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
            this.type = MineshaftFeature.Type.byId(compoundTag.getInt("MST"));
        }

        @Override
        protected boolean canBeReplaced(LevelReader levelReader, int n, int n2, int n3, BoundingBox boundingBox) {
            BlockState blockState = this.getBlock(levelReader, n, n2, n3, boundingBox);
            return !blockState.is(this.type.getPlanksState().getBlock()) && !blockState.is(this.type.getWoodState().getBlock()) && !blockState.is(this.type.getFenceState().getBlock());
        }

        @Override
        protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
            compoundTag.putInt("MST", this.type.ordinal());
        }

        protected boolean isSupportingBox(BlockGetter blockGetter, BoundingBox boundingBox, int n, int n2, int n3, int n4) {
            for (int i = n; i <= n2; ++i) {
                if (!this.getBlock(blockGetter, i, n3 + 1, n4, boundingBox).isAir()) continue;
                return false;
            }
            return true;
        }

        protected boolean edgesLiquid(BlockGetter blockGetter, BoundingBox boundingBox) {
            int n;
            int n2;
            int n3 = Math.max(this.boundingBox.minX() - 1, boundingBox.minX());
            int n4 = Math.max(this.boundingBox.minY() - 1, boundingBox.minY());
            int n5 = Math.max(this.boundingBox.minZ() - 1, boundingBox.minZ());
            int n6 = Math.min(this.boundingBox.maxX() + 1, boundingBox.maxX());
            int n7 = Math.min(this.boundingBox.maxY() + 1, boundingBox.maxY());
            int n8 = Math.min(this.boundingBox.maxZ() + 1, boundingBox.maxZ());
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (n2 = n3; n2 <= n6; ++n2) {
                for (n = n5; n <= n8; ++n) {
                    if (blockGetter.getBlockState(mutableBlockPos.set(n2, n4, n)).getMaterial().isLiquid()) {
                        return true;
                    }
                    if (!blockGetter.getBlockState(mutableBlockPos.set(n2, n7, n)).getMaterial().isLiquid()) continue;
                    return true;
                }
            }
            for (n2 = n3; n2 <= n6; ++n2) {
                for (n = n4; n <= n7; ++n) {
                    if (blockGetter.getBlockState(mutableBlockPos.set(n2, n, n5)).getMaterial().isLiquid()) {
                        return true;
                    }
                    if (!blockGetter.getBlockState(mutableBlockPos.set(n2, n, n8)).getMaterial().isLiquid()) continue;
                    return true;
                }
            }
            for (n2 = n5; n2 <= n8; ++n2) {
                for (n = n4; n <= n7; ++n) {
                    if (blockGetter.getBlockState(mutableBlockPos.set(n3, n, n2)).getMaterial().isLiquid()) {
                        return true;
                    }
                    if (!blockGetter.getBlockState(mutableBlockPos.set(n6, n, n2)).getMaterial().isLiquid()) continue;
                    return true;
                }
            }
            return false;
        }

        protected void setPlanksBlock(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BlockState blockState, int n, int n2, int n3) {
            if (!this.isInterior(worldGenLevel, n, n2, n3, boundingBox)) {
                return;
            }
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
            if (blockState2.isAir()) {
                worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
            }
        }
    }

    public static class MineShaftRoom
    extends MineShaftPiece {
        private final List<BoundingBox> childEntranceBoxes = Lists.newLinkedList();

        public MineShaftRoom(int n, Random random, int n2, int n3, MineshaftFeature.Type type) {
            super(Ps4PieceTypes.MINE_SHAFT_ROOM, n, type, new BoundingBox(n2, 50, n3, n2 + 7 + random.nextInt(6), 54 + random.nextInt(6), n3 + 7 + random.nextInt(6)));
            this.type = type;
        }

        public MineShaftRoom(ServerLevel serverLevel, CompoundTag compoundTag) {
            super(Ps4PieceTypes.MINE_SHAFT_ROOM, compoundTag);
            BoundingBox.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.getList("Entrances", 11)).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(this.childEntranceBoxes::addAll);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            BoundingBox boundingBox;
            MineShaftPiece mineShaftPiece;
            int n;
            int n2 = this.getGenDepth();
            int n3 = this.boundingBox.getYSpan() - 3 - 1;
            if (n3 <= 0) {
                n3 = 1;
            }
            for (n = 0; n < this.boundingBox.getXSpan() && (n += random.nextInt(this.boundingBox.getXSpan())) + 3 <= this.boundingBox.getXSpan(); n += 4) {
                mineShaftPiece = Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + n, this.boundingBox.minY() + random.nextInt(n3) + 1, this.boundingBox.minZ() - 1, Direction.NORTH, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(boundingBox.minX(), boundingBox.minY(), this.boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), this.boundingBox.minZ() + 1));
            }
            for (n = 0; n < this.boundingBox.getXSpan() && (n += random.nextInt(this.boundingBox.getXSpan())) + 3 <= this.boundingBox.getXSpan(); n += 4) {
                mineShaftPiece = Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() + n, this.boundingBox.minY() + random.nextInt(n3) + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(boundingBox.minX(), boundingBox.minY(), this.boundingBox.maxZ() - 1, boundingBox.maxX(), boundingBox.maxY(), this.boundingBox.maxZ()));
            }
            for (n = 0; n < this.boundingBox.getZSpan() && (n += random.nextInt(this.boundingBox.getZSpan())) + 3 <= this.boundingBox.getZSpan(); n += 4) {
                mineShaftPiece = Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() + random.nextInt(n3) + 1, this.boundingBox.minZ() + n, Direction.WEST, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), this.boundingBox.minX() + 1, boundingBox.maxY(), boundingBox.maxZ()));
            }
            for (n = 0; n < this.boundingBox.getZSpan() && (n += random.nextInt(this.boundingBox.getZSpan())) + 3 <= this.boundingBox.getZSpan(); n += 4) {
                mineShaftPiece = Ps4MineShaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() + random.nextInt(n3) + 1, this.boundingBox.minZ() + n, Direction.EAST, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.maxX() - 1, boundingBox.minY(), boundingBox.minZ(), this.boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()));
            }
        }

        @Override
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.edgesLiquid(worldGenLevel, boundingBox)) {
                return false;
            }
            this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.minY(), this.boundingBox.maxZ(), Blocks.DIRT.defaultBlockState(), CAVE_AIR, true);
            this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY() + 1, this.boundingBox.minZ(), this.boundingBox.maxX(), Math.min(this.boundingBox.minY() + 3, this.boundingBox.maxY()), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
            for (BoundingBox boundingBox2 : this.childEntranceBoxes) {
                this.generateBox(worldGenLevel, boundingBox, boundingBox2.minX(), boundingBox2.maxY() - 2, boundingBox2.minZ(), boundingBox2.maxX(), boundingBox2.maxY(), boundingBox2.maxZ(), CAVE_AIR, CAVE_AIR, false);
            }
            this.generateUpperHalfSphere(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY() + 4, this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, false);
            return true;
        }

        @Override
        public void move(int n, int n2, int n3) {
            super.move(n, n2, n3);
            for (BoundingBox boundingBox : this.childEntranceBoxes) {
                boundingBox.move(n, n2, n3);
            }
        }

        @Override
        protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
            super.addAdditionalSaveData(serverLevel, compoundTag);
            BoundingBox.CODEC.listOf().encodeStart((DynamicOps)NbtOps.INSTANCE, this.childEntranceBoxes).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("Entrances", (Tag)tag));
        }
    }
}

