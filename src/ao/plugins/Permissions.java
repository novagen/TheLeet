package ao.plugins;

public class Permissions {
    
    public static final int SYSTEM = 6;
    public static final int SUPER_ADMIN = 5;
    public static final int ADMIN = 4;
    public static final int MODERATOR = 3;
    public static final int MEMBER = 2;
    public static final int ALL = 1;
    public static final int BANNED = 0;
    
    public static int parseString(String string){
        String s = string.toUpperCase();
        if (s.compareTo("ALL") == 0 || s.compareTo("UNKNOWN") == 0) {
            return ALL;
        } else if (s.compareTo("MEMBER") == 0 || s.compareTo("GUEST") == 0){
            return MEMBER;
        } else if (s.compareTo("MOD") == 0 || s.compareTo("MODERATOR") == 0){
            return MODERATOR;
        } else if (s.compareTo("ADMIN") == 0){
            return ADMIN;
        } else if(s.compareTo("SUPER_ADMIN") == 0 || s.compareTo("SUPER ADMIN") == 0 || s.compareTo("SUPERADMIN") == 0 ||s.compareTo("SADMIN") == 0){
            return SUPER_ADMIN;
        } else if (s.compareTo("BANNED") == 0){
            return BANNED;
        }
        return -1;
    }
    
    public static String toString(int i){
        switch (i) {
            case ALL:  return "All";
            case MEMBER: return "Member";
            case MODERATOR: return "Moderator";
            case ADMIN: return "Admin";
            case SUPER_ADMIN: return "Super Admin";
            case BANNED: return "Banned";
        }
        return "Error";
    }
}