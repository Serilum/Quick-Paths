package com.natamus.quickpaths.neoforge.events;

import com.natamus.quickpaths.events.PathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class NeoForgePathEvent {
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post e) {
		PathEvent.onServerTick(e.getServer());
	}
	
	@SubscribeEvent
	public static void onRightClickGrass(PlayerInteractEvent.RightClickBlock e) {
		if (!PathEvent.onRightClickGrass(e.getLevel(), e.getEntity(), e.getHand(), e.getPos(), e.getHitVec())) {
			e.setCanceled(true);
		}
	}
}
