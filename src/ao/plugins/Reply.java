package ao.plugins;

import ao.misc.Convert;
import ao.protocol.packets.Packet.Direction;
import ao.protocol.packets.bi.*;

public class Reply {
    
    public enum Channel {
        TELL, PRIVATE, GROUP, CON;
    }
    
    public static void send(PluginUser user, Channel channel, byte[] channelid, int id, String msg) {
        send("System", user, channel, channelid, id, msg);
    }
    
    public static void send(String name, PluginUser user, Channel channel, byte[] channelid, int id, String msg) {
        try {
            switch (channel) {
                case TELL:
                    PrivateMessagePacket tellPacket = new PrivateMessagePacket(id, msg, Direction.TO_SERVER);
                    user.sendPacket(tellPacket);
                    break;
                case PRIVATE:
                    PrivateChannelMessagePacket pcPacket = new PrivateChannelMessagePacket(Convert.byteToInt(channelid), msg);
                    user.sendPacket(pcPacket);
                    break;
                case GROUP:
                    ChannelMessagePacket cPacket = new ChannelMessagePacket(channelid, msg);
                    user.sendPacket(cPacket);
                    break;
                case CON:
                    user.print(name, msg);
                    break;
                default:
                    throw new Exception("Channel reply error");
            }
        } catch (Exception e) {
            user.exception(name, e);
        }
    }
}
