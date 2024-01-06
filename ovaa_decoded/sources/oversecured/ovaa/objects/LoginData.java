package oversecured.ovaa.objects;

import java.io.Serializable;
/* loaded from: classes.dex */
public class LoginData implements Serializable {
    public String email;
    public String password;

    public LoginData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String toString() {
        return this.email + ":" + this.password;
    }
}
