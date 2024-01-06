package oversecured.ovaa.network;

import oversecured.ovaa.objects.LoginData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;
/* loaded from: classes.dex */
public interface LoginService {
    @POST
    Call<Void> login(@Url String str, @Body LoginData loginData);
}
