/*
 * This file is part of Debris, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.Debris.world;

import java.util.Random;

import javax.annotation.Nonnull;

import org.blockartistry.Debris.ModOptions;
import org.blockartistry.Debris.blocks.ModBlocks;
import org.blockartistry.Debris.blocks.BlockDebrisVariant.Variant;
import org.blockartistry.Debris.util.BlockStateWeightTable;
import org.blockartistry.Debris.util.MyUtils;
import org.blockartistry.Debris.util.WorldUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class DebrisBiomeDecorator {

	private static final int[] dimensionList = MyUtils.splitToInts(ModOptions.dimensionList, ',');
	private static final boolean dimensionListBlack = ModOptions.dimensionListAsBlack;
	private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	private static final int GROUND_ADJUST = 2;
	private static final int MIN_Y = 5;
	private static final int PLACE_ATTEMPTS = 2;

	private static final BlockStateWeightTable trashBlocks = new BlockStateWeightTable();

	static {
		for (final Variant v : Variant.values())
			trashBlocks.add(ModBlocks.DEBRIS.getBlockState(v), v.getWeight());
	}

	private IBlockState getTrashBlock(@Nonnull final Random rand) {
		return trashBlocks.nextState(rand);
	}

	private DebrisBiomeDecorator() {
	}

	private static boolean isGenAllowedInDimension(final int dimension) {
		boolean inList = false;
		for (final int d : dimensionList) {
			if (d == dimension) {
				inList = true;
				break;
			}
		}
		if (dimensionListBlack)
			return !inList;
		return inList;
	}

	private static boolean isGenerationAllowed(final DecorateBiomeEvent.Decorate event) {
		return (event.getResult() == Result.ALLOW || event.getResult() == Result.DEFAULT)
				&& event.getType() == EventType.FLOWERS
				&& isGenAllowedInDimension(event.getWorld().provider.getDimension());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onWorldDecoration(final DecorateBiomeEvent.Decorate event) {

		// Player/op may not want debris in world gen
		if (!ModOptions.enableDebris)
			return;

		if (isGenerationAllowed(event)) {

			// Calculate the range and scaling based on
			// the world sea level. Normal sea level is assumed
			// to be 64, which is the Overworld sea level.
			final int groundLevel = event.getWorld().provider.getAverageGroundLevel();
			final int toPlace = (int) (ModOptions.rubbleDensity * ((float) groundLevel / 64F));
			final int maxY = groundLevel - GROUND_ADJUST;
			final int spread = maxY - MIN_Y;

			// In case someone does something real funky with a
			// dimension.
			if (spread < 1 || toPlace < 1)
				return;

			final World world = event.getWorld();
			final Random rand = event.getRand();

			for (int i = 0; i < toPlace; i++) {

				final int x = event.getPos().getX() + rand.nextInt(16) + 8;
				final int z = event.getPos().getZ() + rand.nextInt(16) + 8;
				final int y = rand.nextInt(spread) + MIN_Y;

				pos.setPos(x, y, z);

				for (int j = 0; j < PLACE_ATTEMPTS; j++) {
					if (WorldUtils.isAirBlock(world, pos) && ModBlocks.DEBRIS.canBlockStay(world, pos, null)) {
						world.setBlockState(pos, getTrashBlock(rand));
						break;
					}
					pos.setY(pos.getY() - 1);
				}
			}
		}
	}

	public static void init() {
		MinecraftForge.TERRAIN_GEN_BUS.register(new DebrisBiomeDecorator());
	}

}
