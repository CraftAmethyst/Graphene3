package org.craftamethyst.tritium.config;


import me.zcraft.tconfig.annotation.ClientOnly;
import me.zcraft.tconfig.annotation.ConfigVersion;
import me.zcraft.tconfig.annotation.Range;
import me.zcraft.tconfig.annotation.SubCategory;

import java.util.Arrays;
import java.util.List;

@ConfigVersion(1)
public class TritiumConfigBase {
   /* @SubCategory("Performance")
    public Performance performance = new Performance();

    @ClientOnly
    @SubCategory("Rendering")
    public Rendering rendering = new Rendering();

    @ClientOnly
    @SubCategory("Client Optimizations")
    public ClientOptimizations clientOptimizations = new ClientOptimizations();

    @SubCategory("Network")
    public Network network = new Network();
*/
    @SubCategory("Entities")
    public Entities entities = new Entities();

  /*  @SubCategory("Tech Optimizations")
    public TechOptimizations techOptimizations = new TechOptimizations();

    @ClientOnly
    @SubCategory("Fixes")
    public Fixes fixes = new Fixes();

    @SubCategory("Server Performance")
    public ServerPerformance serverPerformance = new ServerPerformance();
*/
    /*
    public static class Performance {
        @SubCategory("FastFurnace")
        public FastFurnace fastFurnace = new FastFurnace();
        @SubCategory("BlockStateCache")
        public BlockStateCache blockStateCache = new BlockStateCache();
        @SubCategory("EndIslandOptimization")
        public EndIslandOptimization endIslandOptimization = new EndIslandOptimization();
        @SubCategory("MathOptimizations")
        public MathOptimizations mathOptimizations = new MathOptimizations();
        @SubCategory("LightingOptimizations")
        public LightingOptimizations lightingOptimizations = new LightingOptimizations();

        public static class FastFurnace {
            public static boolean fastFurnace = true;
        }
        public static class BlockStateCache {
            public static boolean blockStatePairKeyCache = true;
        }
        public static class EndIslandOptimization {
            public static boolean enableEndIslandOptimization = true;
        }
        public static class MathOptimizations {
            public static boolean enableMathOptimizations = true;
            public static boolean optimizeLerpFunctions = true;
            public static boolean optimizeLengthSquared = true;
            public static boolean optimizeRandomFunctions = true;
        }
        public static class LightingOptimizations {
            public static boolean enableLightingOptimizations = true;
            public static boolean optimizeDynamicGraph = true;
            public static boolean bambooLight = true;
        }
    }

    @ClientOnly
    public static class Rendering {
        @SubCategory("ChestRenderingOpt")
        public CRO cro = new CRO();
        @SubCategory("FastBlit")
        public FastBlit fastBlit = new FastBlit();
        // GPU Plus
        @SubCategory("GPUPlus")
        public static GpuPlus GpuPlus = new GpuPlus();

        public static class CRO {
            public static boolean chest_rendering_optimization = false;
        }

        public static class FastBlit {
            public static boolean fastBlit = true;
        }

        public static class GpuPlus {
            public static boolean gpuPlus = true;
            public static boolean gpuPlusVbo = true;
            public static boolean gpuPlusIndex = true;
        }

        @SubCategory("Reflex")
        public Reflex reflex = new Reflex();

        // Entity and Block Entity Culling
        @SubCategory("Entity Culling")
        public EntityCulling entityCulling = new EntityCulling();

        // Leaf Culling
        @SubCategory("Leaf Culling")
        public LeafCulling leafCulling = new LeafCulling();

        public static class Reflex {
            public static boolean enableReflex = true;
            public static boolean reflexDebug = false;
            @Range(min=-100000, max=100000)
            public static int reflexOffsetNs = 0;
            @Range(min=0, max=1000)
            public static int MAX_FPS = 0;
        }
        public static class EntityCulling {
            public static boolean enableCulling = true;
            public static boolean enableBlockEntityCulling = true;
            public static boolean enableTickStopping = false;
            public static boolean enableNameTagCulling = true;

            public static List<String> entityBlacklist = Arrays.asList("minecraft:player", "minecraft:villager");
        }

        public static class LeafCulling {
            public static boolean enableLeafCulling = true;
            public static boolean hideInnerLeaves = false;
            public static boolean enableFaceOcclusionCulling = true;
        }
    }

    @ClientOnly
    public static class ClientOptimizations {
        @SubCategory("FastLanguage")
        public static FL FL= new FL();
        @SubCategory("FastResourcePack")
        public static FastResourcePack FastResourcePack= new FastResourcePack();

        public static class FL {
            public static boolean fastLanguageSwitch = true;
        }

        public static class FastResourcePack {
            public static boolean resourcePackCache = true;
        }
        @SubCategory("dynamicFPS")
        public DynamicFPS dynamicFPS = new DynamicFPS();

        public static class DynamicFPS{
            public static boolean enable = true;
            @Range(min = 1)
            public static int minimizedFPS = 1;
        }

    }

    public static class Network {
        // Future network optimizations will be added here
    }
*/
    public static class Entities {
        @SubCategory("EntityOpt")
        public EntityOpt entityOpt = new EntityOpt();
        public static class EntityOpt {
            public static boolean optimizeEntities = true;
            //public static boolean ite = true;

            @Range(min = 1, max = 256)
            public static int horizontalRange = 64;

            @Range(min = 1, max = 256)
            public static int verticalRange = 32;

            public static List<String> entityWhitelist = Arrays.asList("minecraft:ender_dragon");

        }

        /*@SubCategory("entityStacking")
        public EntityStacking entityStacking = new EntityStacking();

        public static class EntityStacking{
            public static boolean enable = true;
            public static boolean lockMaxedStacks = true;
            public static boolean showStackCount = true;
            @Range(min = 0)
            public static int maxStackSize = 0;
            @Range(min = 0.1,max = 10)
            public static double mergeDistance = 1.5;
            @Range(min = 0,max = 2)
            public static int listMode=0;
            public static List<String> itemList = Arrays.asList(
                    "minecraft:item"
            );
        }*/
    }
/*
    public static class TechOptimizations {
        @SubCategory("Create Optimizations")
        public CreateOptimizations createOptimizations = new CreateOptimizations();

        public static class CreateOptimizations {
            public static boolean enableRailOffloading = true;
        }
    }

    @ClientOnly
    public static class Fixes {
        @SubCategory("Button Fix")
        public ButtonFix buttonFix = new ButtonFix();
       @SubCategory("No GLog")
       public NoGLog noGLog = new NoGLog();
        @SubCategory("Memory Leak Fix")
        public MemoryLeakFix memoryLeakFix = new MemoryLeakFix();
        @SubCategory("Bee Fixes")
        public BeeFixes beeFixes = new BeeFixes();


        public static class ButtonFix {
            public static boolean buttonFix = true;
        }

        public static class NoGLog {
            public static boolean noGLog = true;
        }

        public static class MemoryLeakFix {
            public static boolean AE2WTLibCreativeTabLeakFix = true;
            public static boolean ScreenshotByteBufferLeakFix = true;
        }
        public static class BeeFixes {
            public static boolean enableBeeFixes = true;
            public static boolean fixWeatherInNether = true;
            public static boolean fixBeeRandomPos = true;
            public static boolean fixBeeGravity = true;
            public static boolean fixBeeTurtleEgg = true;
        }
    }

    public static class ServerPerformance {
        @SubCategory("Noise Sampling Cache")
        public NoiseSamplingCache noiseSamplingCache = new NoiseSamplingCache();

        @SubCategory("Jigsaw Optimizations")
        public JigsawOptimizations jigsawOptimizations = new JigsawOptimizations();

        public static class NoiseSamplingCache {
            public static boolean noiseSamplingCache = true;
        }

        public static class JigsawOptimizations {
            public static boolean enableJigsawOptimizations = true;
            public static boolean enableOctreeCollisionDetection = true;
            public static boolean enableFastWeightedSampling = true;
            public static boolean enableStructureBlockFiltering = true;
            public static boolean enableJigsawGenerationCheck = true;
        }
    }

 */
}