package com.natamus.quickpaths.forge.events;

import com.natamus.quickpaths.events.PathEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgePathEvent {
	@SubscribeEvent
	public void onServerTick(ServerTickEvent e) {
		if (!e.phase.equals(Phase.END)) {
			return;
		}

		PathEvent.onServerTick(null);
	}
	
	@SubscribeEvent
	public void onRightClickGrass(PlayerInteractEvent.RightClickBlock e) {
		if (!PathEvent.onRightClickGrass(e.getWorld(), e.getPlayer(), e.getHand(), e.getPos(), e.getHitVec())) {
			e.setCanceled(true);
		}
	}
}
