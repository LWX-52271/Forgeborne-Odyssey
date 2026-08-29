package com.lwx.forgeborneodyssey.blocks.anvils;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.world.OreQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 石砧方块实体——极简实现，仅存储一个物品并保证客户端同步
 */
public class AnvilBlockEntity extends BlockEntity {

    private ItemStack storedItem = ItemStack.EMPTY;
    private int ambientSoundCooldown = 0; // 环境音效冷却计时器
    
    // 锻造相关数据
    private int hitCount = 0; // 锻打次数计数器
    
    // 雕刻相关数据
    private int carveCount = 0; // 雕刻次数计数器
    
    // 渲染拉伸效果相关数据（客户端）
    private float stretchFactor = 0.0f; // 拉伸因子，随每次敲击累积
    
    // 矿石破碎相关数据
    private int oreCrushCount = 0; // 矿石破碎敲击次数
    private int oreCrushRequired = 0; // 矿石破碎所需总次数（1-2，随机）

    // 打制石器三阶段流程：打台面(Platform) → 剥片(Flaking) → 修整(Retouch)
    // 或：打台面(Platform) → 修整石核(CoreShaping) → 大工具
    private int knappingHitCount = 0; // 敲击次数（剥片或修整石核共用）
    private int knappingRequiredHits = 0; // 所需总次数
    private boolean knappingPlatformCreated = false; // 是否已打出台面（阶段一完成）
    private boolean isCoreShaping = false; // true=修整石核路线，false=剥片路线
    private long knappingLastHitTick = 0;  // 上次敲击的tick，用于检测连续急敲
    private int knappingFragility = 0;    // 急敲脆弱度 0-100，达到100则碎裂
    private static final int RAPID_HIT_THRESHOLD = 10; // 间隔低于此值视为急敲（0.5秒）
    
    // 矿碎块 -> 对应颗粒的映射表（不含锡）
    private static final Map<Item, Item> ORE_CHUNK_TO_GRAIN = new HashMap<>();
    
    static {
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_CHALCOPYRITE.get(), ModItems.CHALCOPYRITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_BORNITE.get(), ModItems.BORNITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_CHALCOCITE.get(), ModItems.CHALCOCITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_COVELLITE.get(), ModItems.COVELLITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_CUBANITE.get(), ModItems.CUBANITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_MALACHITE.get(), ModItems.MALACHITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_AZURITE.get(), ModItems.AZURITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_CUPRITE.get(), ModItems.CUPRITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_TENORITE.get(), ModItems.TENORITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_CHALCANTHITE.get(), ModItems.CHALCANTHITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_BROCHANTITE.get(), ModItems.BROCHANTITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_MIXED_COPPER.get(), ModItems.MIXED_COPPER_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_NATIVE_COPPER.get(), ModItems.NATIVE_COPPER_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_TETRAHEDRITE.get(), ModItems.TETRAHEDRITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_TENNANTITE.get(), ModItems.TENNANTITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_TORBERNITE.get(), ModItems.TORBERNITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_CUPROVANADITE.get(), ModItems.CUPROVANADITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_CHRYSOCOLLA.get(), ModItems.CHRYSOCOLLA_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_MAGNETITE.get(), ModItems.MAGNETITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_SCHEELITE.get(), ModItems.SCHEELITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_GALENA.get(), ModItems.GALENA_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_SPHALERITE.get(), ModItems.SPHALERITE_GRAIN.get());
        ORE_CHUNK_TO_GRAIN.put(ModItems.RAW_MOLYBDENITE.get(), ModItems.MOLYBDENITE_GRAIN.get());
    }

    // 石器打制产出权重表（燧石片修整 → 小工具，刀头最常见，镰刀头最稀有）
    private static final java.util.List<KnappingOutput> KNAPPING_OUTPUTS = java.util.List.of(
        new KnappingOutput(() -> new ItemStack(ModItems.FLINT_KNIFE_HEAD.get()), 40),
        new KnappingOutput(() -> new ItemStack(ModItems.FLINT_ARROWHEAD.get(), 2), 25),
        new KnappingOutput(() -> new ItemStack(ModItems.FLINT_SPEARHEAD.get()), 15),
        new KnappingOutput(() -> new ItemStack(ModItems.FLINT_SHOVEL_HEAD.get()), 10),
        new KnappingOutput(() -> new ItemStack(ModItems.FLINT_SICKLE_HEAD.get()), 5),
        new KnappingOutput(() -> new ItemStack(ModItems.STONE_AXE_HEAD.get()), 5)
    );

    // 修整石核产出权重表（石核本体 → 大工具，斧头最常见）
    private static final java.util.List<CoreShapingOutput> CORE_SHAPING_OUTPUTS = java.util.List.of(
        new CoreShapingOutput(() -> new ItemStack(ModItems.STONE_AXE_HEAD.get()), 40),
        new CoreShapingOutput(() -> new ItemStack(ModItems.FLINT_SHOVEL_HEAD.get()), 30),
        new CoreShapingOutput(() -> new ItemStack(ModItems.STONE_HOE_HEAD.get()), 30)
    );

    private record KnappingOutput(java.util.function.Supplier<ItemStack> supplier, int weight) {}
    private record CoreShapingOutput(java.util.function.Supplier<ItemStack> supplier, int weight) {}

    public AnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ANVIL_BLOCK_ENTITY.get(), pos, state);
        // 初始化环境音效冷却时间为随机值，避免所有石砧同时播放音效
        this.ambientSoundCooldown = level != null ? level.random.nextInt(100) : 0;
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    /**
     * 检查是否可以放置该物品
     * 允许放置所有与锻打有关的金属物品，以及矿碎块（不含锡石/含锡砂土）：
     * - 胚料：金坯料、银坯料、铜坯料、软化铜坯料
     * - 软化金属条：软化铜条
     * - 金属片：金片、银片、铜片
     * - 金属弯片：金弯片、银弯片、铜弯片
     * - 金属槽片：金槽片、银槽片、铜槽片
     * - 金属斧头：金斧头、银斧头、铜斧头
     * - 金属刀刃：金剑刃、银剑刃、铜剑刃
     * - 金属刀：原始金刀、原始银刀、原始铜刀
     * - 金属针：金针、银针、铜针
     * - 其他金属制品：金珠、银珠、铜珠、金条、银条、铜环、铜钩
     * - 饰针胸甲：金饰针胸甲、银饰针胸甲、铜饰针胸甲
     * - 打制武器：打制铜剑、打制银剑、打制金剑
     * - 打制工具：打制铜斧、打制银斧、打制金斧
     * - 铜鱼竿
     * - 矿碎块（18种铜矿 + 5种矽卡岩矿，不含锡石/含锡砂土）
     * 
     * 注意：金属碎片（copper/silver/gold fragment）不允许放置在石砧上，因为它们无法再次锻造
     */
    public boolean canPlaceItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 允许放置矿碎块（不含锡石/含锡砂土）
        if (isRawOreChunk(stack)) {
            return true;
        }

        // 允许放置燧石砾石、石核或燧石片，用于石器打制
        if (stack.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get()) ||
            stack.is(ModItems.FLINT_PEBBLE.get()) ||
            stack.is(ModItems.STONE_CORE.get()) ||
            stack.is(ModItems.FLINT_FLAKE.get())) {
            return true;
        }
        
        // 允许放置所有与锻打有关的金属物品
        return stack.is(ModItems.GOLD_BILLET.get()) ||
               stack.is(ModItems.SILVER_BILLET.get()) ||
               stack.is(ModItems.COPPER_BILLET.get()) ||
               stack.is(ModItems.SOFT_COPPER_BILLET.get()) ||
               stack.is(ModItems.SOFT_COPPER_STRIP.get()) ||
               stack.is(ModItems.GOLD_SHEET.get()) ||
               stack.is(ModItems.SILVER_SHEET.get()) ||
               stack.is(ModItems.COPPER_SHEET.get()) ||
               stack.is(ModItems.GOLD_CURVE.get()) ||
               stack.is(ModItems.SILVER_CURVE.get()) ||
               stack.is(ModItems.COPPER_CURVE.get()) ||
               stack.is(ModItems.GOLD_SLOT.get()) ||
               stack.is(ModItems.SILVER_SLOT.get()) ||
               stack.is(ModItems.COPPER_SLOT.get()) ||
               stack.is(ModItems.GOLD_AXE.get()) ||
               stack.is(ModItems.SILVER_AXE.get()) ||
               stack.is(ModItems.COPPER_AXE.get()) ||
               stack.is(ModItems.GOLD_SWORD_BLADE.get()) ||
               stack.is(ModItems.SILVER_SWORD_BLADE.get()) ||
               stack.is(ModItems.COPPER_SWORD_BLADE.get()) ||
               stack.is(ModItems.GOLD_KNIFE.get()) ||
               stack.is(ModItems.SILVER_KNIFE.get()) ||
               stack.is(ModItems.COPPER_KNIFE.get()) ||
               stack.is(ModItems.GOLD_PIN.get()) ||
               stack.is(ModItems.SILVER_PIN.get()) ||
               stack.is(ModItems.COPPER_PIN.get()) ||
               stack.is(ModItems.GOLD_BEAD.get()) ||
               stack.is(ModItems.SILVER_BEAD.get()) ||
               stack.is(ModItems.COPPER_BEAD.get()) ||
               stack.is(ModItems.GOLD_BAR.get()) ||
               stack.is(ModItems.SILVER_BAR.get()) ||
               stack.is(ModItems.COPPER_RING.get()) ||
               stack.is(ModItems.COPPER_HOOK.get()) ||
               stack.is(ModItems.GOLD_PIN_CHESTPLATE.get()) ||
               stack.is(ModItems.SILVER_PIN_CHESTPLATE.get()) ||
               stack.is(ModItems.COPPER_PIN_CHESTPLATE.get()) ||
               stack.is(ModItems.WROUGHT_COPPER_SWORD.get()) ||
               stack.is(ModItems.WROUGHT_SILVER_SWORD.get()) ||
               stack.is(ModItems.WROUGHT_GOLD_SWORD.get()) ||
               stack.is(ModItems.WROUGHT_COPPER_AXE.get()) ||
               stack.is(ModItems.WROUGHT_SILVER_AXE.get()) ||
               stack.is(ModItems.WROUGHT_GOLD_AXE.get()) ||
               stack.is(ModItems.COPPER_FISHING_ROD.get());
    }
    
    /**
     * 检查物品是否为矿碎块（不含锡石/含锡砂土）
     */
    private boolean isRawOreChunk(ItemStack stack) {
        return ORE_CHUNK_TO_GRAIN.containsKey(stack.getItem());
    }

    public void setStoredItem(ItemStack stack) {
        // 检查是否为相同类型的物品，如果是则保留进度
        boolean isSameType = !this.storedItem.isEmpty() && 
                            !stack.isEmpty() &&
                            this.storedItem.getItem() == stack.getItem();
        
        this.storedItem = stack;
        
        // 只有放置不同类型物品时才重置计数器和拉伸因子
        if (!isSameType) {
            this.hitCount = 0; // 重置敲击计数
            this.carveCount = 0; // 重置雕刻计数
            this.stretchFactor = 0.0f; // 重置拉伸因子
            this.oreCrushCount = 0; // 重置矿石破碎计数
            this.oreCrushRequired = 0; // 重置矿石破碎所需次数
            this.knappingHitCount = 0; // 重置石器打制计数
            this.knappingRequiredHits = 0; // 重置石器打制所需次数
            this.knappingPlatformCreated = false; // 重置台面状态
            this.isCoreShaping = false; // 重置修整石核状态
            this.knappingFragility = 0; // 重置脆弱度
            this.knappingLastHitTick = 0; // 重置上次敲击时间
        }
        
        setChanged();
        // 立即通知世界更新，不区分服务端客户端
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    /**
     * 处理锻造敲击
     */
    public void handleForgingHit(ServerPlayer player, ItemStack hammer, float offsetX, float offsetZ, boolean sneaking) {
        if (level == null || level.isClientSide) return;
        if (storedItem.isEmpty()) return;
        
        // 检查是否为矿碎块，进行矿石破碎
        if (isRawOreChunk(storedItem)) {
            handleOreCrushing(player, hammer, offsetX, offsetZ);
            return;
        }

        // 检查是否为地表圆石或燧石砾石，进行石器打制（打台面）
        if (storedItem.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get()) ||
            storedItem.is(ModItems.FLINT_PEBBLE.get())) {
            handleKnappingHit(player, hammer, offsetX, offsetZ);
            return;
        }

        // 石核的两条路线：
        // Shift+右键 → 路线B：修整石核（大工具）
        // 普通右键 → 路线A：剥片（燧石片 → 小工具）
        if (storedItem.is(ModItems.STONE_CORE.get())) {
            if (sneaking) {
                handleCoreShapingHit(player, hammer, offsetX, offsetZ);
            } else {
                handleKnappingHit(player, hammer, offsetX, offsetZ);
            }
            return;
        }

        // 检查是否为燧石片，进行修整
        if (storedItem.is(ModItems.FLINT_FLAKE.get())) {
            handleKnappingHit(player, hammer, offsetX, offsetZ);
            return;
        }
        
        // 检查是否为可锻打的物品（金属胚料、软化胚料、金属弯片或金属槽片）
        boolean canForge = storedItem.is(ModItems.COPPER_BILLET.get()) ||
                          storedItem.is(ModItems.SILVER_BILLET.get()) ||
                          storedItem.is(ModItems.GOLD_BILLET.get()) ||
                          storedItem.is(ModItems.SOFT_COPPER_BILLET.get()) ||
                          storedItem.is(ModItems.COPPER_CURVE.get()) ||
                          storedItem.is(ModItems.SILVER_CURVE.get()) ||
                          storedItem.is(ModItems.GOLD_CURVE.get()) ||
                          storedItem.is(ModItems.COPPER_SLOT.get()) ||
                          storedItem.is(ModItems.SILVER_SLOT.get()) ||
                          storedItem.is(ModItems.GOLD_SLOT.get());
        
        if (!canForge) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.cannot_forging_this_item"),
                true
            );
            return;
        }
        
        // 检查金属坯料的重量，只有≥50g的才能被锻造
        if (storedItem.is(ModItems.COPPER_BILLET.get()) ||
            storedItem.is(ModItems.SILVER_BILLET.get()) ||
            storedItem.is(ModItems.GOLD_BILLET.get())) {
            net.minecraft.nbt.CompoundTag tag = storedItem.getTag();
            if (tag != null && tag.contains("Weight")) {
                double weight = tag.getDouble("Weight");
                if (weight < 50.0) {
                    player.displayClientMessage(
                        Component.translatable("message.forgeborneodyssey.anvil.weight_too_low"),
                        true
                    );
                    return;
                }
            }
        }
        
        // 根据锤子类型消耗不同的饱食度
        float exhaustionAmount;
        if (hammer.is(ModItems.HANDLE_STONE_HAMMER.get())) {
            exhaustionAmount = 0.2f;
        } else {
            exhaustionAmount = 0.4f;
        }
        player.causeFoodExhaustion(exhaustionAmount);
        
        // 每次敲击增加计数（受负重影响，负重过重时可能白敲）
        this.hitCount++;
        float forgeEfficiency = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getForgingEfficiencyMultiplier(player);
        boolean effectiveHit = level.random.nextFloat() < forgeEfficiency;
        if (!effectiveHit) {
            this.hitCount--;
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.ineffective_hit"),
                true
            );
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return;
        }
        
        // 每次敲击增加拉伸因子，最多累积到1.5
        this.stretchFactor = Math.min(this.stretchFactor + 0.15f, 1.5f);
        
        // 播放敲击音效和粒子效果
        com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.NEAR.with(
                net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    32.0, level.dimension()
                )
            ),
            new com.lwx.forgeborneodyssey.network.ForgingSparkPacket(worldPosition, offsetX, offsetZ)
        );
        
        // 检查是否为金属弯片的锻造（6次转换为金属斧头）
        boolean isCurveForging = storedItem.is(ModItems.COPPER_CURVE.get()) ||
                                storedItem.is(ModItems.SILVER_CURVE.get()) ||
                                storedItem.is(ModItems.GOLD_CURVE.get());
        
        // 检查是否为金属槽片的锻造（7次转换为金属刀刃）
        boolean isSlotForging = storedItem.is(ModItems.COPPER_SLOT.get()) ||
                               storedItem.is(ModItems.SILVER_SLOT.get()) ||
                               storedItem.is(ModItems.GOLD_SLOT.get());
        
        if (isCurveForging && this.hitCount >= 6) {
            // 转换为对应的金属斧头
            convertToMetalAxe(player);
        } else if (isSlotForging && this.hitCount >= 7) {
            // 转换为对应的金属刀刃
            convertToMetalBlade(player);
        } else if (!isCurveForging && !isSlotForging && this.hitCount >= 8) {
            // 胚料转换为金属片（8次）- 有1%的几率锻造失败
            if (level.random.nextInt(100) < 1) {
                // 锻造失败，掉落金属碎片
                handleForgingFailure(player);
            } else {
                // 锻造成功
                convertToMetalSheet(player);
            }
        } else {
            setChanged();
            // 同步数据到客户端
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            // 显示进度提示
            int maxHits;
            if (isCurveForging) {
                maxHits = 6;
            } else if (isSlotForging) {
                maxHits = 7;
            } else {
                maxHits = 8;
            }
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.forging_progress", this.hitCount, maxHits),
                true
            );
        }
    }
    
    /**
     * 处理矿石破碎敲击
     * 模拟夏朝人用石锤在石砧上手工破碎矿石的完整流程：
     * - 品质越高的矿石越致密，需要更多锤击才能碎裂
     * - 破碎过程中有粉尘损耗（约10%概率损失物料）
     * - 手工劳作消耗大量饱食度
     * - 最终分离出矿物颗粒（有用）和掺和料（废石）
     */
    private void handleOreCrushing(ServerPlayer player, ItemStack hammer, float offsetX, float offsetZ) {
        if (level == null || level.isClientSide) return;
        if (storedItem.isEmpty()) return;
        
        // 首次敲击时根据矿石品质确定所需总次数
        if (oreCrushRequired <= 0) {
            CompoundTag tag = storedItem.getTag();
            float quality = 0.5f;
            if (tag != null && tag.contains("ore_quality")) {
                quality = tag.getFloat("ore_quality");
            }
            OreQuality oreQuality = OreQuality.fromValue(quality);
            oreCrushRequired = switch (oreQuality) {
                case FRACTURED -> 2;                                  // 已碎裂，一敲即碎
                case ROUGH     -> 2 + level.random.nextInt(2);        // 2-3
                case INTACT    -> 3;                                   // 完整，需3锤
                case DENSE     -> 3 + level.random.nextInt(2);        // 3-4，致密难碎
                case PERFECT   -> 3 + level.random.nextInt(2);        // 3-4，极难碎裂
                default        -> 3;
            };
        }
        
        // 手工碎石消耗饱食度（夏朝纯人力劳作，消耗较大）
        float exhaustionAmount;
        if (hammer.is(ModItems.HANDLE_STONE_HAMMER.get())) {
            exhaustionAmount = 0.25f;
        } else if (hammer.is(ModItems.COBBLESTONE_HAMMER.get())) {
            exhaustionAmount = 0.35f;
        } else {
            exhaustionAmount = 0.30f;
        }
        player.causeFoodExhaustion(exhaustionAmount);
        
        // 受负重影响，手臂太累时可能无效敲击
        this.oreCrushCount++;
        float forgeEfficiency = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getForgingEfficiencyMultiplier(player);
        boolean effectiveHit = level.random.nextFloat() < forgeEfficiency;
        if (!effectiveHit) {
            this.oreCrushCount--;
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.ineffective_hit"),
                true
            );
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return;
        }
        
        // 播放石锤敲击音效，音调随进度升高（模拟矿石逐渐碎裂）
        float pitch = 0.85f + 0.05f * oreCrushCount + level.random.nextFloat() * 0.1f;
        level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.STONE_HIT,
            net.minecraft.sounds.SoundSource.BLOCKS, 0.9f, pitch);
        
        // 矿碎块自身贴图的碎裂粒子，粒子数量随进度递增
        int particleCount = 5 + oreCrushCount * 3;
        ((ServerLevel) level).sendParticles(
            new ItemParticleOption(ParticleTypes.ITEM, storedItem),
            worldPosition.getX() + 0.5D + offsetX * 0.5D,
            worldPosition.getY() + 1.1D,
            worldPosition.getZ() + 0.5D + offsetZ * 0.5D,
            particleCount,
            0.15D, 0.15D, 0.15D,
            0.05D
        );
        
        // 检查是否达到所需次数
        if (this.oreCrushCount >= this.oreCrushRequired) {
            crushOreChunk(player);
        } else {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.ore_crushing_progress", 
                    this.oreCrushCount, this.oreCrushRequired),
                true
            );
        }
    }
    
    /**
     * 矿石破碎完成：碎裂成掺和料和对应矿物颗粒
     * 
     * 夏朝手工碎石产出模型：
     * - 品质决定矿物含量，品质越高颗粒越多、废石越少
     * - 纯度影响颗粒的品质标签，但不影响数量
     * - 手工破碎有粉尘损耗（约10%概率损失一个颗粒或掺和料）
     * - 总体产废率远高于有用矿物产出率，符合原始工艺特征
     */
    private void crushOreChunk(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;
        
        CompoundTag tag = storedItem.getTag();
        float quality = 0.5f;
        float purity = 0.5f;
        if (tag != null) {
            if (tag.contains("ore_quality")) {
                quality = tag.getFloat("ore_quality");
            }
            if (tag.contains("ore_purity")) {
                purity = tag.getFloat("ore_purity");
            }
        }
        
        Item grainItem = ORE_CHUNK_TO_GRAIN.get(storedItem.getItem());
        if (grainItem == null) return;
        
        // 使用 OreQuality 枚举的随机倍率（含品质等级内的随机波动）
        OreQuality oreQuality = OreQuality.fromValue(quality);
        float qualityMultiplier = oreQuality.getRandomMultiplier(level.random);
        // 品质倍率范围：碎裂 0.70~0.85 / 粗糙 0.85~0.95 / 完好 0.95~1.10 / 致密 1.10~1.25 / 完美 1.25~1.50
        
        // 颗粒数量：品质倍率 × 基础随机(1~2)，至少1个
        int grainCount = Math.max(1, Math.round(qualityMultiplier * (1 + level.random.nextInt(2))));
        
        // 掺和料数量：夏朝手工碎石废石率极高
        // 废石倍率 = 2.5 - 品质倍率，范围 1.00~1.80
        // 基础废石 2~4，乘以废石倍率后范围约 2~7
        float wasteMultiplier = 2.5f - qualityMultiplier;
        int temperCount = Math.max(1, Math.round(wasteMultiplier * (2 + level.random.nextInt(3))));
        
        // 粉尘损耗：约10%概率损失一个产出物（优先损失掺和料）
        if (level.random.nextFloat() < 0.10f) {
            if (temperCount > 1) {
                temperCount--;
            } else if (grainCount > 1) {
                grainCount--;
            }
        }
        
        // 播放碎裂音效，音调随品质升高（品质越高碎裂声越清脆）
        float breakPitch = 0.75f + quality * 0.5f + level.random.nextFloat() * 0.15f;
        level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.STONE_BREAK,
            net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, breakPitch);
        
        // 生成矿物颗粒——继承原矿的纯度和品质
        for (int i = 0; i < grainCount; i++) {
            ItemStack grainStack = new ItemStack(grainItem);
            CompoundTag grainTag = grainStack.getOrCreateTag();
            grainTag.putFloat("ore_purity", purity);
            grainTag.putFloat("ore_quality", quality);
            
            net.minecraft.world.entity.item.ItemEntity grainEntity = 
                new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5) * 0.7,
                    worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5) * 0.7,
                    grainStack
                );
            grainEntity.setDefaultPickUpDelay();
            level.addFreshEntity(grainEntity);
        }
        
        // 生成掺和料（废石）——继承原矿品质，无纯度
        for (int i = 0; i < temperCount; i++) {
            ItemStack temperStack = new ItemStack(ModItems.TEMPER_GROG.get());
            CompoundTag temperTag = temperStack.getOrCreateTag();
            temperTag.putFloat("ore_quality", quality);
            
            net.minecraft.world.entity.item.ItemEntity temperEntity = 
                new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5) * 0.7,
                    worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5) * 0.7,
                    temperStack
                );
            temperEntity.setDefaultPickUpDelay();
            level.addFreshEntity(temperEntity);
        }
        
        // 清空石砧
        this.storedItem = ItemStack.EMPTY;
        this.oreCrushCount = 0;
        this.oreCrushRequired = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        
        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.anvil.ore_crushing_complete", 
                grainCount, temperCount),
            true
        );
    }

    /**
     * 处理打制石器敲击 —— 三阶段流程
     * 
     * 阶段一（打台面）：燧石砾石 → 第一击敲出平台 → 制成石核
     *   第一击必定成功，砾石变为石核物品
     * 
     * 阶段二（剥片）：石核 → 斜敲台面边缘 → 每次60%概率剥落燧石片
     *   40%概率打废（产出碎石废料）
     *   连续急敲（间隔<0.5秒）累积脆弱度，100%则石核碎裂
     * 
     * 阶段三（修整）：燧石片 → 精细敲击 → 随机石器头
     *   修整为精细活，无急敲惩罚
     */
    private void handleKnappingHit(ServerPlayer player, ItemStack hammer, float offsetX, float offsetZ) {
        if (level == null || level.isClientSide) return;
        if (storedItem.isEmpty()) return;

        boolean isPebble = storedItem.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get()) ||
                           storedItem.is(ModItems.FLINT_PEBBLE.get());
        boolean isCore = storedItem.is(ModItems.STONE_CORE.get());
        boolean isFlake = storedItem.is(ModItems.FLINT_FLAKE.get());
        if (!isPebble && !isCore && !isFlake) return;

        long currentTick = level.getGameTime();

        // 消耗饱食度
        float exhaustionAmount;
        if (hammer.is(ModItems.HANDLE_STONE_HAMMER.get())) {
            exhaustionAmount = 0.15f;
        } else {
            exhaustionAmount = 0.25f;
        }
        player.causeFoodExhaustion(exhaustionAmount);

        // === 急敲检测（打台面+剥片阶段） ===
        if ((isPebble || isCore) && knappingLastHitTick > 0 && (currentTick - knappingLastHitTick) < RAPID_HIT_THRESHOLD) {
            this.knappingFragility += 30;
            this.knappingLastHitTick = currentTick;
            hammer.hurtAndBreak(2, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.knapping_rapid_hit",
                    this.knappingFragility),
                true
            );
            playKnappingFeedback(offsetX, offsetZ, false);

            if (this.knappingFragility >= 100) {
                shatterPebble(player);
                return;
            }

            // 脆弱度警告：首次达到60时提示玩家放慢节奏
            if (this.knappingFragility == 60) {
                player.displayClientMessage(
                    Component.translatable("message.forgeborneodyssey.anvil.knapping_fragility_warning",
                        this.knappingFragility),
                    true
                );
            }

            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        this.knappingLastHitTick = currentTick;

        // === 阶段一：打台面 ===
        if (isPebble && !knappingPlatformCreated) {
            createPlatform(player, hammer, offsetX, offsetZ);
            return;
        }

        // === 阶段二：剥片 ===
        if (isCore) {
            detachFlake(player, hammer, offsetX, offsetZ);
            return;
        }

        // === 阶段三：修整 ===
        if (isFlake) {
            retouchFlake(player, hammer, offsetX, offsetZ);
            return;
        }
    }

    /**
     * 路线B：修整石核（Core Shaping）
     * Shift+右键石核，直接修整石核本体为大型工具头（斧/铲/锄）
     * 3-5次敲击，无急敲惩罚（大工具需要更稳的手法）
     */
    private void handleCoreShapingHit(ServerPlayer player, ItemStack hammer, float offsetX, float offsetZ) {
        if (level == null || level.isClientSide) return;

        // 首次敲击时初始化
        if (!isCoreShaping) {
            this.isCoreShaping = true;
            int strengthLevel = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getStrengthLevel(player);
            int baseHits = 3 + level.random.nextInt(3); // 3-5次
            int reduction = Math.min(2, strengthLevel / 5);
            this.knappingRequiredHits = baseHits - reduction;
            this.knappingHitCount = 0;
        }

        hammer.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));

        // 力量效率判定
        float efficiency = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getForgingEfficiencyMultiplier(player);
        boolean effectiveHit = level.random.nextFloat() < efficiency;

        if (!effectiveHit) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.ineffective_hit"),
                true
            );
            playKnappingFeedback(offsetX, offsetZ, false);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        this.knappingHitCount++;
        playKnappingFeedback(offsetX, offsetZ, true);

        if (this.knappingHitCount >= this.knappingRequiredHits) {
            completeCoreShaping(player);
        } else {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            player.displayClientMessage(
                Component.translatable(getCoreShapingMessage(knappingHitCount, knappingRequiredHits)),
                true
            );
        }
    }

    /**
     * 修整石核完成：产出大型工具头
     */
    private void completeCoreShaping(ServerPlayer player) {
        if (level == null) return;

        // 加权随机选择大型工具
        int totalWeight = CORE_SHAPING_OUTPUTS.stream().mapToInt(CoreShapingOutput::weight).sum();
        int roll = level.random.nextInt(totalWeight);
        int cumulative = 0;
        ItemStack result = ItemStack.EMPTY;
        for (CoreShapingOutput output : CORE_SHAPING_OUTPUTS) {
            cumulative += output.weight();
            if (roll < cumulative) {
                result = output.supplier().get();
                break;
            }
        }
        if (result.isEmpty()) {
            result = new ItemStack(ModItems.STONE_AXE_HEAD.get());
        }

        net.minecraft.world.entity.item.ItemEntity itemEntity =
            new net.minecraft.world.entity.item.ItemEntity(
                level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.0D,
                worldPosition.getZ() + 0.5D,
                result
            );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);

        level.playSound(null, worldPosition,
            net.minecraft.sounds.SoundEvents.STONE_BREAK,
            net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.2f);

        this.storedItem = ItemStack.EMPTY;
        this.knappingHitCount = 0;
        this.knappingRequiredHits = 0;
        this.knappingPlatformCreated = false;
        this.isCoreShaping = false;
        this.knappingFragility = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.anvil.core_shaping_complete",
                result.getHoverName().getString()),
            true
        );
    }

    /**
     * 修整石核进度消息
     */
    private static String getCoreShapingMessage(int hits, int required) {
        if (hits == 1) return "message.forgeborneodyssey.anvil.core_shaping_first";
        if (hits == required - 1) return "message.forgeborneodyssey.anvil.core_shaping_final";
        return "message.forgeborneodyssey.anvil.core_shaping_stage";
    }

    /**
     * 阶段一：打台面
     * 第一击敲出平整台面，圆石制成石核。必定成功，不需要力量判定。
     */
    private void createPlatform(ServerPlayer player, ItemStack hammer, float offsetX, float offsetZ) {
        if (level == null) return;

        // 砾石变为石核
        this.storedItem = new ItemStack(ModItems.STONE_CORE.get());
        this.knappingPlatformCreated = true;

        int strengthLevel = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getStrengthLevel(player);
        int baseHits = 3 + level.random.nextInt(3); // 3-5次剥片机会
        int reduction = Math.min(2, strengthLevel / 5);
        this.knappingRequiredHits = baseHits - reduction;

        hammer.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
        playKnappingFeedback(offsetX, offsetZ, true);

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.anvil.platform_created"),
            true
        );
    }

    /**
     * 阶段二：剥片
     * 斜敲石核台面边缘，概率性剥落燧石片。真实情况：十次敲击有几次直接打废。
     */
    private void detachFlake(ServerPlayer player, ItemStack hammer, float offsetX, float offsetZ) {
        if (level == null) return;

        hammer.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));

        // 力量效率判定
        float efficiency = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getForgingEfficiencyMultiplier(player);
        boolean effectiveHit = level.random.nextFloat() < efficiency;

        if (!effectiveHit) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.ineffective_hit"),
                true
            );
            playKnappingFeedback(offsetX, offsetZ, false);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        this.knappingHitCount++;

        // 60%概率剥落燧石片，40%概率打废（产出碎石废料）
        boolean gotFlake = level.random.nextFloat() < 0.60f;
        if (gotFlake) {
            spawnFlintFlakes(1);
            playKnappingFeedback(offsetX, offsetZ, true);
        } else {
            spawnDebitage(1);
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.flaking_waste"),
                true
            );
            playKnappingFeedback(offsetX, offsetZ, false);
        }

        // 正常节奏敲击降低脆弱度
        if (knappingFragility > 0) {
            knappingFragility = Math.max(0, knappingFragility - 10);
        }

        if (this.knappingHitCount >= this.knappingRequiredHits) {
            completeFlaking(player);
        } else {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            player.displayClientMessage(
                Component.translatable(getKnappingMessage(knappingHitCount, knappingRequiredHits, true)),
                true
            );
        }
    }

    /**
     * 阶段三：修整
     * 精细敲击燧石片边缘，修整为石器头。无急敲惩罚。
     */
    private void retouchFlake(ServerPlayer player, ItemStack hammer, float offsetX, float offsetZ) {
        if (level == null) return;

        // 首次修整时初始化
        if (knappingRequiredHits <= 0) {
            int strengthLevel = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getStrengthLevel(player);
            int baseHits = 3;
            int reduction = Math.min(1, strengthLevel / 5);
            knappingRequiredHits = baseHits - reduction;
        }

        hammer.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));

        // 力量效率判定
        float efficiency = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getForgingEfficiencyMultiplier(player);
        boolean effectiveHit = level.random.nextFloat() < efficiency;

        if (!effectiveHit) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.ineffective_hit"),
                true
            );
            playKnappingFeedback(offsetX, offsetZ, false);
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return;
        }

        this.knappingHitCount++;
        playKnappingFeedback(offsetX, offsetZ, true);

        if (this.knappingHitCount >= this.knappingRequiredHits) {
            completeRetouch(player);
        } else {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            player.displayClientMessage(
                Component.translatable(getKnappingMessage(knappingHitCount, knappingRequiredHits, false)),
                true
            );
        }
    }

    /**
     * 在石砧周围生成燧石片掉落物
     */
    private void spawnFlintFlakes(int count) {
        if (level == null) return;
        for (int i = 0; i < count; i++) {
            ItemStack flake = new ItemStack(ModItems.FLINT_FLAKE.get());
            net.minecraft.world.entity.item.ItemEntity flakeEntity =
                new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5) * 0.6,
                    worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5) * 0.6,
                    flake
                );
            flakeEntity.setDefaultPickUpDelay();
            flakeEntity.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.2,
                0.15 + level.random.nextDouble() * 0.15,
                (level.random.nextDouble() - 0.5) * 0.2
            );
            level.addFreshEntity(flakeEntity);
        }
    }

    /**
     * 在石砧周围生成碎石废料掉落物
     */
    private void spawnDebitage(int count) {
        if (level == null) return;
        for (int i = 0; i < count; i++) {
            ItemStack debris = new ItemStack(ModItems.STONE_DEBITAGE.get());
            net.minecraft.world.entity.item.ItemEntity debrisEntity =
                new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5) * 0.6,
                    worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5) * 0.6,
                    debris
                );
            debrisEntity.setDefaultPickUpDelay();
            debrisEntity.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.15,
                0.1 + level.random.nextDouble() * 0.1,
                (level.random.nextDouble() - 0.5) * 0.15
            );
            level.addFreshEntity(debrisEntity);
        }
    }

    /**
     * 剥片阶段完成：石核耗尽，消耗完毕
     */
    private void completeFlaking(ServerPlayer player) {
        if (level == null) return;

        // 最后剥落 2 个燧石片
        spawnFlintFlakes(2);

        level.playSound(null, worldPosition,
            net.minecraft.sounds.SoundEvents.STONE_BREAK,
            net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.8f);

        this.storedItem = ItemStack.EMPTY;
        this.knappingHitCount = 0;
        this.knappingRequiredHits = 0;
        this.knappingPlatformCreated = false;
        this.isCoreShaping = false;
        this.knappingFragility = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.anvil.core_exhausted"),
            true
        );
    }

    /**
     * 修整阶段完成：燧石片加工为随机石器头
     */
    private void completeRetouch(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;

        // 加权随机选择产出
        int totalWeight = KNAPPING_OUTPUTS.stream().mapToInt(KnappingOutput::weight).sum();
        int roll = level.random.nextInt(totalWeight);
        int cumulative = 0;
        ItemStack result = ItemStack.EMPTY;
        for (KnappingOutput output : KNAPPING_OUTPUTS) {
            cumulative += output.weight();
            if (roll < cumulative) {
                result = output.supplier().get();
                break;
            }
        }
        if (result.isEmpty()) {
            result = new ItemStack(ModItems.FLINT_KNIFE_HEAD.get());
        }

        net.minecraft.world.entity.item.ItemEntity itemEntity =
            new net.minecraft.world.entity.item.ItemEntity(
                level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.0D,
                worldPosition.getZ() + 0.5D,
                result
            );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);

        level.playSound(null, worldPosition,
            net.minecraft.sounds.SoundEvents.STONE_BREAK,
            net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.5f);

        this.storedItem = ItemStack.EMPTY;
        this.knappingHitCount = 0;
        this.knappingRequiredHits = 0;
        this.knappingPlatformCreated = false;
        this.isCoreShaping = false;
        this.knappingFragility = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.anvil.knapping_complete",
                result.getHoverName().getString()),
            true
        );
    }

    /**
     * 圆石碎裂：失去进度，散落少量燧石碎片
     */
    private void shatterPebble(ServerPlayer player) {
        if (level == null) return;

        // 碎裂时产出少量燧石片和大量碎石废料
        int flakeCount = 1 + level.random.nextInt(2);
        for (int i = 0; i < flakeCount; i++) {
            ItemStack flake = new ItemStack(ModItems.FLINT_FLAKE.get());
            net.minecraft.world.entity.item.ItemEntity flakeEntity =
                new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5) * 0.8,
                    worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5) * 0.8,
                    flake
                );
            flakeEntity.setDefaultPickUpDelay();
            flakeEntity.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.3,
                0.2 + level.random.nextDouble() * 0.2,
                (level.random.nextDouble() - 0.5) * 0.3
            );
            level.addFreshEntity(flakeEntity);
        }

        // 额外的碎石废料
        int debrisCount = 2 + level.random.nextInt(3);
        for (int i = 0; i < debrisCount; i++) {
            ItemStack debris = new ItemStack(ModItems.STONE_DEBITAGE.get());
            net.minecraft.world.entity.item.ItemEntity debrisEntity =
                new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5) * 0.8,
                    worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5) * 0.8,
                    debris
                );
            debrisEntity.setDefaultPickUpDelay();
            debrisEntity.setDeltaMovement(
                (level.random.nextDouble() - 0.5) * 0.2,
                0.1 + level.random.nextDouble() * 0.15,
                (level.random.nextDouble() - 0.5) * 0.2
            );
            level.addFreshEntity(debrisEntity);
        }

        level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.STONE_BREAK,
            net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.5f);

        this.storedItem = ItemStack.EMPTY;
        this.knappingHitCount = 0;
        this.knappingRequiredHits = 0;
        this.knappingPlatformCreated = false;
        this.isCoreShaping = false;
        this.knappingFragility = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.anvil.knapping_shatter"),
            true
        );
    }

    /**
     * 根据敲击进度获取剥片/修整阶段的描述性消息
     * @param isFlaking true=剥片阶段，false=修整阶段
     */
    private static String getKnappingMessage(int hits, int required, boolean isFlaking) {
        if (isFlaking) {
            if (hits == 1) return "message.forgeborneodyssey.anvil.flaking_first";
            if (hits == required - 1) return "message.forgeborneodyssey.anvil.flaking_final";
            return "message.forgeborneodyssey.anvil.flaking_stage1";
        } else {
            if (hits == 1) return "message.forgeborneodyssey.anvil.retouch_first_hit";
            if (hits == required - 1) return "message.forgeborneodyssey.anvil.retouch_final";
            return "message.forgeborneodyssey.anvil.retouch_stage1";
        }
    }

    /**
     * 播放石器打制的音效和粒子效果
     * 模拟石锤敲击石核，石片飞溅，音效随进度升高
     */
    private void playKnappingFeedback(float offsetX, float offsetZ, boolean effective) {
        if (level == null) return;

        if (effective) {
            // 有效敲击：清脆的石击声，音调随进度升高
            float pitch = 0.85f + knappingHitCount * 0.06f + level.random.nextFloat() * 0.08f;
            level.playSound(null, worldPosition,
                net.minecraft.sounds.SoundEvents.STONE_HIT,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.7f, pitch);

            // 石片飞溅粒子——向敲击方向两端散开
            int particleCount = 5 + level.random.nextInt(3);
            double spreadX = 0.35D;
            double spreadZ = 0.35D;
            ((ServerLevel) level).sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, storedItem.copy()),
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.15D,
                worldPosition.getZ() + 0.5D,
                particleCount,
                spreadX * (level.random.nextDouble() - 0.5),
                0.05D,
                spreadZ * (level.random.nextDouble() - 0.5),
                0.15D
            );
        } else {
            // 无效敲击：沉闷的敲击声
            level.playSound(null, worldPosition,
                net.minecraft.sounds.SoundEvents.STONE_HIT,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 0.55f);

            // 少量粉尘
            ((ServerLevel) level).sendParticles(
                ParticleTypes.EXPLOSION,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.1D,
                worldPosition.getZ() + 0.5D,
                1, 0.05D, 0.05D, 0.05D, 0.02D
            );
        }
    }

    /**
     * 处理用斧头弯曲金属片或制作槽片
     */
    public void handleAxeBend(ServerPlayer player, ItemStack axe) {
        if (level == null || level.isClientSide) return;
        if (storedItem.isEmpty()) return;
        
        ItemStack resultItem = ItemStack.EMPTY;
        boolean isSlotOperation = false;
        boolean isRingOperation = false;
        boolean isHookOperation = false;
        boolean isPinOperation = false;
        
        // 根据金属片类型转换为对应的弯片
        if (storedItem.is(ModItems.COPPER_SHEET.get())) {
            resultItem = new ItemStack(ModItems.COPPER_CURVE.get());
        } else if (storedItem.is(ModItems.SILVER_SHEET.get())) {
            resultItem = new ItemStack(ModItems.SILVER_CURVE.get());
        } else if (storedItem.is(ModItems.GOLD_SHEET.get())) {
            resultItem = new ItemStack(ModItems.GOLD_CURVE.get());
        }
        // 根据金属弯片类型转换为对应的槽片
        else if (storedItem.is(ModItems.COPPER_CURVE.get())) {
            resultItem = new ItemStack(ModItems.COPPER_SLOT.get());
            isSlotOperation = true;
        } else if (storedItem.is(ModItems.SILVER_CURVE.get())) {
            resultItem = new ItemStack(ModItems.SILVER_SLOT.get());
            isSlotOperation = true;
        } else if (storedItem.is(ModItems.GOLD_CURVE.get())) {
            resultItem = new ItemStack(ModItems.GOLD_SLOT.get());
            isSlotOperation = true;
        }
        // 根据铜槽片类型转换为铜环
        else if (storedItem.is(ModItems.COPPER_SLOT.get())) {
            resultItem = new ItemStack(ModItems.COPPER_RING.get());
            isRingOperation = true;
        }
        // 根据铜环类型转换为铜钩
        else if (storedItem.is(ModItems.COPPER_RING.get())) {
            resultItem = new ItemStack(ModItems.COPPER_HOOK.get());
            isHookOperation = true;
        }
        // 根据铜钩类型转换为铜针
        else if (storedItem.is(ModItems.COPPER_HOOK.get())) {
            resultItem = new ItemStack(ModItems.COPPER_PIN.get());
            isPinOperation = true;
        }
        // 根据银槽片类型转换为银针
        else if (storedItem.is(ModItems.SILVER_SLOT.get())) {
            resultItem = new ItemStack(ModItems.SILVER_PIN.get());
            isPinOperation = true;
        }
        // 根据金槽片类型转换为金针
        else if (storedItem.is(ModItems.GOLD_SLOT.get())) {
            resultItem = new ItemStack(ModItems.GOLD_PIN.get());
            isPinOperation = true;
        }
        
        if (!resultItem.isEmpty()) {
            // 继承原物品的质量和纯度属性
            inheritQualityAndPurity(storedItem, resultItem);
            
            // 替换石砧上的物品
            this.storedItem = resultItem;
            this.hitCount = 0;
            this.carveCount = 0;
            this.stretchFactor = 0.0f;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // 播放音效（槽片、环、钩和针使用不同音调）
            float pitch;
            if (isPinOperation) {
                pitch = 2.2f;
            } else if (isHookOperation) {
                pitch = 2.0f;
            } else if (isRingOperation) {
                pitch = 1.8f;
            } else if (isSlotOperation) {
                pitch = 1.5f;
            } else {
                pitch = 1.2f;
            }
            level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.ANVIL_PLACE,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, pitch);
            
            // 发送粒子效果数据包
            com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.NEAR.with(
                    net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                        worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        32.0, level.dimension()
                    )
                ),
                new com.lwx.forgeborneodyssey.network.ForgingSparkPacket(worldPosition, 0.0f, 0.0f)
            );
            
            // 显示完成消息
            String messageKey;
            if (isPinOperation) {
                messageKey = "message.forgeborneodyssey.anvil.hook_or_slot_made_to_pin";
            } else if (isHookOperation) {
                messageKey = "message.forgeborneodyssey.anvil.ring_made_to_hook";
            } else if (isRingOperation) {
                messageKey = "message.forgeborneodyssey.anvil.slot_made_to_ring";
            } else if (isSlotOperation) {
                messageKey = "message.forgeborneodyssey.anvil.curve_made_to_slot";
            } else {
                messageKey = "message.forgeborneodyssey.anvil.sheet_bent_to_curve";
            }
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(messageKey),
                true
            );
        } else {
            // 不是可处理的物品
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.cannot_process_this_item"),
                true
            );
        }
    }
    
    /**
     * 处理用凿子雕刻金属针为饰针或金属槽片为金属刀
     */
    public void handleChiselCarve(ServerPlayer player, ItemStack chisel) {
        if (level == null || level.isClientSide) return;
        if (storedItem.isEmpty()) return;
        
        // 检查是否为金属针
        boolean isMetalPin = storedItem.is(ModItems.COPPER_PIN.get()) ||
                            storedItem.is(ModItems.SILVER_PIN.get()) ||
                            storedItem.is(ModItems.GOLD_PIN.get());
        
        // 检查是否为金属槽片
        boolean isMetalSlot = storedItem.is(ModItems.COPPER_SLOT.get()) ||
                             storedItem.is(ModItems.SILVER_SLOT.get()) ||
                             storedItem.is(ModItems.GOLD_SLOT.get());
        
        if (!isMetalPin && !isMetalSlot) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.cannot_carve_this_item"),
                true
            );
            return;
        }
        
        // 每次雕刻增加计数（受负重影响）
        this.carveCount++;
        float forgeEfficiency = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getForgingEfficiencyMultiplier(player);
        boolean effectiveHit = level.random.nextFloat() < forgeEfficiency;
        if (!effectiveHit) {
            this.carveCount--;
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.ineffective_hit"),
                true
            );
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return;
        }
        
        // 播放雕刻音效和粒子效果
        com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.NEAR.with(
                net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    32.0, level.dimension()
                )
            ),
            new com.lwx.forgeborneodyssey.network.ForgingSparkPacket(worldPosition, 0.0f, 0.0f)
        );
        
        // 金属针需要10次雕刻转换为饰针胸甲，金属槽片需要6次雕刻转换为金属刀
        int requiredCount = isMetalPin ? 10 : 6;
        
        if (this.carveCount >= requiredCount) {
            if (isMetalPin) {
                // 转换为对应的饰针胸甲
                convertToPinArmor(player);
            } else {
                // 转换为对应的金属刀
                convertSlotToKnife(player);
            }
        } else {
            setChanged();
            // 同步数据到客户端
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            // 显示进度提示
            String messageKey = isMetalPin ? "message.forgeborneodyssey.anvil.carving_progress" : "message.forgeborneodyssey.anvil.slot_to_knife_progress";
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(messageKey, this.carveCount, requiredCount),
                true
            );
        }
    }
    
    /**
     * 将金属针转换为对应的饰针胸甲
     */
    private void convertToPinArmor(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;
        
        ItemStack pinArmorItem = ItemStack.EMPTY;
        
        // 根据金属针类型转换为对应的饰针胸甲
        if (storedItem.is(ModItems.COPPER_PIN.get())) {
            pinArmorItem = new ItemStack(ModItems.COPPER_PIN_CHESTPLATE.get());
        } else if (storedItem.is(ModItems.SILVER_PIN.get())) {
            pinArmorItem = new ItemStack(ModItems.SILVER_PIN_CHESTPLATE.get());
        } else if (storedItem.is(ModItems.GOLD_PIN.get())) {
            pinArmorItem = new ItemStack(ModItems.GOLD_PIN_CHESTPLATE.get());
        }
        
        if (!pinArmorItem.isEmpty()) {
            // 继承原物品的质量和纯度属性
            inheritQualityAndPurity(storedItem, pinArmorItem);
            
            // 替换石砧上的物品为饰针胸甲
            this.storedItem = pinArmorItem;
            this.hitCount = 0; // 重置敲击计数
            this.carveCount = 0; // 重置雕刻计数
            this.stretchFactor = 0.0f; // 重置拉伸因子
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // 播放完成音效
            level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.ANVIL_PLACE,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 2.5f);
            
            // 发送粒子效果数据包
            com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.NEAR.with(
                    net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                        worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        32.0, level.dimension()
                    )
                ),
                new com.lwx.forgeborneodyssey.network.ForgingSparkPacket(worldPosition, 0.0f, 0.0f)
            );
            
            // 显示完成消息
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.pin_to_armor_complete"),
                true
            );
        }
    }
    
    /**
     * 获取当前拉伸因子（0-1之间）
     */
    public float getStretchFactor() {
        return stretchFactor;
    }
    
    /**
     * 获取当前锻打次数
     */
    public int getHitCount() {
        return hitCount;
    }

    /**
     * 获取打制进度比例（0.0 ~ 1.0），用于渲染
     */
    public float getKnappingProgress() {
        if (knappingRequiredHits <= 0) return 0.0f;
        return Math.min(1.0f, (float) knappingHitCount / knappingRequiredHits);
    }

    public int getKnappingFragility() {
        return knappingFragility;
    }

    public boolean isKnappingPlatformCreated() {
        return knappingPlatformCreated;
    }

    public boolean isCoreShaping() {
        return isCoreShaping;
    }

    public boolean isKnappingInProgress() {
        return !storedItem.isEmpty() && (storedItem.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())
            || storedItem.is(ModItems.FLINT_PEBBLE.get())
            || storedItem.is(ModItems.STONE_CORE.get())
            || storedItem.is(ModItems.FLINT_FLAKE.get()));
    }
    
    /**
     * 将金属弯片转换为对应的金属斧头
     */
    private void convertToMetalAxe(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;
        
        ItemStack axeItem = ItemStack.EMPTY;
        
        // 根据弯片类型转换为对应的金属斧头
        if (storedItem.is(ModItems.COPPER_CURVE.get())) {
            axeItem = new ItemStack(ModItems.COPPER_AXE.get());
        } else if (storedItem.is(ModItems.SILVER_CURVE.get())) {
            axeItem = new ItemStack(ModItems.SILVER_AXE.get());
        } else if (storedItem.is(ModItems.GOLD_CURVE.get())) {
            axeItem = new ItemStack(ModItems.GOLD_AXE.get());
        }
        
        if (!axeItem.isEmpty()) {
            // 继承原物品的质量和纯度属性
            inheritQualityAndPurity(storedItem, axeItem);
            
            // 替换石砧上的物品为金属斧头
            this.storedItem = axeItem;
            this.hitCount = 0; // 重置敲击计数
            this.stretchFactor = 0.0f; // 重置拉伸因子
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // 显示完成消息
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.curve_to_axe_complete"),
                true
            );
        }
    }
    
    /**
     * 将金属槽片转换为对应的金属刀
     */
    private void convertSlotToKnife(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;
        
        ItemStack knifeItem = ItemStack.EMPTY;
        
        // 根据槽片类型转换为对应的金属刀
        if (storedItem.is(ModItems.COPPER_SLOT.get())) {
            knifeItem = new ItemStack(ModItems.COPPER_KNIFE.get());
        } else if (storedItem.is(ModItems.SILVER_SLOT.get())) {
            knifeItem = new ItemStack(ModItems.SILVER_KNIFE.get());
        } else if (storedItem.is(ModItems.GOLD_SLOT.get())) {
            knifeItem = new ItemStack(ModItems.GOLD_KNIFE.get());
        }
        
        if (!knifeItem.isEmpty()) {
            // 继承原物品的质量和纯度属性
            inheritQualityAndPurity(storedItem, knifeItem);
            
            // 替换石砧上的物品为金属刀
            this.storedItem = knifeItem;
            this.hitCount = 0; // 重置敲击计数
            this.carveCount = 0; // 重置雕刻计数
            this.stretchFactor = 0.0f; // 重置拉伸因子
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // 播放完成音效
            level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.ANVIL_PLACE,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 2.0f);
            
            // 发送粒子效果数据包
            com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.NEAR.with(
                    net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                        worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        32.0, level.dimension()
                    )
                ),
                new com.lwx.forgeborneodyssey.network.ForgingSparkPacket(worldPosition, 0.0f, 0.0f)
            );
            
            // 显示完成消息
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.slot_to_knife_complete"),
                true
            );
        }
    }
    
    /**
     * 将金属槽片转换为对应的金属刀刃
     */
    private void convertToMetalBlade(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;
        
        ItemStack bladeItem = ItemStack.EMPTY;
        
        // 根据槽片类型转换为对应的金属刀刃
        if (storedItem.is(ModItems.COPPER_SLOT.get())) {
            bladeItem = new ItemStack(ModItems.COPPER_SWORD_BLADE.get());
        } else if (storedItem.is(ModItems.SILVER_SLOT.get())) {
            bladeItem = new ItemStack(ModItems.SILVER_SWORD_BLADE.get());
        } else if (storedItem.is(ModItems.GOLD_SLOT.get())) {
            bladeItem = new ItemStack(ModItems.GOLD_SWORD_BLADE.get());
        }
        
        if (!bladeItem.isEmpty()) {
            // 继承原物品的质量和纯度属性
            inheritQualityAndPurity(storedItem, bladeItem);
            
            // 替换石砧上的物品为金属刀刃
            this.storedItem = bladeItem;
            this.hitCount = 0; // 重置敲击计数
            this.stretchFactor = 0.0f; // 重置拉伸因子
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // 显示完成消息
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.slot_to_blade_complete"),
                true
            );
        }
    }
    
    /**
     * 将胚料转换为对应的金属片
     */
    private void convertToMetalSheet(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;
        
        ItemStack sheetItem = ItemStack.EMPTY;
        
        // 根据胚料类型转换为对应的金属片
        if (storedItem.is(ModItems.COPPER_BILLET.get())) {
            sheetItem = new ItemStack(ModItems.COPPER_SHEET.get());
        } else if (storedItem.is(ModItems.SILVER_BILLET.get())) {
            sheetItem = new ItemStack(ModItems.SILVER_SHEET.get());
        } else if (storedItem.is(ModItems.GOLD_BILLET.get())) {
            sheetItem = new ItemStack(ModItems.GOLD_SHEET.get());
        } else if (storedItem.is(ModItems.SOFT_COPPER_BILLET.get())) {
            // 软化铜坯料也转换为红铜片
            sheetItem = new ItemStack(ModItems.COPPER_SHEET.get());
        }
        
        if (!sheetItem.isEmpty()) {
            // 继承原物品的质量和纯度属性，锻打后重量为原来的95%~98%
            inheritQualityAndPurity(storedItem, sheetItem, true);
            
            // 替换石砧上的物品为金属片
            this.storedItem = sheetItem;
            this.hitCount = 0; // 重置敲击计数
            this.stretchFactor = 0.0f; // 重置拉伸因子
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // 显示完成消息
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.sheet_complete"),
                true
            );
        }
    }
    
    /**
     * 处理锻造失败，掉落金属碎片
     */
    private void handleForgingFailure(ServerPlayer player) {
        if (level == null || storedItem.isEmpty()) return;
        
        // 获取原物品的重量
        double originalWeight = 0;
        if (storedItem.hasTag() && storedItem.getTag().contains("Weight")) {
            originalWeight = storedItem.getTag().getDouble("Weight");
        }
        
        // 计算碎片重量范围（原重量的40%~50%）
        double minFragmentWeight = originalWeight * 0.4;
        double maxFragmentWeight = originalWeight * 0.5;
        
        // 随机生成1~2个碎片
        int fragmentCount = level.random.nextInt(2) + 1;
        
        // 根据原物品类型确定碎片类型
        ItemStack fragmentItem = ItemStack.EMPTY;
        if (storedItem.is(ModItems.COPPER_BILLET.get()) || storedItem.is(ModItems.SOFT_COPPER_BILLET.get())) {
            fragmentItem = new ItemStack(ModItems.COPPER_FRAGMENT.get());
        } else if (storedItem.is(ModItems.SILVER_BILLET.get())) {
            fragmentItem = new ItemStack(ModItems.SILVER_FRAGMENT.get());
        } else if (storedItem.is(ModItems.GOLD_BILLET.get())) {
            fragmentItem = new ItemStack(ModItems.GOLD_FRAGMENT.get());
        }
        
        if (!fragmentItem.isEmpty()) {
            // 为每个碎片设置重量和质量
            for (int i = 0; i < fragmentCount; i++) {
                ItemStack fragment = fragmentItem.copy();
                
                // 随机分配重量（在40%~50%范围内平均分配）
                double fragmentWeight = (minFragmentWeight + level.random.nextDouble() * (maxFragmentWeight - minFragmentWeight)) / fragmentCount;
                
                // 设置碎片的NBT标签
                net.minecraft.nbt.CompoundTag tag = fragment.getOrCreateTag();
                tag.putDouble("Weight", fragmentWeight);
                
                // 根据重量设置重量等级
                com.lwx.forgeborneodyssey.items.fragments.AbstractMetalFragmentItem fragmentItemObj = 
                    (com.lwx.forgeborneodyssey.items.fragments.AbstractMetalFragmentItem) fragment.getItem();
                
                // 设置随机重量等级（基于重量比例）
                double weightRatio = fragmentWeight / (originalWeight / fragmentCount);
                com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem.Quality quality;
                if (weightRatio < 0.85) {
                    quality = com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem.Quality.LOW;
                } else if (weightRatio > 1.15) {
                    quality = com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem.Quality.HIGH;
                } else {
                    quality = com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem.Quality.MEDIUM;
                }
                tag.putString("Quality", quality.getName());
                
                // 继承纯度（稍微降低）
                if (storedItem.hasTag() && storedItem.getTag().contains("Purity")) {
                    float originalPurity = storedItem.getTag().getFloat("Purity");
                    float fragmentPurity = Math.max(50.0f, originalPurity - level.random.nextFloat() * 10.0f);
                    tag.putFloat("Purity", fragmentPurity);
                }
                
                // 在石砧位置生成物品实体
                net.minecraft.world.entity.item.ItemEntity itemEntity = 
                    new net.minecraft.world.entity.item.ItemEntity(
                        level,
                        worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5) * 0.5,
                        worldPosition.getY() + 1.0D,
                        worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5) * 0.5,
                        fragment
                    );
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
            
            // 清空石砧上的物品
            this.storedItem = ItemStack.EMPTY;
            this.hitCount = 0;
            this.stretchFactor = 0.0f;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            
            // 播放失败音效
            level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.STONE_BREAK,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.8f);
            
            // 显示失败消息
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.forging_failed", fragmentCount),
                true
            );
        }
    }

    /**
     * 处理环境音效（仅客户端）
     * 定期播放轻微的石砧环境音效，增强沉浸感
     */
    public void tickAmbientSounds() {
        if (level == null) return;

        // 仅在客户端播放环境音效
        if (!level.isClientSide) return;

        // 减少冷却计时器
        if (ambientSoundCooldown > 0) {
            ambientSoundCooldown--;
            return;
        }

        // 只有当石砧上有物品时才播放环境音效
        if (!storedItem.isEmpty()) {
            // 每 200-400 ticks (10-20 秒) 播放一次轻微的金属锻造声
            if (level.random.nextInt(200) == 0) {
                // 使用铁砧环境音效，更符合锻造场景
                level.playLocalSound(worldPosition, net.minecraft.sounds.SoundEvents.ANVIL_STEP,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.05f, 0.6f + level.random.nextFloat() * 0.4f, false);

                // 重置冷却时间（200-400 ticks）
                ambientSoundCooldown = 200 + level.random.nextInt(200);
            }
        }
    }

    /**
     * 石器打制的服务器端tick：处理脆弱度衰减和低频粒子提示
     */
    public void tickKnappingPhase() {
        if (level == null || level.isClientSide) return;
        if (!isKnappingInProgress()) return;

        long currentTick = level.getGameTime();

        // 剥片阶段：长时间不敲也会降低脆弱度（冷却恢复）
        // 修整石核阶段：无急敲机制，不恢复脆弱度
        boolean isFlakingStage = !isCoreShaping
                && (storedItem.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())
                || storedItem.is(ModItems.FLINT_PEBBLE.get())
                || storedItem.is(ModItems.STONE_CORE.get()));
        if (isFlakingStage
                && knappingFragility > 0
                && currentTick % 10 == 0) {
            knappingFragility = Math.max(0, knappingFragility - 1);
            setChanged();
        }

        // 低频粒子提示（每20 tick = 1秒一个粒子）
        if (currentTick % 20 != 0) return;

        double cx = worldPosition.getX() + 0.5D;
        double cy = worldPosition.getY() + 1.1D;
        double cz = worldPosition.getZ() + 0.5D;

        if (storedItem.is(ModItems.STONE_CORE.get())) {
            if (isCoreShaping) {
                // 修整石核阶段：深色星点（厚重感）
                ((ServerLevel) level).sendParticles(ParticleTypes.SCRAPE,
                    cx, cy, cz, 1, 0.1D, 0.03D, 0.1D, 0);
            } else {
                // 剥片阶段：白色星点
                ((ServerLevel) level).sendParticles(ParticleTypes.CRIT,
                    cx, cy, cz, 1, 0.15D, 0.05D, 0.15D, 0);
            }
        } else if (storedItem.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())
                || storedItem.is(ModItems.FLINT_PEBBLE.get())) {
            // 打台面阶段：不显示粒子（等待第一击）
        } else {
            // 修整阶段：黄色星点
            ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                cx, cy, cz, 1, 0.1D, 0.05D, 0.1D, 0);
        }

        if (knappingFragility >= 60 && currentTick % 40 == 0) {
            ((ServerLevel) level).sendParticles(ParticleTypes.SMOKE,
                cx, cy + 0.2D, cz, 1, 0.05D, 0.02D, 0.05D, 0.01D);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedItem.isEmpty()) {
            tag.put("Item", storedItem.save(new CompoundTag()));
        }
        tag.putInt("HitCount", hitCount);
        tag.putInt("CarveCount", carveCount);
        tag.putFloat("StretchFactor", stretchFactor);
        tag.putInt("OreCrushCount", oreCrushCount);
        tag.putInt("OreCrushRequired", oreCrushRequired);
        tag.putInt("KnappingHitCount", knappingHitCount);
        tag.putInt("KnappingRequiredHits", knappingRequiredHits);
        tag.putBoolean("KnappingPlatformCreated", knappingPlatformCreated);
        tag.putBoolean("IsCoreShaping", isCoreShaping);
        tag.putLong("KnappingLastHitTick", knappingLastHitTick);
        tag.putInt("KnappingFragility", knappingFragility);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedItem = tag.contains("Item") ? ItemStack.of(tag.getCompound("Item")) : ItemStack.EMPTY;
        hitCount = tag.getInt("HitCount");
        carveCount = tag.getInt("CarveCount");
        stretchFactor = tag.getFloat("StretchFactor");
        oreCrushCount = tag.getInt("OreCrushCount");
        oreCrushRequired = tag.getInt("OreCrushRequired");
        knappingHitCount = tag.getInt("KnappingHitCount");
        knappingRequiredHits = tag.getInt("KnappingRequiredHits");
        knappingPlatformCreated = tag.getBoolean("KnappingPlatformCreated");
        isCoreShaping = tag.getBoolean("IsCoreShaping");
        knappingLastHitTick = tag.getLong("KnappingLastHitTick");
        knappingFragility = tag.getInt("KnappingFragility");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
    
    /**
     * 从源物品继承质量、纯度和重量属性到目标物品
     * @param sourceItem 源物品
     * @param targetItem 目标物品
     */
    private void inheritQualityAndPurity(ItemStack sourceItem, ItemStack targetItem) {
        inheritQualityAndPurity(sourceItem, targetItem, false);
    }
    
    /**
     * 从源物品继承质量、纯度和重量属性到目标物品
     * @param sourceItem 源物品
     * @param targetItem 目标物品
     * @param isForging 是否为锻打操作（锻打会损失2%~5%的质量）
     */
    private void inheritQualityAndPurity(ItemStack sourceItem, ItemStack targetItem, boolean isForging) {
        if (sourceItem.isEmpty() || targetItem.isEmpty()) return;
        
        // 复制 NBT 标签以保留质量、纯度和重量
        if (sourceItem.hasTag()) {
            CompoundTag sourceTag = sourceItem.getTag();
            CompoundTag targetTag = targetItem.getOrCreateTag();
            
            // 复制质量属性
            if (sourceTag.contains("Quality")) {
                targetTag.putString("Quality", sourceTag.getString("Quality"));
            }
            
            // 复制纯度属性
            if (sourceTag.contains("Purity")) {
                targetTag.putFloat("Purity", sourceTag.getFloat("Purity"));
            }
            
            // 复制重量属性，如果是锻打则减少2%~5%
            if (sourceTag.contains("Weight")) {
                double originalWeight = sourceTag.getDouble("Weight");
                if (isForging && level != null) {
                    // 锻打后重量为原来的95%~98%
                    double weightRatio = 0.95 + level.random.nextDouble() * 0.03;
                    targetTag.putDouble("Weight", originalWeight * weightRatio);
                } else {
                    targetTag.putDouble("Weight", originalWeight);
                }
            }
        }
    }
}