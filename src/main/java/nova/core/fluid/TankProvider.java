package nova.core.fluid;

import java.util.Set;

/**
 * An object that provides a fluid container.
 * Items will implement this as they are not direction sensitive.
 * @author Calclavia
 */
//TODO: Switch to component system
public interface TankProvider {
	Set<Tank> getTanks();
}
