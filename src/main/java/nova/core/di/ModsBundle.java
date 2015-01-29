package nova.core.di;

import java.util.Set;

import com.google.common.collect.Sets;

import se.jbee.inject.bootstrap.BootstrapperBundle;
import se.jbee.inject.bootstrap.Bundle;

public class ModsBundle extends BootstrapperBundle{

	private static Set<Class<? extends Bundle>> bundles = Sets.newHashSet();
	
	@Override
	protected void bootstrap() {
		install(WrapperBundle.class);
		
		bundles.stream().forEach(bundle-> this.install(bundle));
	}

	public static void add(Class<? extends Bundle> bundle) {
		bundles.add(bundle);
	}
	public static boolean remove(Class<? extends Bundle> bundle) {
		return bundles.remove(bundle);
	}
	
}