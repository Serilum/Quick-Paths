package com.natamus.quickpaths.forge.events;

import com.natamus.quickpaths.events.PathEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandles;

public class ForgePathEvent {
	public static void registerEventsInBus() {
		BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgePathEvent.class);
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post e) {
		PathEvent.onServerTick(e.getServer());
	}
	
	@SubscribeEvent
	public static boolean onRightClickGrass(PlayerInteractEvent.RightClickBlock e) {
		if (!PathEvent.onRightClickGrass(e.getLevel(), e.getEntity(), e.getHand(), e.getPos(), e.getHitVec())) {
			return true;
		}
		return false;
	}
}
