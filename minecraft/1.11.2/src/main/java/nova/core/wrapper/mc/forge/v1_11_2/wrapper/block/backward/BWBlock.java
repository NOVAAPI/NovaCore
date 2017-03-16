/*
 * Copyright (c) 2015 NOVA, All rights reserved.
 * This library is free software, licensed under GNU Lesser General Public License version 3
 *
 * This file is part of NOVA.
 *
 * NOVA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NOVA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NOVA.  If not, see <http://www.gnu.org/licenses/>.
 */

package nova.core.wrapper.mc.forge.v1_11_2.wrapper.block.backward;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import nova.core.block.Block;
import nova.core.block.component.BlockProperty;
import nova.core.block.component.LightEmitter;
import nova.core.component.misc.Collider;
import nova.core.component.renderer.StaticRenderer;
import nova.core.component.transform.BlockTransform;
import nova.core.item.ItemFactory;
import nova.core.retention.Data;
import nova.core.retention.Storable;
import nova.core.retention.Store;
import nova.core.sound.Sound;
import nova.core.util.shape.Cuboid;
import nova.core.world.World;
import nova.core.wrapper.mc.forge.v1_11_2.util.WrapperEvent;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.block.world.WorldConverter;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.cuboid.CuboidConverter;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.data.DataConverter;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.entity.EntityConverter;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.item.ItemConverter;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.render.backward.BWBakedModel;
import nova.internal.core.Game;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BWBlock extends Block implements Storable {
	private final IBlockState blockState;
	@Store
	private TileEntity mcTileEntity;

	public BWBlock(net.minecraft.block.Block block) {
		this.blockState = block.getDefaultState();
	}

	@SuppressWarnings("deprecation")
	public BWBlock(IBlockState incompleteBlockState, World world, Vector3D pos) {
		BlockTransform transform = components.add(new BlockTransform());
		transform.setWorld(world);
		transform.setPosition(pos);
		incompleteBlockState = incompleteBlockState.getActualState(blockAccess(), blockPos());
		this.blockState = incompleteBlockState.getBlock().getExtendedState(incompleteBlockState, blockAccess(), blockPos());

		components.add(new BlockProperty.Opacity().setOpacity(blockState().getMaterial().blocksLight() ? 1 : 0));

		BlockProperty.BlockSound blockSound = components.add(new BlockProperty.BlockSound());
		SoundType soundType;
		if (blockAccess() instanceof net.minecraft.world.World)
			soundType = block().getSoundType(blockState(), (net.minecraft.world.World) blockAccess(), blockPos(), null);
		else
			soundType = block().getSoundType();

		if (soundType.getPlaceSound() != null)
			blockSound.setBlockSound(BlockProperty.BlockSound.BlockSoundTrigger.PLACE,
				new Sound(soundType.getPlaceSound().getSoundName().getResourceDomain(),
				          soundType.getPlaceSound().getSoundName().getResourcePath()));

		if (soundType.getBreakSound() != null)
			blockSound.setBlockSound(BlockProperty.BlockSound.BlockSoundTrigger.BREAK,
				new Sound(soundType.getBreakSound().getSoundName().getResourceDomain(),
				          soundType.getBreakSound().getSoundName().getResourcePath()));

		if (soundType.getStepSound() != null)
			blockSound.setBlockSound(BlockProperty.BlockSound.BlockSoundTrigger.WALK,
				new Sound(soundType.getStepSound().getSoundName().getResourceDomain(),
				          soundType.getStepSound().getSoundName().getResourcePath()));

		components.add(new LightEmitter()).setEmittedLevel(() -> blockState().getLightValue(blockAccess(), blockPos()) / 15d);
		components.add(new Collider(this))
			.setBoundingBox(() -> {
				AxisAlignedBB aabb = blockState().getBoundingBox(blockAccess(), blockPos());
				return new Cuboid(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
			}).setOcclusionBoxes(entity -> {
				List<AxisAlignedBB> aabbs = new ArrayList<>();
				if (blockAccess() instanceof net.minecraft.world.World)
					blockState().addCollisionBoxToList(
						(net.minecraft.world.World) blockAccess(),
						blockPos(),
						CuboidConverter.instance().toNative(entity
							.flatMap(e -> e.components.getOp(Collider.class))
							.map(c -> c.boundingBox.get())
							.orElseGet(() -> Cuboid.ONE.add(pos))),
						aabbs,
						entity.map(EntityConverter.instance()::toNative).orElse(null),
						true
					);
				return aabbs.stream()
					.map(aabb -> (Cuboid) Game.natives().toNova(aabb))
					.map(cuboid -> cuboid.subtract(pos))
					.collect(Collectors.toSet());
			}).setSelectionBoxes(entity -> {
				final AxisAlignedBB bb;
				if (blockAccess() instanceof net.minecraft.world.World) {
					bb = block().getSelectedBoundingBox(blockState(), ((net.minecraft.world.World) blockAccess()), blockPos());
				} else {
					bb = blockState().getBoundingBox(blockAccess(), blockPos()).offset(blockPos());
				}
				Cuboid cuboid = Game.natives().toNova(bb);
				return Collections.singleton(cuboid.subtract(position()));
			});
		components.add(new StaticRenderer())
			.onRender(model -> {
				switch (blockState().getRenderType()) {
					case INVISIBLE:
						// rendering of invisible type
						break;
					case LIQUID:
						// fluid rendering
						//  TODO
						break;
					case ENTITYBLOCK_ANIMATED:
						// dynamic block rendering
						//  Handled by DynamicRenderer
						break;
					case MODEL:
						// model rendering
						model.addChild(new BWBakedModel(Minecraft.getMinecraft().getBlockRendererDispatcher()
							.getModelForState(blockState()), DefaultVertexFormats.BLOCK,
							Optional.of(blockState()), MathHelper.getPositionRandom(blockPos())));
						break;
					default:
						break;
				}
			});
		// TODO: TileEntity rendering using DynamicRenderer

		WrapperEvent.BWBlockCreate event = new WrapperEvent.BWBlockCreate(world, pos, this, block());
		Game.events().publish(event);
	}

	@Override
	public ItemFactory getItemFactory() {
		return ItemConverter.instance().toNova(Item.getItemFromBlock(block()));
	}

	public net.minecraft.block.Block block() {
		return blockState.getBlock();
	}

	public int meta() {
		return block().getMetaFromState(blockState());
	}

	public BlockPos blockPos() {
		return new BlockPos(x(), y(), z());
	}

	public IBlockAccess blockAccess() {
		return WorldConverter.instance().toNative(world());
	}

	public IBlockState blockState() {
		return blockState;
	}

	public Optional<TileEntity> tile() {
		if (mcTileEntity == null && block().hasTileEntity(blockState())) {
			mcTileEntity = blockAccess().getTileEntity(blockPos());
		}
		return Optional.ofNullable(mcTileEntity);
	}

	@Override
	public boolean canReplace() {
		return block().canPlaceBlockAt((net.minecraft.world.World) blockAccess(), blockPos());
	}

	@Override
	public boolean shouldDisplacePlacement() {
		if (block() == Blocks.SNOW_LAYER && (blockState().getValue(BlockSnow.LAYERS) < 1)) {
			return false;
		}

		if (block() == Blocks.VINE || block() == Blocks.TALLGRASS || block() == Blocks.DEADBUSH || block().isReplaceable(blockAccess(), blockPos())) {
			return false;
		}
		return super.shouldDisplacePlacement();
	}

	@Override
	public void save(Data data) {
		Storable.super.save(data);
		tile().ifPresent(tile -> data.putAll(DataConverter.instance().toNova(tile.serializeNBT())));
	}

	@Override
	public void load(Data data) {
		Storable.super.load(data);
		tile().ifPresent(tile -> tile.deserializeNBT(Game.natives().toNative(data)));
	}

	@Override
	public String getLocalizedName() {
		return block().getLocalizedName();
	}

	@Override
	public String getUnlocalizedName() {
		return block().getUnlocalizedName();
	}

	@Override
	public String toString() {
		return getID() + '(' + world() + '@' + x() + ',' + y() + ',' + z() + ')';
	}
}
