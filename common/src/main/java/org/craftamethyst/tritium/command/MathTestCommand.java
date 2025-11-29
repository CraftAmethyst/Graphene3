package org.craftamethyst.tritium.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

public class MathTestCommand {
    private static final int WARMUP_ITERATIONS = 100;
    private static final int TEST_ITERATIONS = 10;
    private static final int TEST_RANGE = 30;
    private static final Executor TEST_EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("xtktool")
                        .then(Commands.literal("test")
                                .executes(ctx -> runTests(ctx.getSource()))
                        ));
    }

    private static int runTests(CommandSourceStack source) {
        source.sendSystemMessage(Component.literal("§6[XTKMathTool] §fStarting advanced math performance tests..."));
        source.sendSystemMessage(Component.literal("§7Preparing environment..."));

        boolean originalDoDaylight = source.getLevel().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
        boolean originalDoMobSpawning = source.getLevel().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        source.getLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        source.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);

        try {
            // Warmup
            source.sendSystemMessage(Component.literal("§7Warming up..."));
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                runAllTests(true);
            }

            // Actual tests
            source.sendSystemMessage(Component.literal("§7Running tests..."));
            List<TestResult> results = runAllTests(false);

            // Calculate average score
            double totalScore = 0;
            for (TestResult result : results) {
                totalScore += result.score;
            }
            double finalScore = totalScore / results.size();

            // Display results
            source.sendSystemMessage(Component.literal("§6============= Math Performance Results ============="));
            for (TestResult result : results) {
                source.sendSystemMessage(Component.literal(String.format("§b%-30s §fScore: §6%.1f §f(§7Avg: %.3fms§f)",
                        result.name, result.score, result.avgTimeMs)));
            }

            source.sendSystemMessage(Component.literal("§6-----------------------------------------------"));
            source.sendSystemMessage(Component.literal("§6Total Score: §f" + String.format("%.1f", finalScore)));

            String performanceLevel = getPerformanceLevel(finalScore);
            source.sendSystemMessage(Component.literal("§6Performance Level: " + performanceLevel));
            source.sendSystemMessage(Component.literal("§6Tests completed!"));
        } finally {
            source.getLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(originalDoDaylight, null);
            source.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(originalDoMobSpawning, null);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static List<TestResult> runAllTests(boolean warmup) {
        List<CompletableFuture<TestResult>> futures = new ArrayList<>();

        futures.add(runTestAsync("Sin", MathTestCommand::testSin, warmup));
        futures.add(runTestAsync("Cos", MathTestCommand::testCos, warmup));
        futures.add(runTestAsync("Floor (float)", MathTestCommand::testFloorFloat, warmup));
        futures.add(runTestAsync("Floor (double)", MathTestCommand::testFloorDouble, warmup));
        futures.add(runTestAsync("Ceil (float)", MathTestCommand::testCeilFloat, warmup));
        futures.add(runTestAsync("Ceil (double)", MathTestCommand::testCeilDouble, warmup));
        futures.add(runTestAsync("Abs (float)", MathTestCommand::testAbsFloat, warmup));
        futures.add(runTestAsync("Abs (int)", MathTestCommand::testAbsInt, warmup));
        futures.add(runTestAsync("Clamp (float)", MathTestCommand::testClampFloat, warmup));
        futures.add(runTestAsync("Clamp (int)", MathTestCommand::testClampInt, warmup));
        futures.add(runTestAsync("Clamp (double)", MathTestCommand::testClampDouble, warmup));
        futures.add(runTestAsync("Lerp (float)", MathTestCommand::testLerpFloat, warmup));
        futures.add(runTestAsync("Lerp (double)", MathTestCommand::testLerpDouble, warmup));
        futures.add(runTestAsync("Lerp (int)", MathTestCommand::testLerpInt, warmup));
        futures.add(runTestAsync("WrapDegrees (float)", MathTestCommand::testWrapDegreesFloat, warmup));
        futures.add(runTestAsync("WrapDegrees (double)", MathTestCommand::testWrapDegreesDouble, warmup));
        futures.add(runTestAsync("WrapDegrees (int)", MathTestCommand::testWrapDegreesInt, warmup));
        futures.add(runTestAsync("InvSqrt", MathTestCommand::testInvSqrt, warmup));
        futures.add(runTestAsync("Sqrt (float)", MathTestCommand::testSqrtFloat, warmup));
        futures.add(runTestAsync("Sqrt (double)", MathTestCommand::testSqrtDouble, warmup));
        futures.add(runTestAsync("Atan2", MathTestCommand::testAtan2, warmup));
        futures.add(runTestAsync("HSV to RGB", MathTestCommand::testHsvToRgb, warmup));
        futures.add(runTestAsync("Smallest PowerOfTwo", MathTestCommand::testSmallestPowerOfTwo, warmup));
        futures.add(runTestAsync("Is PowerOfTwo", MathTestCommand::testIsPowerOfTwo, warmup));
        futures.add(runTestAsync("FloorDiv", MathTestCommand::testFloorDiv, warmup));
        futures.add(runTestAsync("Mod (int)", MathTestCommand::testModInt, warmup));
        futures.add(runTestAsync("Mod (float)", MathTestCommand::testModFloat, warmup));

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<TestResult> results = new ArrayList<>();
        for (CompletableFuture<TestResult> future : futures) {
            if (future.isDone() && !future.isCompletedExceptionally()) {
                results.add(future.join());
            }
        }
        return results;
    }

    private static CompletableFuture<TestResult> runTestAsync(String name, DoubleSupplier test, boolean warmup) {
        return CompletableFuture.supplyAsync(() -> {
            int iterations = warmup ? WARMUP_ITERATIONS : TEST_ITERATIONS;
            long totalTime = 0;

            for (int i = 0; i < iterations; i++) {
                long start = System.nanoTime();
                test.getAsDouble();
                totalTime += System.nanoTime() - start;
            }

            double avgTimeNs = totalTime / (double) iterations;
            double baseTimeNs = getBaseTimeForTest(name);

            // ✅ 取消100分上限
            double score = (baseTimeNs / avgTimeNs) * 100;

            return new TestResult(name, avgTimeNs / 1e6, score);
        }, TEST_EXECUTOR);
    }

    private static double getBaseTimeForTest(String name) {
        return switch (name) {
            case "Sin" -> 500_000;
            case "Cos" -> 500_000;
            case "Floor (float)" -> 200_000;
            case "Floor (double)" -> 200_000;
            case "Ceil (float)" -> 200_000;
            case "Ceil (double)" -> 200_000;
            case "Abs (float)" -> 150_000;
            case "Abs (int)" -> 100_000;
            case "Clamp (float)" -> 250_000;
            case "Clamp (int)" -> 200_000;
            case "Clamp (double)" -> 250_000;
            case "Lerp (float)" -> 300_000;
            case "Lerp (double)" -> 300_000;
            case "Lerp (int)" -> 300_000;
            case "WrapDegrees (float)" -> 300_000;
            case "WrapDegrees (double)" -> 300_000;
            case "WrapDegrees (int)" -> 300_000;
            case "InvSqrt" -> 400_000;
            case "Sqrt (float)" -> 400_000;
            case "Sqrt (double)" -> 500_000;
            case "Atan2" -> 800_000;
            case "HSV to RGB" -> 600_000;
            case "Smallest PowerOfTwo" -> 200_000;
            case "Is PowerOfTwo" -> 150_000;
            case "FloorDiv" -> 200_000;
            case "Mod (int)" -> 200_000;
            case "Mod (float)" -> 200_000;
            default -> 300_000;
        };
    }

    private static String getPerformanceLevel(double score) {
        if (score >= 90) return "§aExcellent (优秀)";
        if (score >= 75) return "§eGood (良好)";
        if (score >= 50) return "§6Acceptable (可接受)";
        return "§cPoor (较差)";
    }

    private static class TestResult {
        final String name;
        final double avgTimeMs;
        final double score;

        TestResult(String name, double avgTimeMs, double score) {
            this.name = name;
            this.avgTimeMs = avgTimeMs;
            this.score = score;
        }
    }

    // ==== 测试方法实现（保持不变） ====
    private static double testSin() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.sin(i * 0.3f); return sum; }
    private static double testCos() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.cos(i * 0.3f); return sum; }
    private static double testFloorFloat() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.floor(i * 0.3f); return sum; }
    private static double testFloorDouble() { double sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.floor(i * 0.3); return sum; }
    private static double testCeilFloat() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.ceil(i * 0.3f); return sum; }
    private static double testCeilDouble() { double sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.ceil(i * 0.3); return sum; }
    private static double testAbsFloat() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.abs(i * 0.3f); return sum; }
    private static double testAbsInt() { int sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Math.abs(i); return sum; }
    private static double testClampFloat() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.clamp(i * 0.3f, -10, 10); return sum; }
    private static double testClampInt() { int sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.clamp(i, -100, 100); return sum; }
    private static double testClampDouble() { double sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.clamp(i * 0.3, -10, 10); return sum; }
    private static double testLerpFloat() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.lerp(0.5f, i * 0.3f, (i + 1) * 0.3f); return sum; }
    private static double testLerpDouble() { double sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.lerp(0.5, i * 0.3, (i + 1) * 0.3); return sum; }
    private static double testLerpInt() { int sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.lerpInt(0.5f, i, i + 100); return sum; }
    private static double testWrapDegreesFloat() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.wrapDegrees(i * 10.0f); return sum; }
    private static double testWrapDegreesDouble() { double sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.wrapDegrees(i * 10.0); return sum; }
    private static double testWrapDegreesInt() { int sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Mth.wrapDegrees(i * 10); return sum; }
    private static double testInvSqrt() { float sum = 0; for (int i = 1; i < TEST_RANGE; i++) sum += Mth.fastInvSqrt(i * 0.3f); return sum; }
    private static double testSqrtFloat() { float sum = 0; for (int i = 0; i < TEST_RANGE; i++) sum += Mth.sqrt(i * 0.3f); return sum; }
    private static double testSqrtDouble() { double sum = 0; for (int i = 0; i < TEST_RANGE; i++) sum += Math.sqrt(i * 0.3); return sum; }
    private static double testAtan2() { double sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) sum += Math.atan2(i * 0.3, (i + 1) * 0.3); return sum; }
    private static double testHsvToRgb() { float sum = 0; for (int i = 0; i < TEST_RANGE; i++) { int c = Mth.hsvToRgb(i / (float) TEST_RANGE, 0.5f, 0.5f); sum += (c & 0xFF) / 255.0f; } return sum; }
    private static double testSmallestPowerOfTwo() { int sum = 0; for (int i = 0; i < TEST_RANGE; i++) sum += Mth.smallestEncompassingPowerOfTwo(i); return sum; }
    private static double testIsPowerOfTwo() { int sum = 0; for (int i = 0; i < TEST_RANGE; i++) if (Mth.isPowerOfTwo(i)) sum++; return sum; }
    private static double testFloorDiv() { int sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) if (i != 0) sum += Math.floorDiv(i * 10, i); return sum; }
    private static double testModInt() { int sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) if (i != 0) sum += Math.floorMod(i * 10, i); return sum; }
    private static double testModFloat() { float sum = 0; for (int i = -TEST_RANGE; i < TEST_RANGE; i++) if (i != 0) sum += Math.floorMod((int)(i * 10.0f), i); return sum; }
}