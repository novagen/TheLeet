package ao.misc;

public class AOML {
    public static String Blob(String name, String content){
        String message = "<a href=\"text://";
        message += content.replaceAll("\"", "'");
        message += "\">"+name+"</a>";
        return message;
    }

    public static String Chatcmd(String name, String content){
        String message = "<a href='chatcmd://";
        content = content.replaceAll("<", "&lt;");
        content = content.replaceAll(">", "&gt;");
        message += content.replaceAll("\'", "&#39;");
        message += "\'>"+name+"</a>";
        return message;
    }
    
    /*
     * Cleans a string with html encoding characters
     */
    public static String cleanString(String s){
        return s.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
