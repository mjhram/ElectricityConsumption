package ac.mohammad.com.electricityconsumption;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by mohammad.haider on 2/16/2015.
 */
public class elec_info implements Parcelable {
    //private MainActivity theActivity;
    public Long id;
    public String time;//used for sqliteDB timestamp
    public Long prevDateInMilliSec;
    public Long nextDateInMilliSec;
    public Long prevReading;
    public Long nextReading;
    //public String price;
    public double pricenum;
    public String calculationString;
    public int isItBill;

    final private String TABLE_Units = databaseHandler.TABLE_Units;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeString(time);
        dest.writeLong(prevDateInMilliSec);
        dest.writeLong(nextDateInMilliSec);
        dest.writeLong(prevReading);
        dest.writeLong(nextReading);
        //dest.writeString(price);
        dest.writeDouble(pricenum);
        dest.writeString(calculationString);
        dest.writeInt(isItBill);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        //time = in.readString();
        prevDateInMilliSec=in.readLong();
        nextDateInMilliSec=in.readLong();
        prevReading=in.readLong();
        nextReading=in.readLong();
        //price = in.readString();
        pricenum = in.readDouble();
        calculationString = in.readString();
        isItBill = in.readInt();
    }

    //private Activity theActivity;
    public elec_info() {
    }

    public elec_info(Parcel in){
        //theActivity = activity;
        readFromParcel(in);
    }

    public static final Creator<elec_info> CREATOR = new Creator<elec_info>() {

        @Override
        public elec_info createFromParcel(Parcel source) {
            return new elec_info(source);
        }

        @Override
        public elec_info[] newArray(int size) {
            return new elec_info[size];
        }
    };

    //aFormat example = "EEE MMM d HH:mm:ss zz yyyy"
    /*static public Date stringToDate(String aDate, String aFormat) {
        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat(aFormat);
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;
    }*/

    static public elec_info getInfoFromRow(Cursor in) {
        elec_info tmpMobInfo = new elec_info();

        tmpMobInfo.id=in.getLong(in.getColumnIndexOrThrow("No"));
        tmpMobInfo.time=in.getString(in.getColumnIndexOrThrow("time"));
        tmpMobInfo.prevDateInMilliSec=in.getLong(in.getColumnIndexOrThrow("prevDateInMilliSec"));
        tmpMobInfo.nextDateInMilliSec=in.getLong(in.getColumnIndexOrThrow("nextDateInMilliSec"));
        tmpMobInfo.prevReading=in.getLong(in.getColumnIndexOrThrow("prevReading"));
        tmpMobInfo.nextReading=in.getLong(in.getColumnIndexOrThrow("nextReading"));
        //tmpMobInfo.price=in.getString(in.getColumnIndexOrThrow("price"));
        tmpMobInfo.pricenum=in.getDouble(in.getColumnIndexOrThrow("pricenum"));
        tmpMobInfo.calculationString=in.getString(in.getColumnIndexOrThrow("calcStr"));
        tmpMobInfo.isItBill=in.getInt(in.getColumnIndexOrThrow("isItBill"));

        return tmpMobInfo;
    }

    public void addRecord2db(SQLiteDatabase db)
    {
        // Tag used to cancel the request
        final String tag_string = "addRecord";
        ContentValues params = new ContentValues();
        String tmp = prevDateInMilliSec.toString();
        params.put("prevDateInMilliSec", tmp);
        tmp = nextDateInMilliSec.toString();
        params.put("nextDateInMilliSec", tmp);
        tmp = prevReading.toString();
        params.put("prevReading", tmp);
        tmp = nextReading.toString();
        params.put("nextReading", tmp);
        //params.put("price", price);
        params.put("pricenum", pricenum);
        params.put("calcStr", calculationString);
        params.put("isItBill", isItBill);
        long tmpL =  db.insert(TABLE_Units, null, params);
        Log.d("Test", Long.toString(tmpL));
    }
}
