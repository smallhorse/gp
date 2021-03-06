package com.ubtechinc.goldenpig.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

public class AddressBookmodel implements Parcelable, MultiItemEntity {
    public String name;
    public String phone;
    public long id;
    /**
     * 0为默认数据，1为尾部，2为头部
     */
    public int type = 0;
    public Boolean noCard = false;
    public Boolean selectAll = false;
    public Boolean select = false;
    public String sortLetter;
    public String pinyin;

    public AddressBookmodel() {
    }


    protected AddressBookmodel(Parcel in) {
        pinyin = in.readString();
        sortLetter = in.readString();
        name = in.readString();
        phone = in.readString();
        id = in.readLong();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pinyin);
        dest.writeString(sortLetter);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeLong(id);
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AddressBookmodel> CREATOR = new Creator<AddressBookmodel>() {
        @Override
        public AddressBookmodel createFromParcel(Parcel in) {
            return new AddressBookmodel(in);
        }

        @Override
        public AddressBookmodel[] newArray(int size) {
            return new AddressBookmodel[size];
        }
    };

    @Override
    public int getItemType() {
        return type;
    }
}
