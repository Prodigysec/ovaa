package oversecured.ovaa.objects;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/* loaded from: classes.dex */
public class MemoryCorruptionParcelable implements Parcelable {
    public static final Parcelable.Creator<MemoryCorruptionParcelable> CREATOR = new Parcelable.Creator<MemoryCorruptionParcelable>() { // from class: oversecured.ovaa.objects.MemoryCorruptionParcelable.1
        @Override // android.os.Parcelable.Creator
        public MemoryCorruptionParcelable[] newArray(int i) {
            return new MemoryCorruptionParcelable[i];
        }

        @Override // android.os.Parcelable.Creator
        public MemoryCorruptionParcelable createFromParcel(Parcel parcel) {
            return new MemoryCorruptionParcelable(parcel);
        }
    };
    private static final Gson GSON = new GsonBuilder().create();
    public Object data;

    private MemoryCorruptionParcelable(Parcel parcel) {
        try {
            Class clazz = Class.forName(parcel.readString());
            this.data = GSON.fromJson(parcel.readString(), (Class<Object>) clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.data.getClass().getCanonicalName());
        parcel.writeString(GSON.toJson(this.data));
    }
}