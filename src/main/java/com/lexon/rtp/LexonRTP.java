package com.lexon.rtp;
import com.lexon.rtp.command.RtpCommand;
import com.lexon.rtp.command.RtpQueueCommand;
import com.lexon.rtp.config.ConfigManager;
import com.lexon.rtp.config.MessageManager;
import com.lexon.rtp.listener.CrossServerListener;
import com.lexon.rtp.listener.MenuListener;
import com.lexon.rtp.location.LocationFinder;
import com.lexon.rtp.queue.QueueManager;
import com.lexon.rtp.redis.RedisManager;
import com.lexon.rtp.util.Scheduler;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
public final class LexonRTP extends JavaPlugin {
    private Scheduler scheduler;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private RedisManager redisManager;
    private LocationFinder locationFinder;
    private QueueManager queueManager;
    private RtpService rtpService;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.scheduler = new Scheduler(this);
        this.configManager = new ConfigManager(this);
        this.configManager.load();
        this.messageManager = new MessageManager(this);
        this.messageManager.load();
        this.redisManager = new RedisManager(this);
        this.redisManager.connect();
        this.locationFinder = new LocationFinder(this);
        this.queueManager = new QueueManager(this);
        this.rtpService = new RtpService(this);
        if (configManager.isQueueEnabled()) {
            this.queueManager.start();
        }
        register("rtp", new RtpCommand(this));
        register("rtpqueue", new RtpQueueCommand(this));
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new CrossServerListener(this), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getLogger().info("LexonRTP enabled. Scheduler: " + scheduler.name()
                + " | Storage: " + redisManager.statusText().replaceAll("&.", ""));
    }
    @Override
    public void onDisable() {
        if (queueManager != null) {
            queueManager.stop();
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (redisManager != null) {
            redisManager.close();
        }
        getLogger().info("LexonRTP disabled.");
    }
    private void register(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("Command not defined in plugin.yml: " + name);
        }
    }
    public Scheduler scheduler() {
        return scheduler;
    }
    public ConfigManager config() {
        return configManager;
    }
    public MessageManager messages() {
        return messageManager;
    }
    public RedisManager redis() {
        return redisManager;
    }
    public LocationFinder locationFinder() {
        return locationFinder;
    }
    public QueueManager queue() {
        return queueManager;
    }
    public RtpService rtpService() {
        return rtpService;
    }
}
