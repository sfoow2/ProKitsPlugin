package me.sfoow.prokits.Data;

import me.sfoow.prokits.Ect.VaultUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import static me.sfoow.prokits.Ect.utils.Qwerty;
import static me.sfoow.prokits.Ect.utils.SendYes;

@Command("eco")
@CommandPermission("op")
public class Economy {

    // /eco add <player> <amount>
    @Subcommand("add")
    public void addMoney(CommandSender sender, Player target, int amount) {
        // Add tokens

        VaultUtils.addMoney(target,amount);

        if (sender instanceof Player){
        // Send confirmation
        SendYes((Player) sender);
        Qwerty((Player) sender, "&aAdded " + amount + " to " + target.getName() + "!");
    }}

    // /eco set <player> <amount>
    @Subcommand("set")
    public void setMoney(CommandSender sender, Player target, int amount) {
        // Set tokens
        VaultUtils.setBalance(target,amount);

        // Send confirmation
        if (sender instanceof Player){
        SendYes((Player) sender);
        Qwerty((Player) sender, "&aSet " + amount + " to " + target.getName() + "!");
    }}
}

