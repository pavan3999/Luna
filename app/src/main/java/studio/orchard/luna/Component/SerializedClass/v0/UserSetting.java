package studio.orchard.luna.Component.SerializedClass.v0;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UserSetting implements Serializable {
    private static final long serialVersionUID = 6852122942242548176L;
    public boolean enable = true;
    public boolean showInfo = true;
    public String userName = "";
    public String userEmail = "";
    public String background = "WHITE";
    public float fontSize = 21;
    public Map<String, String> cookies;
    public List<String> blackList;
    public UserSetting(){
        cookies = new HashMap<>();
        blackList = new ArrayList<>();
    }
}
