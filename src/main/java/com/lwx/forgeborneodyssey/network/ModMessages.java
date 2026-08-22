package com.lwx.forgeborneodyssey.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络通道注册
 */
public class ModMessages {
    
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("forgeborneodyssey", "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    private static int messageID = 0;
    
    /**
     * 注册网络消息
     */
    public static void register() {
        // 锻造敲击（客户端 -> 服务端）
        CHANNEL.messageBuilder(ForgingHitPacket.class, messageID++)
            .encoder(ForgingHitPacket::toBytes)
            .decoder(ForgingHitPacket::new)
            .consumerMainThread(ForgingHitPacket::handle)
            .add();
        
        // 更新光标位置（服务端 -> 客户端）
        CHANNEL.messageBuilder(UpdateCursorPacket.class, messageID++)
            .encoder(UpdateCursorPacket::toBytes)
            .decoder(UpdateCursorPacket::new)
            .consumerMainThread(UpdateCursorPacket::handle)
            .add();
        
        // 更新锻造数据（服务端 -> 客户端）
        CHANNEL.messageBuilder(UpdateForgingDataPacket.class, messageID++)
            .encoder(UpdateForgingDataPacket::toBytes)
            .decoder(UpdateForgingDataPacket::new)
            .consumerMainThread(UpdateForgingDataPacket::handle)
            .add();
        
        // 同步石砧金属选择 GUI 数据（服务端 -> 客户端）
        CHANNEL.messageBuilder(SyncMetalSelectionDataPacket.class, messageID++)
            .encoder(SyncMetalSelectionDataPacket::toBytes)
            .decoder(SyncMetalSelectionDataPacket::new)
            .consumerMainThread(SyncMetalSelectionDataPacket::handle)
            .add();
        
        // 金属选择确认（客户端 -> 服务端）
        CHANNEL.messageBuilder(ConfirmMetalSelectionPacket.class, messageID++)
            .encoder(ConfirmMetalSelectionPacket::toBytes)
            .decoder(ConfirmMetalSelectionPacket::new)
            .consumerMainThread(ConfirmMetalSelectionPacket::handle)
            .add();
        
        // 锻造火花粒子效果（服务端 -> 客户端）
        CHANNEL.messageBuilder(ForgingSparkPacket.class, messageID++)
            .encoder(ForgingSparkPacket::toBytes)
            .decoder(ForgingSparkPacket::new)
            .consumerMainThread(ForgingSparkPacket::handle)
            .add();
        
        // 挖掘动作同步（服务端 -> 所有客户端）
        CHANNEL.messageBuilder(MiningActionPacket.class, messageID++)
            .encoder(MiningActionPacket::toBytes)
            .decoder(MiningActionPacket::new)
            .consumerMainThread(MiningActionPacket::handle)
            .add();
        
        // 应力值同步（服务端 -> 所有客户端）
        CHANNEL.messageBuilder(SyncStressPacket.class, messageID++)
            .encoder(SyncStressPacket::toBytes)
            .decoder(SyncStressPacket::new)
            .consumerMainThread(SyncStressPacket::handle)
            .add();

        // 火裂采矿热量同步（服务端 -> 所有客户端）
        CHANNEL.messageBuilder(FireCrackSyncPacket.class, messageID++)
            .encoder(FireCrackSyncPacket::toBytes)
            .decoder(FireCrackSyncPacket::new)
            .consumerMainThread(FireCrackSyncPacket::handle)
            .add();

        // 火裂采矿热量批量同步（服务端 -> 所有客户端）
        CHANNEL.messageBuilder(FireCrackBatchSyncPacket.class, messageID++)
            .encoder(FireCrackBatchSyncPacket::toBytes)
            .decoder(FireCrackBatchSyncPacket::new)
            .consumerMainThread(FireCrackBatchSyncPacket::handle)
            .add();

        // 爬行状态同步（服务端 -> 客户端）
        CHANNEL.messageBuilder(SyncCrawlStatePacket.class, messageID++)
            .encoder(SyncCrawlStatePacket::toBytes)
            .decoder(SyncCrawlStatePacket::new)
            .consumerMainThread(SyncCrawlStatePacket::handle)
            .add();

        // 吹管吹气粒子效果（服务端 -> 客户端）
        CHANNEL.messageBuilder(BlowpipeBurstPacket.class, messageID++)
            .encoder(BlowpipeBurstPacket::toBytes)
            .decoder(BlowpipeBurstPacket::new)
            .consumerMainThread(BlowpipeBurstPacket::handle)
            .add();

        // 力气数据同步（服务端 -> 客户端）
        CHANNEL.messageBuilder(SyncStrengthPacket.class, messageID++)
            .encoder(SyncStrengthPacket::toBytes)
            .decoder(SyncStrengthPacket::new)
            .consumerMainThread(SyncStrengthPacket::handle)
            .add();
    }
}