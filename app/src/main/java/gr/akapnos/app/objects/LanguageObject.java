package gr.akapnos.app.objects;
import java.io.Serializable;
import gr.akapnos.app.Helper;
public class LanguageObject implements Serializable{
    private String lang_code;
    private String icon_large;
    public LanguageObject() {}
    public String getLang_code() {
        return Helper.safeString(lang_code);
    }
    public void setLang_code(String lang_code) {
        this.lang_code = lang_code;
    }
    public String getIcon_large() {
        return Helper.safeString(icon_large);
    }
    public void setIcon_large(String icon_large) {
        this.icon_large = icon_large;
    }
}
