/*
 The MIT License (MIT)

 Copyright (c) 2017 Wundero

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package me.rojo8399.placeholderapi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;

import me.rojo8399.placeholderapi.expansions.ConfigurableExpansion;
import me.rojo8399.placeholderapi.expansions.Expansion;
import me.rojo8399.placeholderapi.expansions.ListeningExpansion;
import me.rojo8399.placeholderapi.expansions.ReloadableExpansion;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class Registry {

	private Map<String, Expansion> registry = new ConcurrentHashMap<>();

	Registry() {
	}

	public boolean has(String id) {
		return registry.containsKey(id.toLowerCase());
	}

	public int refreshAll() {
		List<Expansion> toRefresh = registry.values().stream().collect(Collectors.toList());
		registry.clear();
		int count = 0;
		for (Expansion e : toRefresh) {
			count += refresh(e) ? 1 : 0;
		}
		return count;
	}

	public boolean refresh(Expansion e) {
		if (e instanceof ReloadableExpansion) {
			boolean b = ((ReloadableExpansion) e).reload();
			if (!b) {
				return b;
			}
		}
		if (e instanceof ListeningExpansion) {
			Sponge.getEventManager().unregisterListeners(e);
		}
		return register(e);
	}

	public boolean refresh(String id) {
		id = id.toLowerCase();
		if (!has(id)) {
			return false;
		}
		Expansion e = registry.remove(id);
		return refresh(e);
	}

	public Set<Expansion> getAll() {
		return registry.values().stream().collect(Collectors.toSet());
	}

	public boolean register(Expansion e) {
		if (e == null) {
			return false;
		}
		if (e.getIdentifier() == null || e.getIdentifier().isEmpty()) {
			return false;
		}
		boolean ru = false;
		if (e.getIdentifier().contains("_")) {
			ru = true;
		}
		if (registry.containsKey(e.getIdentifier().toLowerCase())) {
			return false;
		}
		if (e instanceof ConfigurableExpansion) {
			ConfigurableExpansion ce = (ConfigurableExpansion) e;
			ConfigurationNode node = PlaceholderAPIPlugin.getInstance().getRootConfig().getNode("expansions",
					e.getIdentifier());
			if (node.isVirtual()) {
				try {
					ObjectMapper.forObject(ce).serialize(node);
					PlaceholderAPIPlugin.getInstance().saveConfig();
				} catch (Exception e2) {
				}
			}
			try {
				e = ce = ObjectMapper.forObject(ce).populate(node);
			} catch (ObjectMappingException e1) {
				try {
					ObjectMapper.forObject(ce).serialize(node);
					PlaceholderAPIPlugin.getInstance().saveConfig();
				} catch (Exception e2) {
				}
				return false;
			}
		}
		if (!e.canRegister()) {
			return false;
		}
		if (e instanceof ListeningExpansion) {
			Sponge.getEventManager().registerListeners(
					((ListeningExpansion) e).getPlugin().orElse(PlaceholderAPIPlugin.getInstance()), e);
		}
		registry.put((ru ? e.getIdentifier().replace("_", "") : e.getIdentifier()).toLowerCase(), e);
		return true;
	}

	public Expansion get(String id) {
		if (!has(id)) {
			return null;
		}
		return registry.get(id);
	}

}
