# Tritium TODO

- [x]  **[Full version]** Fast Language: Dramatically speed up language switching by intercepting resource bundle reloading when switching languages
- [x]  **[Except NeoForge]** Fast Event: Replaced generated classes with construction lambdas to speed up construction event listening
- [ ]  **[Full version]** Leaf Culling: Ported Optifine smart foliage options
- [ ]  **[Full version]** Optimization of entity stacking: Similar to Spigot's stacking optimization of entities (referring to dropped items and experience balls), if entities in an area are too dense, then these dropped items will be merged directly
- [x]  **[Full version]** Optimization of entity :Stop the ticking of distant entities
- [ ]  **[Full version]** Chest rendering optimization: Removed dynamic models of chests, leaving them rendered as static block geometry (at the expense of no animation when chests open)
- [ ]  **[Full version]** Distant object stop tick: If an entity or fluid is too far away from the player, it will no longer tick
- [ ]  **[Full version]** Dynamic FPS: Automatically reduce framerate when the game is unfocused (down to 1 FPS) or minimized (not rendering at all)
- [ ]  **[Full version]** Memory Leak Fix: Fixed various memory leaks, such as the game crashing after a while (even if you did nothing)
- [x]  **[Full version]** Mask GL Error: Fix GL error log refreshes even though there is no substantive error.
- [ ]  **[Full version]** GPU Plus: Fix the game's video memory leak problem through various methods, and also bring a variety of new OpenGL 4+ features
- [ ]  **[Windows only]** GPU entity collision optimization: By placing the physical AABB collision calculation on the GPU, the main thread pressure is shared, achieving significant performance improvement in dense biological scenes.
- [x]  **[Forge only]** Quickly save the world: Make the world saving thread asynchronous to greatly increase the speed of saving the world
- [x]  bugfix(vl): MC-259387
- [x]  config: add config screen
- [ ]  techOpt(SFM): Factory Manager
- [ ]  techOpt(gt)
