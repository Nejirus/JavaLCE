package dev.legacyconsole.worldgen.ps4.structure;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
public final class Ps4PieceTypes {
 private Ps4PieceTypes(){}
 private static StructurePieceType register(StructurePieceType factory,String id){return Registry.register(Registry.STRUCTURE_PIECE,new ResourceLocation("legacyconsole","ps4_"+id),factory);}
 public static final StructurePieceType DESERT_PYRAMID_PIECE=register(Ps4DesertPyramidPiece::new,"tedp");
 public static final StructurePieceType JUNGLE_PYRAMID_PIECE=register(Ps4JunglePyramidPiece::new,"tejp");
 public static final StructurePieceType MINE_SHAFT_CORRIDOR=register(Ps4MineShaftPieces.MineShaftCorridor::new,"mscorridor");
 public static final StructurePieceType MINE_SHAFT_CROSSING=register(Ps4MineShaftPieces.MineShaftCrossing::new,"mscrossing");
 public static final StructurePieceType MINE_SHAFT_ROOM=register(Ps4MineShaftPieces.MineShaftRoom::new,"msroom");
 public static final StructurePieceType MINE_SHAFT_STAIRS=register(Ps4MineShaftPieces.MineShaftStairs::new,"msstairs");
 public static final StructurePieceType OCEAN_MONUMENT_BUILDING=register(Ps4OceanMonumentPieces.MonumentBuilding::new,"omb");
 public static final StructurePieceType OCEAN_MONUMENT_CORE_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentCoreRoom::new,"omcr");
 public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_XY_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentDoubleXYRoom::new,"omdxyr");
 public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_X_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentDoubleXRoom::new,"omdxr");
 public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_YZ_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentDoubleYZRoom::new,"omdyzr");
 public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_Y_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentDoubleYRoom::new,"omdyr");
 public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_Z_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentDoubleZRoom::new,"omdzr");
 public static final StructurePieceType OCEAN_MONUMENT_ENTRY_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentEntryRoom::new,"omentry");
 public static final StructurePieceType OCEAN_MONUMENT_PENTHOUSE=register(Ps4OceanMonumentPieces.OceanMonumentPenthouse::new,"ompenthouse");
 public static final StructurePieceType OCEAN_MONUMENT_SIMPLE_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentSimpleRoom::new,"omsimple");
 public static final StructurePieceType OCEAN_MONUMENT_SIMPLE_TOP_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentSimpleTopRoom::new,"omsimplet");
 public static final StructurePieceType OCEAN_MONUMENT_WING_ROOM=register(Ps4OceanMonumentPieces.OceanMonumentWingRoom::new,"omwr");
 public static final StructurePieceType STRONGHOLD_CHEST_CORRIDOR=register(Ps4StrongholdPieces.ChestCorridor::new,"shcc");
 public static final StructurePieceType STRONGHOLD_FILLER_CORRIDOR=register(Ps4StrongholdPieces.FillerCorridor::new,"shfc");
 public static final StructurePieceType STRONGHOLD_FIVE_CROSSING=register(Ps4StrongholdPieces.FiveCrossing::new,"sh5c");
 public static final StructurePieceType STRONGHOLD_LEFT_TURN=register(Ps4StrongholdPieces.LeftTurn::new,"shlt");
 public static final StructurePieceType STRONGHOLD_LIBRARY=register(Ps4StrongholdPieces.Library::new,"shli");
 public static final StructurePieceType STRONGHOLD_PORTAL_ROOM=register(Ps4StrongholdPieces.PortalRoom::new,"shpr");
 public static final StructurePieceType STRONGHOLD_PRISON_HALL=register(Ps4StrongholdPieces.PrisonHall::new,"shph");
 public static final StructurePieceType STRONGHOLD_RIGHT_TURN=register(Ps4StrongholdPieces.RightTurn::new,"shrt");
 public static final StructurePieceType STRONGHOLD_ROOM_CROSSING=register(Ps4StrongholdPieces.RoomCrossing::new,"shrc");
 public static final StructurePieceType STRONGHOLD_STAIRS_DOWN=register(Ps4StrongholdPieces.StairsDown::new,"shsd");
 public static final StructurePieceType STRONGHOLD_START=register(Ps4StrongholdPieces.StartPiece::new,"shstart");
 public static final StructurePieceType STRONGHOLD_STRAIGHT=register(Ps4StrongholdPieces.Straight::new,"shs");
 public static final StructurePieceType STRONGHOLD_STRAIGHT_STAIRS_DOWN=register(Ps4StrongholdPieces.StraightStairsDown::new,"shssd");
 public static final StructurePieceType SWAMPLAND_HUT=register(Ps4SwamplandHutPiece::new,"tesh");
 public static final StructurePieceType TEMPLATE=register(Ps4TemplatePiece::new,"template");
 public static final StructurePieceType RUIN=register(Ps4RuinPiece::new,"ruin");
 public static final StructurePieceType TREASURE=register(Ps4TreasurePiece::new,"treasure");
 public static void initialize(){}
}
