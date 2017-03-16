/*
 * Copyright (c) 2017 NOVA, All rights reserved.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NOVA. If not, see <http://www.gnu.org/licenses/>.
 */

package nova.core.wrapper.mc.forge.v1_11_2.wrapper.capability.forward;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import nova.core.util.Direction;
import nova.core.util.EnumSelector;
import nova.core.wrapper.mc.forge.v1_11_2.wrapper.DirectionConverter;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author ExE Boss
 */
public interface NovaCapabilityProvider extends ICapabilityProvider {

	boolean hasCapabilities();

	/**
	 * Internal NOVA method. Used by wrappers to add wrapper specific capabilities.
	 *
	 * Ex. NOVA-Energy adds wrappers for EnergyStorage component -> IEnergyStorage capability.
	 *
	 * @param capability The capability to add.
	 * @param capabilityInstance The capability instance to add.
	 * @throws IllegalArgumentException if the capability is already present for the direction.
	 * @returns instance
	 */
	@Nonnull
	default <T> T addCapability(@Nonnull Capability<T> capability, @Nonnull T instance) {
		return addCapability(capability, instance, Direction.UNKNOWN);
	}

	/**
	 * Internal NOVA method. Used by wrappers to add wrapper specific capabilities.
	 *
	 * Ex. NOVA-Energy adds wrappers for EnergyStorage component -> IEnergyStorage capability.
	 *
	 * @param capability The capability to add.
	 * @param capabilityInstance The capability instance to add.
	 * @param directions The directions to add the capability to.
	 * @throws IllegalArgumentException if the capability is already present for the direction.
	 * @returns instance
	 */
	@Nonnull
	default <T> T addCapability(@Nonnull Capability<T> capability, @Nonnull T instance, @Nonnull Direction... directions) {
		return addCapability(capability, instance, EnumSelector.of(Direction.class).blockAll().apart(directions).lock());
	}

	/**
	 * Internal NOVA method. Used by wrappers to add wrapper specific capabilities.
	 *
	 * Ex. NOVA-Energy adds wrappers for EnergyStorage component -> IEnergyStorage capability.
	 *
	 * @param capability The capability to add.
	 * @param capabilityInstance The capability instance to add.
	 * @param direction The direction to add the capability to.
	 * @throws IllegalArgumentException if the capability is already present for the direction.
	 * @returns instance
	 */
	@Nonnull
	default <T> T addCapability(@Nonnull Capability<T> capability, @Nonnull T instance, @Nonnull Direction direction) {
		return addCapability(capability, instance, direction == Direction.UNKNOWN ?
			EnumSelector.of(Direction.class).allowAll().lock() :
			EnumSelector.of(Direction.class).blockAll().apart(direction).lock());
	}

	/**
	 * Internal NOVA method. Used by wrappers to add wrapper specific capabilities.
	 *
	 * Ex. NOVA-Energy adds wrappers for EnergyStorage component -> IEnergyStorage capability.
	 *
	 * @param capability The capability to add.
	 * @param instance The capability instance to add.
	 * @param directions The directions to add the capability to.
	 * @throws IllegalArgumentException if the capability is already present for the direction.
	 * @returns instance
	 */
	@Nonnull
	<T> T addCapability(@Nonnull Capability<T> capability, @Nonnull T instance, @Nonnull EnumSelector<Direction> directions);

	/**
	 * Determines if this object has support for the capability in question on the specific side.
	 * The return value of this might change during runtime if this object gains or looses support
	 * for a capability.
	 * <p>
	 * Example:
	 *   A Pipe getting a cover placed on one side causing it loose the {@link nova.core.component.inventory.Inventory} attachment function for that side.
	 *
	 * @param capability The capability to check
	 * @param direction The Side to check from: @link nova.core.util.Direction.UNKNOWN UNKNOWN}
	 * is defined to represent 'internal' or 'self' or used for cases where side doesn't matter.
	 * @return True if this object supports the capability for this side.
	 */
	boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull Direction direction);

	@Override
	default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		return hasCapability(capability, DirectionConverter.instance().toNova(facing));
	}

	/**
	 * Retrieves the handler for the capability requested on the specific side.
	 * The return value is {@link Optional#empty()} when the object does not support the capability for the direction.
	 * The return value can be the same for multiple faces.
	 * <p>
	 * @param capability The capability to check
	 * @param direction The Side to check from: @link nova.core.util.Direction.UNKNOWN UNKNOWN}
	 * is defined to represent 'internal' or 'self' or used for cases where side doesn't matter.
	 * @return The requested capability. Returns an empty optional when {@link #hasCapability(Capability, EnumFacing)} would return false.
	 */
	@Nonnull
	<T> Optional<T> getCapability(@Nonnull Capability<T> capability, @Nonnull Direction direction);

	@Override
	@Nullable
	default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		return getCapability(capability, DirectionConverter.instance().toNova(facing)).orElse(null);
	}
}
