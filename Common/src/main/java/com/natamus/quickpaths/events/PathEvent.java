package com.natamus.quickpaths.events;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.functions.BlockFunctions;
import com.natamus.collective.functions.MessageFunctions;
import com.natamus.collective.functions.NumberFunctions;
import com.natamus.collective.services.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

public class PathEvent {
	private static final HashMap<String, BlockPos> playernamelastpos = new HashMap<String, BlockPos>();
	private static final HashMap<BlockPos, Pair<Date, List<BlockPos>>> lastpath = new HashMap<BlockPos, Pair<Date, List<BlockPos>>>();
	
	private static int currenttick = 6000;
	
	public static void onServerTick(MinecraftServer minecraftServer) {
		if (currenttick != 0) {
			currenttick -= 1;
			return;
		}
		currenttick = 6000;
		
		Date now = new Date();
		
		List<BlockPos> toremove = new ArrayList<BlockPos>();
		HashMap<BlockPos, Pair<Date, List<BlockPos>>> loop = new HashMap<BlockPos, Pair<Date, List<BlockPos>>>(lastpath);
		for (BlockPos key : loop.keySet()) {
			
			Date pathdate = loop.get(key).getFirst();
			long ms = (now.getTime()-pathdate.getTime());
			if (ms > 300000) {
				toremove.add(key);
			}
		}
		
		for (BlockPos tr : toremove) {
			lastpath.remove(tr);
		}
	}
	
	public static boolean onRightClickGrass(Level level, Player player, InteractionHand hand, BlockPos targetpos, BlockHitResult hitVec) {
		if (level.isClientSide) {
			return true;
		}
		
		ItemStack handstack = player.getItemInHand(hand);
		if (!Services.TOOLFUNCTIONS.isShovel(handstack)) {
			return true;
		}
		
		Date now = new Date();
		Block block = level.getBlockState(targetpos).getBlock();
		if (block.equals(Blocks.AIR)) {
			targetpos = targetpos.below().immutable();
			block = level.getBlockState(targetpos).getBlock();
		}
		
		if (block.equals(Blocks.DIRT_PATH)) {
			if (lastpath.containsKey(targetpos)) {
				int count = 0;
				Pair<Date, List<BlockPos>> pair = lastpath.get(targetpos);
				
				long ms = (now.getTime()-pair.getFirst().getTime());
				if (ms < 300000) {
					for (BlockPos pathpos : pair.getSecond()) {
						if (level.getBlockState(pathpos).getBlock().equals(Blocks.DIRT_PATH) && level.getBlockState(pathpos.immutable().above()).getBlock().equals(Blocks.AIR)) {
							level.setBlockAndUpdate(pathpos, Blocks.GRASS_BLOCK.defaultBlockState());
							count+=1;
						}
					}
				}
				
				lastpath.remove(targetpos);
				MessageFunctions.sendMessage(player, "[Quick Paths] " + count + " grass blocks restored.", ChatFormatting.DARK_GREEN);
				return false;
			}
		}
		else if (!block.equals(Blocks.GRASS_BLOCK)) {
			return true;
		}
		
		if (handstack.getDamageValue() >= handstack.getMaxDamage()-1 && player.isCrouching()) {
			MessageFunctions.sendMessage(player, "[Quick Paths] Your shovel is too damaged to create paths.", ChatFormatting.RED);
			return false;
		}
		
		String playername = player.getName().getString();
		if (playernamelastpos.containsKey(playername) && !player.isCrouching()) {
			BlockPos lastpos = playernamelastpos.get(playername);
			
			boolean movex = true;
			int difx = lastpos.getX()-targetpos.getX();
			int difz = lastpos.getZ()-targetpos.getZ();
			int begindifx = difx;
			int begindifz = difz;
			
			List<Pair<Integer, Integer>> xzset = new ArrayList<Pair<Integer, Integer>>();
			List<BlockPos> pathpositions = new ArrayList<BlockPos>(Arrays.asList(lastpos));
			for (int lyd = lastpos.getY()-10; lyd < lastpos.getY()+10; lyd += 1) {
				difx = begindifx;
				difz = begindifz;
				
				while (difx != 0 || difz != 0) {
					if (movex) {
						difx += NumberFunctions.moveToZero(difx);
						movex = difz == 0;
					}
					else {
						difz += NumberFunctions.moveToZero(difz);
						movex = difx != 0;
					}
					Pair<Integer, Integer> xz = new Pair<>(targetpos.getX()+difx, targetpos.getZ()+difz);
					if (!xzset.contains(xz)) {
						BlockPos betweenpos = new BlockPos(targetpos.getX() + difx, lyd, targetpos.getZ() + difz);
						if (level.getBlockState(betweenpos).getBlock().equals(Blocks.GRASS_BLOCK)) {
							BlockPos abovepos = betweenpos.immutable().above();
							Block aboveblock = level.getBlockState(abovepos).getBlock();
							if (!aboveblock.equals(Blocks.AIR)) {
								if (aboveblock instanceof BushBlock || aboveblock instanceof CropBlock) {
									BlockFunctions.dropBlock(level, abovepos);
								}
								else {
									return true;
								}
							}
							
							level.setBlockAndUpdate(betweenpos, Blocks.DIRT_PATH.defaultBlockState());
							
							pathpositions.add(betweenpos.immutable());
							xzset.add(xz);
							
							if (!player.isCreative()) {
								handstack.hurtAndBreak(1, (ServerLevel)level, null, (item) -> {});
							}
						}
					}
				}
			}
			
			if (handstack.getDamageValue() > handstack.getMaxDamage()) {
				handstack.setDamageValue(handstack.getMaxDamage()-1);
			}
			
			lastpath.put(targetpos, new Pair<>(now, pathpositions));
			playernamelastpos.remove(playername);
			MessageFunctions.sendMessage(player, "[Quick Paths] Path of " + pathpositions.size() + " blocks created. To undo, right click last clicked block again.", ChatFormatting.DARK_GREEN);
		}
		else {
			if (!player.isCrouching()) {
				return true;
			}
			
			level.setBlockAndUpdate(targetpos, Blocks.DIRT_PATH.defaultBlockState());
			
			if (playernamelastpos.containsKey(playername)) {
				BlockPos lastpos = playernamelastpos.get(playername);
				
				if (lastpos != targetpos) {
					if (level.getBlockState(lastpos).getBlock().equals(Blocks.DIRT_PATH)) {
						level.setBlockAndUpdate(lastpos, Blocks.GRASS_BLOCK.defaultBlockState());
					}
				}
			}
			playernamelastpos.put(playername, targetpos);
			MessageFunctions.sendMessage(player, "[Quick Paths] Starting point set to " + targetpos.getX() + ", " + targetpos.getY() + ", " + targetpos.getZ() + ".", ChatFormatting.DARK_GREEN);
			return false;
		}
		
		return true;
	}
}
