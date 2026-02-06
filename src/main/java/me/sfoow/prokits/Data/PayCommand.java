package me.sfoow.prokits.Data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.Suggest;
import revxrsal.commands.annotation.SuggestWith;

import java.util.Collection;
import java.util.UUID;

import static me.sfoow.prokits.Ect.VaultUtils.*;
import static me.sfoow.prokits.Ect.utils.Qwerty;
import static me.sfoow.prokits.Ect.utils.SendNo;
import static me.sfoow.prokits.settings.PaymentsSettings;

public class PayCommand {

    private static Collection<? extends Player> getAllPlayers(){
        return Bukkit.getOnlinePlayers();
    }

    @Command("pay")
    @Cooldown(1)
    public void PayCommand(Player sender,Player target, int amount) {

        if (amount <= 0) {
            Qwerty(sender,"&cYou must pay a positive amount!");
            SendNo(sender);
            return;
        }

        if (sender.getUniqueId().equals(target.getUniqueId())) {
            Qwerty(sender,"&cYou cannot pay yourself!");
            SendNo(sender);
            return;
        }

        if (PaymentsSettings.getOrDefault(target.getUniqueId(),false)){
            Qwerty(sender,"&cThis player has payments blocked!");
            SendNo(sender);
            return;
        }

        int senderBalance = (int) getBalance(sender);

        if (senderBalance < amount) {
            Qwerty(sender,"&cYou do not have enough tokens to pay " + amount + "!");
            Qwerty(sender,"&cYou cannot pay yourself!");
            return;
        }

        // Safe token transfer
        setBalance(sender,senderBalance - amount);
        addMoney(target,amount);

        Qwerty(sender,"&aYou have paid §e" + target.getName() + " §a" + amount + " tokens.");
        Qwerty(target,"&aYou have received §e" + amount + " tokens §afrom §e" + sender.getName() + "§a.");
    }
}
