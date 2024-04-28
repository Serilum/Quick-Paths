package com.natamus.quickpaths.neoforge.events;

import com.natamus.quickpaths.events.PathEvent;
import net.neoforged.neoforge.event.TickEvent.Phase;
import net.neoforged.neoforge.event.TickEvent.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class NeoForgePathEvent {
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent e) {
		if (!e.phase.equals(Phase.END)) {
			return;
		}

		PathEvent.onServerTick(e.getServer());
	}
	
	@SubscribeEvent
	public static void onRightClickGrass(PlayerInteractEvent.RightClickBlock e) {
		if (!PathEvent.onRightClickGrass(e.getLevel(), e.getEntity(), e.getHand(), e.getPos(), e.getHitVec())) {
			e.setCanceled(true);
		}
	}
}
