package com.cadiducho.cservidoresmc.bukkit;

import com.cadiducho.cservidoresmc.bukkit.cmd.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Framework de comandos de Meriland.es
 * @author Cadiducho
 */
public class CommandManager implements TabCompleter {

    private static final List<CommandBase> cmds = new ArrayList<>();
    public static BukkitPlugin plugin = BukkitPlugin.get();

    public static void load() {
        cmds.add(new ReloadCMD());
        cmds.add(new StatsCMD());
        cmds.add(new TestCMD());
        cmds.add(new UpdateCMD());
        cmds.add(new VoteCMD());

        CommandManager managerCmds = new CommandManager();
        
        for (CommandBase cmd : cmds) {
            if (Bukkit.getPluginCommand("40ServidoresMC:" + cmd.getName()) == null) {
                BukkitPlugin.get().log(Level.WARNING, "Error al cargar el comando: " + cmd.getName());
                continue;
            }
            Bukkit.getPluginCommand("40ServidoresMC:" + cmd.getName()).setTabCompleter(managerCmds);
        }
    }

    public static void onCmd(final CommandSender sender, Command cmd, String label, final String[] args) {
        if (label.startsWith(("40ServidoresMC:").toLowerCase())) {
            label = label.substring(("40ServidoresMC:").length());
        }
        for (CommandBase cmdr : cmds) {
            if (label.equals(cmdr.getName()) || cmdr.getAliases().contains(label)) {
                if (sender instanceof ConsoleCommandSender) {
                    ConsoleCommandSender cs = (ConsoleCommandSender) sender;
                    cmdr.run(cs, label, args);
                    break;
                }
                cmdr.run(sender, label, args);
                break;
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> rtrn = null;
        if (label.startsWith("40ServidoresMC:")) {
            label = label.replaceFirst("40ServidoresMC:", "");
        }
        /*
        * Auto Complete normal para cada comando si está declarado
        */
        for (CommandBase cmdr : cmds) {
            if (cmdr.getName().equals(label) || cmdr.getAliases().contains(label)) {
                try {
                    if (!sender.hasPermission(cmdr.getPermission())) {
                        return new ArrayList<>();
                    }
                    rtrn = cmdr.onTabComplete(sender, cmd, label, args, args[args.length - 1], args.length - 1);
                } catch (Exception ex) {
                    BukkitPlugin.get().log("Fallo al autocompletar " + label);
                }
                break;
            }
        }

        // Si el autocomplete es null, que devuelva jugadores
        if (rtrn == null) {
            rtrn = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                rtrn.add(p.getName());
            }
        }

        // Autocomplete para cada argumento
        if (!(args[args.length - 1].isEmpty() || args[args.length - 1] == null)) {
            List<String> remv = new ArrayList<>();
            for (String s : rtrn) {
                if (!StringUtils.startsWithIgnoreCase(s, args[args.length - 1])) {
                    remv.add(s);
                }
            }
            rtrn.removeAll(remv);
        }
        return rtrn;
    }
}
