package org.sana.android.net.qos;

import android.os.Parcel;
import android.os.Parcelable;

import org.sana.net.qos.QOS;

import java.util.Date;

/**
 * Parcelable representation of QOS object.
 */
public class QOSParcel extends QOS implements Parcelable {

    public QOSParcel(){
        super();
    }

    public QOSParcel(Parcel in){
        source = in.readString();
        target = in.readString();
        sent = new Date(in.readLong());
        received = new Date(in.readLong());
        sendCount = in.readInt();
        eventStart = new Date(in.readLong());
        eventComplete = new Date(in.readLong());
        requestComplete = new Date(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(source);
        parcel.writeString(target);
        parcel.writeLong(sent.getTime());
        parcel.writeLong(received.getTime());
        parcel.writeInt(sendCount);
        parcel.writeLong(eventStart.getTime());
        parcel.writeLong(eventComplete.getTime());
        parcel.writeLong(requestComplete.getTime());
    }

    public static Parcelable.Creator<QOSParcel> CREATOR = new Parcelable
            .Creator<QOSParcel>(){

        @Override
        public QOSParcel createFromParcel(Parcel parcel) {
            return new QOSParcel(parcel);
        }

        @Override
        public QOSParcel[] newArray(int i) {
            return new QOSParcel[i];
        }
    };
}
