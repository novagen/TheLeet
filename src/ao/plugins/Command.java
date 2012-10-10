package ao.plugins;

import ao.plugins.Reply.Channel;

public class Command {

    private Channel channel;
    private byte[] channelid;
    private int characterid;
    private int permission;
    private String name;
    private String[] args;
    
    public Command(Channel channel, byte[] channelid, int characterid, int permission, String name, String[] args) {
        this.channel = channel;
        this.channelid = channelid;
        this.characterid = characterid;
        this.permission = permission;
        this.name = name.toLowerCase();
        this.args = args;
    }
    
    public Channel getChannel(){
        return channel;
    }

    public String[] getArgs() {
        return args;
    }

    public byte[] getChannelid() {
        return channelid;
    }

    public int getCharacterid() {
        return characterid;
    }
    
    public int getPermission() {
        return permission;
    }

    public String getName() {
        return name;
    }
}
