package com.lwx.forgeborneodyssey.items.weapons;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.entities.ThrownSlingStone;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class SlingItem extends Item {

    public static final float BASE_VELOCITY = 2.0F;
    public static final float MAX_VELOCITY = 3.5F;

    public enum AmmoQuality {
        LIGHT(30, 1.5F, 2.5F),
        MEDIUM(40, 2.0F, 3.0F),
        HEAVY(50, 2.5F, 3.5F);

        public final int maxDrawDuration;
        public final float baseDamage;
        public final float damageMultiplier;

        AmmoQuality(int maxDrawDuration, float baseDamage, float damageMultiplier) {
            this.maxDrawDuration = maxDrawDuration;
            this.baseDamage = baseDamage;
            this.damageMultiplier = damageMultiplier;
        }
    }

    public SlingItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(100));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack slingStack = player.getItemInHand(hand);
        ItemStack ammoStack = findAmmo(player, slingStack);

        if (ammoStack.isEmpty() && !player.getAbilities().instabuild) {
            return InteractionResultHolder.fail(slingStack);
        }

        AmmoQuality quality = ammoStack.isEmpty() ? AmmoQuality.MEDIUM : getAmmoQuality(ammoStack.getItem());
        slingStack.getOrCreateTag().putString("AmmoQuality", quality.name());

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(slingStack);
    }

    @Override
    public void releaseUsing(ItemStack slingStack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int useTime = this.getUseDuration(slingStack) - timeLeft;
        AmmoQuality quality = getAmmoQualityFromNBT(slingStack);
        float charge = getPowerForTime(useTime, quality.maxDrawDuration);

        if (charge < 0.3F) {
            return;
        }

        ItemStack ammoStack = findAmmo(player, slingStack);
        boolean infiniteAmmo = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, slingStack) > 0;

        if (ammoStack.isEmpty() && !infiniteAmmo) {
            return;
        }

        if (!level.isClientSide) {
            ThrownSlingStone projectile = new ThrownSlingStone(level, player);

            float velocity = BASE_VELOCITY + (MAX_VELOCITY - BASE_VELOCITY) * charge;
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);

            float damage = quality.baseDamage + charge * quality.damageMultiplier;
            projectile.setDamage(damage);
            projectile.setAmmoItem(ammoStack.getItem());

            level.addFreshEntity(projectile);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.SLING_RELEASE.get(), SoundSource.PLAYERS, 0.8F, 0.8F + charge * 0.4F);

            if (!infiniteAmmo) {
                ammoStack.shrink(1);
                if (ammoStack.isEmpty()) {
                    player.getInventory().removeItem(ammoStack);
                }
            }

            slingStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public static float getPowerForTime(int useTime, int maxDrawDuration) {
        float fraction = (float) useTime / (float) maxDrawDuration;
        fraction = (fraction * fraction + fraction * 2.0F) / 3.0F;
        return Math.min(fraction, 1.0F);
    }

    public static ItemStack findAmmo(Player player, ItemStack slingStack) {
        ItemStack offhandStack = player.getOffhandItem();
        if (isAmmo(offhandStack)) {
            return offhandStack;
        }
        return ItemStack.EMPTY;
    }

    public static boolean isAmmo(ItemStack stack) {
        return getAmmoQuality(stack.getItem()) != null;
    }

    public static AmmoQuality getAmmoQuality(Item item) {
        if (isLightRubble(item)) return AmmoQuality.LIGHT;
        if (item == ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get()) return AmmoQuality.HEAVY;
        if (isHeavyOre(item)) return AmmoQuality.HEAVY;
        if (isRubble(item) || isRawOreChunk(item)) return AmmoQuality.MEDIUM;
        return null;
    }

    private static AmmoQuality getAmmoQualityFromNBT(ItemStack stack) {
        String name = stack.getOrCreateTag().getString("AmmoQuality");
        if (name.isEmpty()) return AmmoQuality.MEDIUM;
        try {
            return AmmoQuality.valueOf(name);
        } catch (IllegalArgumentException e) {
            return AmmoQuality.MEDIUM;
        }
    }

    private static boolean isLightRubble(Item item) {
        return item == ModItems.SHALE_RUBBLE.get()
                || item == ModItems.SANDSTONE_RUBBLE.get()
                || item == ModItems.LIMESTONE_RUBBLE.get();
    }

    private static boolean isRubble(Item item) {
        return item == ModItems.MARBLE_RUBBLE.get()
                || item == ModItems.QUARTZITE_RUBBLE.get()
                || item == ModItems.GABBRO_RUBBLE.get()
                || item == ModItems.QUARTZ_VEIN_RUBBLE.get()
                || item == ModItems.SERICITIZED_RUBBLE.get()
                || item == ModItems.CHLORITE_RUBBLE.get();
    }

    private static boolean isHeavyOre(Item item) {
        return item == ModItems.RAW_MAGNETITE.get()
                || item == ModItems.RAW_SCHEELITE.get()
                || item == ModItems.RAW_GALENA.get()
                || item == ModItems.RAW_SPHALERITE.get()
                || item == ModItems.RAW_MOLYBDENITE.get()
                || item == ModItems.RAW_CASSITERITE.get()
                || item == ModItems.RAW_CASSITERITE_SAND.get();
    }

    private static boolean isRawOreChunk(Item item) {
        return item == ModItems.RAW_CHALCOPYRITE.get()
                || item == ModItems.RAW_BORNITE.get()
                || item == ModItems.RAW_CHALCOCITE.get()
                || item == ModItems.RAW_COVELLITE.get()
                || item == ModItems.RAW_CUBANITE.get()
                || item == ModItems.RAW_MALACHITE.get()
                || item == ModItems.RAW_AZURITE.get()
                || item == ModItems.RAW_CUPRITE.get()
                || item == ModItems.RAW_TENORITE.get()
                || item == ModItems.RAW_CHALCANTHITE.get()
                || item == ModItems.RAW_BROCHANTITE.get()
                || item == ModItems.RAW_MIXED_COPPER.get()
                || item == ModItems.RAW_NATIVE_COPPER.get()
                || item == ModItems.RAW_TETRAHEDRITE.get()
                || item == ModItems.RAW_TENNANTITE.get()
                || item == ModItems.RAW_TORBERNITE.get()
                || item == ModItems.RAW_CUPROVANADITE.get()
                || item == ModItems.RAW_CHRYSOCOLLA.get();
    }
}