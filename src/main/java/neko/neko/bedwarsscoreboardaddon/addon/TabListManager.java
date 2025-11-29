package neko.neko.bedwarsscoreboardaddon.addon;

import io.github.bedwarsrel.BedwarsRel;

import io.github.bedwarsrel.events.BedwarsGameStartedEvent;

import io.github.bedwarsrel.events.BedwarsGameEndEvent;

import io.github.bedwarsrel.events.BedwarsPlayerJoinedEvent;

import io.github.bedwarsrel.events.BedwarsPlayerLeaveEvent;

import io.github.bedwarsrel.game.Game;

import io.github.bedwarsrel.game.GameState;

import neko.neko.bedwarsscoreboardaddon.Main;

import neko.neko.bedwarsscoreboardaddon.utils.PlaceholderAPIUtil;

import org.bukkit.Bukkit;

import org.bukkit.ChatColor;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerJoinEvent;

import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.scheduler.BukkitRunnable;


import com.comphenix.protocol.PacketType;

import com.comphenix.protocol.ProtocolLibrary;

import com.comphenix.protocol.ProtocolManager;

import com.comphenix.protocol.events.PacketContainer;

import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class TabListManager implements Listener {

    private Main plugin;
    private boolean enabled;

    public TabListManager(Main plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        if (enabled) return;
        enabled = true;
        
        // 注册监听器
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // 每秒更新一次Tab列表
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayerTabList();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void disable() {
        if (!enabled) return;
        enabled = false;
    }

    /**
     * 更新所有玩家的Tab列表
     */
    private void updateAllPlayerTabList() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTabList(player);
        }
    }

    /**

     * 更新特定玩家的Tab列表

     */

    public void updatePlayerTabList(Player player) {

        Game playerGame = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

        

        if (playerGame != null) {

            // 只有当玩家自己在游戏或等待状态时，才应用自定义Tab样式

            for (Player target : Bukkit.getOnlinePlayers()) {

                Game targetGame = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(target);

                

                if (targetGame != null) {

                    // 目标玩家在游戏或等待状态中，应用自定义Tab样式

                    String displayName = getPlayerTabDisplayName(target, targetGame);

                    // 设置Tab列表中显示的名称

                    target.setPlayerListName(displayName);

                    // 同时更新玩家的显示名称

                    target.setDisplayName(displayName);

                } else {

                    // 目标玩家在大厅，重置为原版名称

                    target.setPlayerListName(null); // 重置为默认名称

                    target.setDisplayName(target.getName()); // 重置显示名称为原名

                }

            }

        } else {

            // 如果玩家不在游戏中，完全不修改任何Tab显示，让其他插件控制

            // 重置所有玩家名称为默认值，避免Tab插件冲突

            for (Player target : Bukkit.getOnlinePlayers()) {

                target.setPlayerListName(null);

                target.setDisplayName(target.getName());

            }

        }

    }

    

    /**

     * 发送Tab列表头尾给玩家

     */

    private void sendTabListHeaderFooter(Player player, String header, String footer) {

        try {

            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

            packet.getChatComponents().write(0, WrappedChatComponent.fromText(header));

            packet.getChatComponents().write(1, WrappedChatComponent.fromText(footer));

            protocolManager.sendServerPacket(player, packet);

        } catch (Exception e) {

            // 如果ProtocolLib方法失败，使用备用方法

            player.setPlayerListName(player.getName()); // 重置名称

        }

    }

    /**
     * 获取玩家在Tab列表中显示的名称
     */
    private String getPlayerTabDisplayName(Player player, Game game) {
        String suffix = "";
        String teamName = "";
        String playerName = player.getName();
        String serverName = "梦幻次元";
        String qqGroup = "QQ群:956908360";
        
        // 获取权限组后缀
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            suffix = PlaceholderAPIUtil.setPlaceholders(player, "%luckperms_suffix%");
            if (suffix != null && !suffix.isEmpty() && !suffix.equals("%luckperms_suffix%")) {
                suffix = ChatColor.translateAlternateColorCodes('&', suffix);
            } else {
                suffix = "";
            }
        }
        
        // 根据游戏状态获取玩家队伍名
        if (game != null && game.getPlayers().contains(player)) {
            // 检查游戏是否正在运行
            if (game.getState() == GameState.RUNNING) {
                // 玩家在游戏中且游戏正在运行
                io.github.bedwarsrel.game.Team playerTeam = game.getPlayerTeam(player);
                if (playerTeam != null) {
                    teamName = playerTeam.getChatColor() + playerTeam.getName();
                }
                
                // 游戏中的Tab样式
                String header = ChatColor.GOLD + "" + ChatColor.BOLD + "» " + ChatColor.WHITE + "" + ChatColor.BOLD + serverName + " " + ChatColor.GOLD + "" + ChatColor.BOLD + "«\n" +
                               ChatColor.GREEN + "游戏中";
                String footer = ChatColor.GRAY + "» " + ChatColor.AQUA + qqGroup + ChatColor.GRAY + " «\n" + 
                               ChatColor.GRAY + "对局人数: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size();
                
                // 设置Tab列表的页眉和页脚
                sendTabListHeaderFooter(player, header, footer);
            } else {
                // 玩家在游戏中但游戏尚未开始（等待中）
                io.github.bedwarsrel.game.Team playerTeam = game.getPlayerTeam(player);
                if (playerTeam != null) {
                    teamName = playerTeam.getChatColor() + playerTeam.getName();
                } else {
                    teamName = ChatColor.GRAY + "等待中";
                }
                
                // 等待中的Tab样式
                String header = ChatColor.GOLD + "" + ChatColor.BOLD + "» " + ChatColor.WHITE + "" + ChatColor.BOLD + serverName + " " + ChatColor.GOLD + "" + ChatColor.BOLD + "«\n" +
                               ChatColor.YELLOW + "等待中";
                String footer = ChatColor.GRAY + "» " + ChatColor.AQUA + qqGroup + ChatColor.GRAY + " «\n" + 
                               ChatColor.GRAY + "对局人数: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size();
                
                // 设置Tab列表的页眉和页脚
                sendTabListHeaderFooter(player, header, footer);
            }
        } else {
            // 玩家不在任何游戏中，不发送自定义Tab页眉页脚，让玩家看到原版样式
            teamName = ChatColor.GRAY + "大厅";
        }
        
        // 格式: suffix | 队伍名 | 玩家名
        String displayName;
        if (!suffix.isEmpty() && !teamName.isEmpty()) {
            displayName = suffix + " " + ChatColor.GRAY + "| " + teamName + " " + ChatColor.GRAY + "| " + playerName;
        } else if (!suffix.isEmpty()) {
            displayName = suffix + " " + ChatColor.GRAY + "| " + playerName;
        } else if (!teamName.isEmpty()) {
            displayName = teamName + " " + ChatColor.GRAY + "| " + playerName;
        } else {
            displayName = playerName;
        }
        
        // 确保总长度不超过限制
        if (displayName.length() > 32) {
            displayName = displayName.substring(0, 32);
        }
        
        return displayName;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 玩家加入时更新Tab列表
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerTabList(event.getPlayer());
            }
        }.runTaskLater(plugin, 20L); // 延迟1秒，确保玩家完全加入
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家退出时无需特殊处理
    }
    
    @EventHandler
    public void onGameStarted(BedwarsGameStartedEvent event) {
        // 游戏开始时更新所有玩家的Tab列表
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayerTabList();
            }
        }.runTaskLater(plugin, 5L); // 延迟更新
    }
    
    @EventHandler
    public void onGameEnd(BedwarsGameEndEvent event) {
        // 游戏结束时更新所有玩家的Tab列表
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayerTabList();
            }
        }.runTaskLater(plugin, 5L); // 延迟更新
    }
    
    @EventHandler
    public void onPlayerJoinGame(BedwarsPlayerJoinedEvent event) {
        // 玩家加入游戏时更新Tab列表
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerTabList(event.getPlayer());
            }
        }.runTaskLater(plugin, 5L); // 延迟更新
    }
    
    @EventHandler
    public void onPlayerLeaveGame(BedwarsPlayerLeaveEvent event) {
        // 玩家离开游戏时更新Tab列表
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerTabList(event.getPlayer());
            }
        }.runTaskLater(plugin, 5L); // 延迟更新
    }
}