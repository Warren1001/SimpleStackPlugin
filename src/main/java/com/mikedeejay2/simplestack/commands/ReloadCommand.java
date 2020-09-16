package com.mikedeejay2.simplestack.commands;

import com.mikedeejay2.mikedeejay2lib.commands.AbstractSubCommand;
import com.mikedeejay2.mikedeejay2lib.util.chat.Chat;
import com.mikedeejay2.simplestack.Simplestack;
import com.mikedeejay2.simplestack.config.Config;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends AbstractSubCommand<Simplestack>
{
    public ReloadCommand(Simplestack plugin)
    {
        super(plugin);
    }

    /**
     * The reload command reloads the block list in the config class based on the current
     * config file. If the server was opened and then the config file was modified,
     * /simplestack reload could be run to reload the config in the server and make the
     * plugin function with the modified config.
     *
     * @param sender The CommandSender that sent the command
     * @param args The arguments for the command (subcommands)
     */
    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        Config config = plugin.config();
        config.reload();
        plugin.chat().sendMessage(sender, "&e&l" + plugin.langManager().getTextLib(sender, "generic.success") + "&r &9" + plugin.langManager().getText(sender, "simplestack.reload.success"));
        if(!(sender instanceof Player)) return;
        Player player = (Player) sender;
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
    }

    @Override
    public String name()
    {
        return "reload";
    }

    @Override
    public String info(CommandSender sender)
    {
        return plugin.langManager().getText(sender, "simplestack.commands.reload.info");
    }

    @Override
    public String[] aliases()
    {
        return new String[]{"rl"};
    }

    @Override
    public String permission()
    {
        return "simplestack.reload";
    }
}
