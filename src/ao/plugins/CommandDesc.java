package ao.plugins;

public class CommandDesc {

    private String name;
    private String description;
    private String prefix;
    private String[] usages;
    private int permissions;

    public CommandDesc(String name, String description, String prefix, String[] usages, int permissions) {
        this.name = name;
        this.description = description;
        this.prefix = prefix;
        this.usages = usages;
        this.permissions = permissions;
    }

    public String getDescription() {
        String desc = description;
        if (usages != null && usages.length > 0) {
            desc += "<br/>\n";
            for (String s : usages) {
                desc += "<br/>\n" + prefix + s;
            }
        }
        return desc;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }
}
