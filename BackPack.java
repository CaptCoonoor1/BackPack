package me.prostedeni.goodcraft.backpack;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class BackPack extends JavaPlugin implements Listener {

    //HashMap of all players and IDs of open BackPacks
    HashMap<String, String> openPack = new HashMap<String, String>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
    }

    //Event for when player clicks with the BackPack
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRightClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                if (e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.CHEST)) {
                    ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                    if (meta != null) {
                        if (meta.getLore() != null) {
                            e.setCancelled(true);
                            String lore = meta.getLore().get(0);
                            String lorePlain = ChatColor.stripColor(lore);
                            String loreNum = lorePlain.replaceFirst("BackPack", "");
                            if (!(getConfig().getBoolean("OwnerOnly"))) {
                                openPack.put(e.getPlayer().getName(), loreNum);
                                MakeGui(e.getPlayer());
                            } else if (getConfig().getBoolean("OwnerOnly")){
                                String lore1 = meta.getLore().get(1);
                                String lore1Plain = ChatColor.stripColor(lore1);
                                String lore1Owner = lore1Plain.replaceFirst("Owner ", "");
                                if (e.getPlayer().getName().equals(lore1Owner)){
                                    openPack.put(e.getPlayer().getName(), loreNum);
                                    MakeGui(e.getPlayer());
                                } else {
                                    e.getPlayer().sendMessage(ChatColor.DARK_RED + "Only Owner can open this BackPack");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //Method that makes the GUI, and makes specified player open it
    public void MakeGui(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, "BackPack");
        if (getConfig().getString(openPack.get(player.getName())) != null) {
            for (String s : getConfig().getConfigurationSection(openPack.get(player.getName())).getKeys(false)) {
                inv.setItem(Integer.valueOf(s), (ItemStack) getConfig().get(openPack.get(player.getName()) + "." + s));
            }
        }
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, getConfig().getLong("SoundVolume"), 0);
    }

    //Event that cancels action so that you cannot put BackPack inside itself
    //additionally, also cancels it if any other BackPack is being moved,
    //and value BackPackCeption is false in the config
    @EventHandler
    public void onPlayer(InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        if (e.getView().getTitle().equals("BackPack") && openPack.containsKey(p.getName())) {
            if (e.getCurrentItem() != null) {
                if (e.getCurrentItem().getType().equals(Material.CHEST)) {
                    if (e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().getLore() != null) {
                        ItemMeta meta = e.getCurrentItem().getItemMeta();
                        if (meta.getLore() != null) {
                            String lore = meta.getLore().get(0);
                            String lorePlain = ChatColor.stripColor(lore);
                            String loreNum = lorePlain.replaceFirst("BackPack", "");
                            if (loreNum.equals(openPack.get(p.getName()))) {
                                e.setCancelled(true);
                            }
                            if (!(getConfig().getBoolean("BackPackCeption"))){
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    //Removes player and backpack ID from HashMap
    @EventHandler
    public void onInventoryInteract(final InventoryCloseEvent e) {
        final Player p = (Player) e.getPlayer();
        if(e.getView().getTitle().equals("BackPack") && openPack.containsKey(p.getName())) {
            int position = 0;
            getConfig().set(openPack.get(p.getName()),null);
            for (ItemStack current : e.getInventory().getContents()) {
                if(current!=null && current.getType()!=Material.AIR) {
                    getConfig().set(openPack.get(p.getName())+"."+position,current);
                }
                ++position;
            }
            saveConfig();
            openPack.remove(p.getName());
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, getConfig().getLong("SoundVolume"), 0);
        }
    }

    //This is one monstrously big command
    //Would've moved it into separate class if i had idea it will be 2/3 of entire plugin
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("backpack")){
            if (sender instanceof Player){
                if (sender.hasPermission("backpack.make") || sender.hasPermission("backpack.retrieve") || sender.hasPermission("backpack.admin")) {
                    if (args.length == 0) {
                        if (sender.hasPermission("backpack.make") || sender.hasPermission("backpack.admin")) {
                            boolean allowed = true;
                            if (sender.hasPermission("backpack.make") && !sender.hasPermission("backpack.admin")) {

                                //This checks if player has permission of backpack.make.(number)
                                //To limit maximum number of BackPacks one player can have

                                int max = 0;
                                for (int x = getConfig().getInt("MaxBackPacks"); x > 0; x--) {
                                    if (sender.hasPermission("backpack.make." + x)) {
                                        max = x;
                                        break;
                                        //This picks the highest (first) ocurrence of backpack.make.(number)
                                        //And puts it into max
                                    }
                                }
                                if (max > 0) {
                                    //If max is higher than zero, then player does have that permission

                                    if (getConfig().getConfigurationSection("data." + sender.getName() + ".backpack.") != null) {
                                        ArrayList<String> backpacks = new ArrayList<>();
                                        for (String s : getConfig().getConfigurationSection("data." + sender.getName() + ".backpack.").getKeys(false)) {
                                            backpacks.add(s);
                                            //This determines how many backpacks player already has
                                        }
                                        if (backpacks.size() == max) {
                                            allowed = false;
                                            //Sets it to false if sender reached the limit
                                        } else if (backpacks.size() < max) {
                                            allowed = true;
                                        }
                                    }
                                }
                            }

                            if (allowed){
                                if (((Player) sender).getInventory().firstEmpty() != -1) {
                                    //Asks if sender's inventory is not full
                                    int randomNumber = ThreadLocalRandom.current().nextInt(1, 65536);
                                    while (getConfig().getList(String.valueOf(randomNumber)) != null) {
                                        randomNumber = ThreadLocalRandom.current().nextInt(1, 65536);
                                        if (getConfig().getString(String.valueOf(randomNumber)) == null) {
                                            break;
                                            //This generates a random number and makes sure it is unique
                                        }
                                    }
                                    ItemStack backpack = new ItemStack(Material.CHEST, 1);
                                    ItemMeta meta = backpack.getItemMeta();
                                    List<String> lores = new ArrayList<String>();
                                    if (meta.getLore() != null) lores = meta.getLore();
                                    lores.add(ChatColor.translateAlternateColorCodes('&', "&" + Integer.toHexString(ThreadLocalRandom.current().nextInt(1, 16)) + "BackPack &r&6" + randomNumber));
                                    lores.add(ChatColor.translateAlternateColorCodes('&', "&" + Integer.toHexString(ThreadLocalRandom.current().nextInt(1, 16)) + "Owner &r&3" + sender.getName()));
                                    meta.setLore(lores);
                                    backpack.setItemMeta(meta);
                                    getConfig().set("data." + sender.getName() + ".backpack." + randomNumber, "");
                                    ((Player) sender).getInventory().addItem(backpack);
                                    sender.sendMessage(ChatColor.DARK_AQUA + "You have been given a BackPack");
                                    lores.clear();
                                    //This whole section just stores backpack ID in lore of the item
                                } else {
                                    sender.sendMessage(ChatColor.DARK_RED + "Your inventory is full");
                                }
                            } else {
                                sender.sendMessage(ChatColor.DARK_RED + "You have reached the limit of BackPacks you can make");
                            }
                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "You don't have sufficient permissions for that");
                        }
                    } else if (args.length == 1) {
                        if (sender.hasPermission("backpack.admin")) {
                            if (args[0].equalsIgnoreCase("reload")) {
                                reloadConfig();
                                saveConfig();
                                sender.sendMessage(ChatColor.DARK_GREEN + "Config reloaded");
                                } else {
                                    if (getConfig().getConfigurationSection("data.") != null) {
                                        if (getConfig().getConfigurationSection("data.").contains(args[0])) {
                                            int i = 1;
                                            sender.sendMessage(ChatColor.DARK_GREEN + "Player " + args[0] + " has these backpacks:");
                                            for (String s : getConfig().getConfigurationSection("data." + args[0] + ".backpack.").getKeys(false)) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l" + i + ". &r&3" + s));
                                                i++;
                                                //This generates a list of all backpacks specified player has and sends them to sender
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.DARK_RED + "This player doesn't have any BackPacks");
                                        }
                                    } else {
                                        sender.sendMessage(ChatColor.DARK_RED + "No BackPacks have been created");
                                    }
                            }
                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "You don't have sufficient permissions for that");
                        }
                    } else if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("retrieve")){
                            if (sender.hasPermission("backpack.retrieve") || sender.hasPermission("backpack.admin")) {
                                if (getConfig().getConfigurationSection("data." + sender.getName() + ".") != null) {
                                        List<String> IDs = new ArrayList<String>();
                                        for (String s : getConfig().getConfigurationSection("data." + sender.getName() + ".backpack.").getKeys(false)) {
                                            IDs.add(s);
                                        }
                                        if (IDs.contains(args[1])){

                                            if (((Player) sender).getPlayer().getInventory().firstEmpty() != -1) {
                                                ItemStack backpack = new ItemStack(Material.CHEST, 1);
                                                ItemMeta meta = backpack.getItemMeta();
                                                List<String> lores = new ArrayList<String>();
                                                if (meta.getLore() != null) lores = meta.getLore();
                                                lores.add(ChatColor.translateAlternateColorCodes('&', "&" + Integer.toHexString(ThreadLocalRandom.current().nextInt(1, 16)) + "BackPack &r&6" + args[1]));
                                                lores.add(ChatColor.translateAlternateColorCodes('&', "&" + Integer.toHexString(ThreadLocalRandom.current().nextInt(1, 16)) + "Owner &r&3" + sender.getName()));
                                                meta.setLore(lores);
                                                backpack.setItemMeta(meta);
                                                getConfig().set("data." + sender.getName() + ".backpack." + args[1], "");
                                                ((Player) sender).getInventory().addItem(backpack);
                                                sender.sendMessage(ChatColor.DARK_AQUA + "Your BackPack has been retrieved");
                                                lores.clear();
                                        } else {
                                            sender.sendMessage(ChatColor.DARK_RED + "Your inventory is full");
                                        }
                                    } else {
                                            sender.sendMessage(ChatColor.DARK_RED + "No BackPack with that ID exists");
                                        }
                                } else {
                                    sender.sendMessage(ChatColor.DARK_RED + "Specified BackPack ID doesn't exist");
                                }
                            } else {
                                sender.sendMessage(ChatColor.DARK_RED + "You don't have sufficient permissions for that");
                            }
                        } else {
                        if (sender.hasPermission("backpack.admin")){
                            if (args[0].equalsIgnoreCase("give")) {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    String playerArg = args[1];
                                    if (p.getName().equals(playerArg)){
                                        if (p.getInventory().firstEmpty() != -1) {
                                            int randomNumber = ThreadLocalRandom.current().nextInt(1, 65536);
                                            while (getConfig().getList(String.valueOf(randomNumber)) != null) {
                                                randomNumber = ThreadLocalRandom.current().nextInt(1, 65536);
                                                if (getConfig().getString(String.valueOf(randomNumber)) == null) {
                                                    break;
                                                }
                                            }
                                            ItemStack backpack = new ItemStack(Material.CHEST, 1);
                                            ItemMeta meta = backpack.getItemMeta();
                                            List<String> lores = new ArrayList<String>();
                                            if (meta.getLore() != null) lores = meta.getLore();
                                            lores.add(ChatColor.translateAlternateColorCodes('&', "&" + Integer.toHexString(ThreadLocalRandom.current().nextInt(1, 16)) + "BackPack &r&6" + randomNumber));
                                            lores.add(ChatColor.translateAlternateColorCodes('&', "&" + Integer.toHexString(ThreadLocalRandom.current().nextInt(1, 16)) + "Owner &r&3" + sender.getName()));
                                            meta.setLore(lores);
                                            backpack.setItemMeta(meta);
                                            getConfig().set("data." + args[1] + ".backpack." + randomNumber, "");
                                            p.getInventory().addItem(backpack);
                                            p.sendMessage(ChatColor.DARK_AQUA + "You have been given a BackPack");
                                            sender.sendMessage(ChatColor.DARK_GREEN + "Given BackPack to player succesfully");
                                            lores.clear();
                                        } else {
                                            sender.sendMessage(ChatColor.DARK_RED + "Player's inventory is full");
                                        }
                                    }

                                }
                            } else if (args[0].equalsIgnoreCase("open")) {
                                if (getConfig().getConfigurationSection("data.") != null) {
                                    if (getConfig().getConfigurationSection(" " + args[1]) != null) {
                                        openPack.put(sender.getName(), " " + args[1]);
                                        MakeGui(((Player) sender).getPlayer());
                                        sender.sendMessage(ChatColor.DARK_GREEN + "BackPack opened");
                                    } else {
                                        sender.sendMessage(ChatColor.DARK_RED + "BackPack with this ID doesn't exist, or it's empty");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.DARK_RED + "No BackPacks have been created");
                                }
                            } else if (args[0].equalsIgnoreCase("removecontents")) {
                                if (getConfig().getConfigurationSection("data.") != null) {
                                    if (getConfig().getConfigurationSection(" " + args[1]) != null) {
                                        getConfig().set(" " + args[1], null);
                                        sender.sendMessage(ChatColor.DARK_GREEN + "BackPack contents emptied");
                                    } else {
                                        sender.sendMessage(ChatColor.DARK_RED + "BackPack with this ID doesn't exist, or it's empty");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.DARK_RED + "No BackPacks have been created");
                                }
                            } else {
                                sender.sendMessage(ChatColor.DARK_RED + "Command not recognized");
                            }
                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "You don't have sufficient permissions for that");
                        }
                    }
                    } else if (args.length > 2){
                        sender.sendMessage(ChatColor.DARK_RED + "Too many arguments");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "You don't have sufficient permissions for that");
                }
                } else {
                sender.sendMessage(ChatColor.DARK_RED + "Only player can send this command");
            }
            }
        return false;
    }

    //First time I've used TabCompleter, learned through this tutorial: https://www.youtube.com/watch?v=rQce_yEXSOE
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String[] args){
        if (command.getName().equalsIgnoreCase("backpack")){
            if (args.length == 1) {
                    final ArrayList<String> l = new ArrayList<>();

                    final ArrayList<String> commands = new ArrayList<>();
                    if (sender.hasPermission("backpack.make") || sender.hasPermission("backpack.admin")) {
                        commands.add("open");
                    }
                    if (sender.hasPermission("backpack.retrieve") || sender.hasPermission("backpack.admin")){
                        commands.add("retrieve");
                    }
                    if (sender.hasPermission("backpack.admin")) {
                        commands.add("removecontents");
                        commands.add("reload");
                        commands.add("give");
                    }

                    if (sender.hasPermission("backpack.admin")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            commands.add(p.getName());
                        }
                    }

                    StringUtil.copyPartialMatches(args[0], commands, l);

                    return l;
            } else if (args.length == 2){
                final ArrayList<String> arguments = new ArrayList<>();
                if (args[0].equalsIgnoreCase("open")){
                    if (this.getConfig().getConfigurationSection("data.") != null) {
                        final ArrayList<String> sList = new ArrayList<String>();
                        for (final String s : this.getConfig().getConfigurationSection("data.").getKeys(false)) {
                            sList.add(s);
                            final ArrayList<String> allNumbers = new ArrayList<String>();
                            for (int i = sList.size() - 1; i >= 0; --i) {
                                allNumbers.addAll(this.getConfig().getConfigurationSection("data." + sList.get(i) + ".backpack.").getKeys(false));
                                for (int index = allNumbers.size() - 1; index >= 0; --index) {
                                    if (this.getConfig().getConfigurationSection(" " + allNumbers.get(index)) != null) {
                                        arguments.add(allNumbers.get(index));
                                    }
                                }
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removecontents")) {
                    if (this.getConfig().getConfigurationSection("data.") != null) {
                        final ArrayList<String> sList = new ArrayList<String>();
                        for (final String s : this.getConfig().getConfigurationSection("data.").getKeys(false)) {
                            sList.add(s);
                            final ArrayList<String> allNumbers = new ArrayList<String>();
                            for (int i = sList.size() - 1; i >= 0; --i) {
                                allNumbers.addAll(this.getConfig().getConfigurationSection("data." + sList.get(i) + ".backpack.").getKeys(false));
                                for (int index = allNumbers.size() - 1; index >= 0; --index) {
                                    if (this.getConfig().getConfigurationSection(" " + allNumbers.get(index)) != null) {
                                        arguments.add(allNumbers.get(index));
                                    }
                                }
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("give")){
                    for (Player p : Bukkit.getOnlinePlayers()){
                        arguments.add(p.getName());
                    }
                } else if (args[0].equalsIgnoreCase("retrieve")){
                    if (getConfig().getConfigurationSection("data.") != null) {
                        if (getConfig().getConfigurationSection("data.").contains(sender.getName())) {
                            for (String s : getConfig().getConfigurationSection("data." + sender.getName() + ".backpack.").getKeys(false)) {
                                arguments.add(s);
                            }
                        }
                    }
                }

                final ArrayList<String> l = new ArrayList<>();

                StringUtil.copyPartialMatches(args[1], arguments, l);

                return l;
            }
        }
        return null;
    }

}
