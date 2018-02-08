package ac.mohammad.com.electricityconsumption;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohammad.haider on 5/16/2016.
 */
public class databaseHandler extends SQLiteOpenHelper {
        // All Static variables
        // Database
        private static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "elecReadings.db";
        private static final String KEY_ID = "No";

        // Contacts table name
        public static final String TABLE_Units = "elecUnits";

        public databaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {
             {
                String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_Units + "("
                        + "No INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,  time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + " prevDateInMilliSec bigint  DEFAULT 0,"
                        + " nextDateInMilliSec bigint  DEFAULT 0,"
                        + " prevReading int(11)  DEFAULT 0,"
                        + " nextReading int(11)  DEFAULT 0,"
                        + " price varchar(15)  DEFAULT 0,"
                        + " calcStr varchar(100)  DEFAULT NULL"
                        + ")";
                 Log.d("Test", CREATE_TABLE);
                db.execSQL(CREATE_TABLE);
            }
        }

        // Upgrading database
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_Units);

            // Create tables again
            onCreate(db);
        }

        /**
         * All CRUD(Create, Read, Update, Delete) Operations
         */

        // Adding new record
        void addRecord(elec_info mobInfo) {
            SQLiteDatabase db = this.getWritableDatabase();
            mobInfo.addRecord2db(db);
            db.close();
        }

    // Deleting single Record
    public void delRecord(elec_info eInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        //String queryStr = "DELETE FROM " + TABLE_Units + " WHERE No = '"+eInfo.id+"'";
        //db.rawQuery(queryStr, null);
        //c.moveToFirst();
        //db.execSQL(queryStr);
        String tmp = "No = "+eInfo.id;
        int a = db.delete(TABLE_Units, tmp, null);
        db.close();
        /*int b = a;

        int tmp = db.delete(TABLE_Units, KEY_ID + " = ?",
                new String[] { String.valueOf(eInfo.id) });
        db.close();
        tmp +=1;
        int a = tmp;
        */
    }

        // Getting All records
        public List<elec_info> getAllRecords(SortType sortType) {
            List<elec_info> cInfoList = new ArrayList<elec_info>();
            // Select All Query
            String orderStr ="";
            if(sortType == SortType.PrevDate) {
                orderStr = " ORDER BY prevDateInMilliSec DESC";
            } else if(sortType == SortType.SaveDate) {
                orderStr = " ORDER BY time DESC";
            } else {
                orderStr = " ORDER BY No DESC";
            }
            String selectQuery = "SELECT  * FROM " + TABLE_Units + orderStr;
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    elec_info tmpMobInfo = elec_info.getInfoFromRow(cursor);
                    // Adding cInfo to list
                    cInfoList.add(tmpMobInfo);
                } while (cursor.moveToNext());
            }
            // return 3gTests list
            return cInfoList;
        }

        // Getting contacts Count
        public int getRecordsCount() {
            String countQuery = "SELECT  * FROM " + TABLE_Units;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);
            int count = cursor.getCount();
            cursor.close();
            return count;
        }
}
