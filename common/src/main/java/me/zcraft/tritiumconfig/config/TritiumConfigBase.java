package me.zcraft.tritiumconfig.config;

import me.zcraft.tritiumconfig.annotation.ClientOnly;

public class TritiumConfigBase {
    public Performance performance = new Performance();

    @ClientOnly
    public Rendering rendering = new Rendering();

    public Network network = new Network();
    public Entities entities = new Entities();
    public TechOptimizations techOptimizations = new TechOptimizations();
    public Fixes fixes = new Fixes();
    public ServerPerformance serverPerformance = new ServerPerformance();

    public static class Performance {
        public boolean bambooLight = true;
    }

    @ClientOnly
    public static class Rendering {

    }

    public static class Network {

    }

    public static class Entities {

    }

    public static class TechOptimizations {

    }

    @ClientOnly
    public static class Fixes {
        public boolean buttonFix = true;
        public boolean noGLog = true;
    }

    public static class ServerPerformance {
        public boolean noiseSamplingCache = true;
    }
}