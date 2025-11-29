package neko.neko.bedwarsscoreboardaddon.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import neko.neko.bedwarsscoreboardaddon.Main;
import neko.neko.bedwarsscoreboardaddon.arena.Arena;
import neko.neko.bedwarsscoreboardaddon.config.Config;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class ShopListener implements Listener {

	private WrappedDataWatcher.Serializer booleanserializer;

	public ShopListener() {
		if (!BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
			booleanserializer = WrappedDataWatcher.Registry.get(Boolean.class);
		}
		packetListener();
	}

	private void packetListener() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Main.getInstance(), ListenerPriority.HIGHEST, new PacketType[] { 
			PacketType.Play.Server.ENTITY_METADATA, 
			PacketType.Play.Server.NAMED_SOUND_EFFECT,
			PacketType.Play.Server.CUSTOM_SOUND_EFFECT
		}) {
			@Override
			public void onPacketSending(PacketEvent e) {
				PacketContainer packet = e.getPacket();
				if (packet.getType() == PacketType.Play.Server.ENTITY_METADATA) {
					int id = packet.getIntegers().read(0);
					if (isShopNPC(id)) {
						// Use direct list of WrappedWatchableObject for newer ProtocolLib versions
						List<WrappedWatchableObject> wrappedDataWatcher = new ArrayList<>();
						
						// Hide nametag (index 3)
						if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
							wrappedDataWatcher.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0));
						} else {
							wrappedDataWatcher.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, booleanserializer), false));
						}
						
						// Additionally, set the silent flag to make entity silent
						if (!BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
							try {
								WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
								// Index 4 is typically the SILENT datawatcher field in newer versions
								wrappedDataWatcher.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(4, booleanSerializer), true));
							} catch (Exception ex) {
								// Fallback if index 4 is not for silent flag in this version
							}
						}
						packet.getWatchableCollectionModifier().write(0, wrappedDataWatcher);
					}
				} else if (packet.getType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
					// Cancel villager and blaze sound effects
					try {
						if (packet.getStrings().size() > 0) {
							String soundName = packet.getStrings().read(0);
							if (soundName != null && (soundName.toLowerCase().contains("villager") || soundName.toLowerCase().contains("blaze") || soundName.toLowerCase().contains("entity.villager") || soundName.toLowerCase().contains("entity.blaze"))) {
								// Check if the sound is near any shop to cancel it
								for (Arena arena : Main.getInstance().getArenaManager().getArenas().values()) {
									if (arena.getShop() != null) {
										e.setCancelled(true);
										return;
									}
								}
							}
						}
					} catch (Exception ex) {
						// Handle potential index out of bounds errors gracefully
					}
				} else if (packet.getType() == PacketType.Play.Server.CUSTOM_SOUND_EFFECT) {
					// Cancel custom villager and blaze sound effects
					try {
						if (packet.getStrings().size() > 0) {
							String soundName = packet.getStrings().read(0);
							if (soundName != null && (soundName.toLowerCase().contains("villager") || soundName.toLowerCase().contains("blaze") || soundName.toLowerCase().contains("entity.villager") || soundName.toLowerCase().contains("entity.blaze"))) {
								// Check if the sound is near any shop to cancel it
								for (Arena arena : Main.getInstance().getArenaManager().getArenas().values()) {
									if (arena.getShop() != null) {
										e.setCancelled(true);
										return;
									}
								}
							}
						}
					} catch (Exception ex) {
						// Handle potential index out of bounds errors gracefully
					}
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCitizensEnable(CitizensEnableEvent e) {
		File folder = Config.getNPCFile();
		FileConfiguration npcconfig = YamlConfiguration.loadConfiguration(folder);
		if (npcconfig.getKeys(false).contains("npcs")) {
			List<String> npcs = npcconfig.getStringList("npcs");
			List<NPC> gamenpcs = new ArrayList<NPC>();
			for (NPC npc : CitizensAPI.getNPCRegistry().sorted()) {
				if (npcs.contains(npc.getId() + "")) {
					gamenpcs.add(npc);
				}
			}
			for (NPC npc : gamenpcs) {
				CitizensAPI.getNPCRegistry().deregister(npc);
			}
			npcconfig.set("npcs", new ArrayList<String>());
			try {
				npcconfig.save(folder);
			} catch (IOException e1) {
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNPCLeftClick(NPCLeftClickEvent e) {
		e.setCancelled(onNPCClick(e.getClicker(), e.getNPC(), e.isCancelled()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNPCRightClick(NPCRightClickEvent e) {
		e.setCancelled(onNPCClick(e.getClicker(), e.getNPC(), e.isCancelled()));
	}

	private boolean onNPCClick(Player player, NPC npc, boolean isCancelled) {
		Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
		if (game == null) {
			return isCancelled;
		}
		return Main.getInstance().getArenaManager().getArena(game.getName()).getShop().onNPCClick(player, npc, isCancelled);
	}

	private boolean isShopNPC(int id) {
		for (Arena arena : Main.getInstance().getArenaManager().getArenas().values()) {
			if (arena.getShop() != null && arena.getShop().isShopNPC(id)) {
				return true;
			}
		}
		return false;
	}
}