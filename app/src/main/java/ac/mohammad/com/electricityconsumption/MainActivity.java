package ac.mohammad.com.electricityconsumption;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    static TextView selectedDateView, prevDateTextView, nextDateTextView,
            priceTextView, calcTextView, textViewUnits, textViewPeriod;
    static long prevDate, nextDate;
    EditText prevReadTextEdit, nextReadTextEdit;
    int unitsPerMonthX2016 = 1000;
    int unitsPerMonth = 500;
    int price4units[] = {10, 10, 20, 40, 80, 80, 120, 120, 200};
    SharedPreferences sharedPref;
    databaseHandler dbHandler;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_history:
                onHistoryClicked(null);
                return true;
            case R.id.menu_export:
                exportDB();
                return true;
            case R.id.menu_import:
                importDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        dbHandler = new databaseHandler(this);

        prevDateTextView = (TextView) findViewById(R.id.textViewPrevDate);
        nextDateTextView = (TextView) findViewById(R.id.textViewNextDate);
        textViewUnits = (TextView) findViewById(R.id.textViewUnits);
        textViewPeriod = (TextView) findViewById(R.id.textViewPeriod);
        priceTextView = (TextView) findViewById(R.id.textViewPrice);
        calcTextView = (TextView) findViewById(R.id.textViewCalc);
        prevReadTextEdit = (EditText) findViewById(R.id.editTextPrevReading);
        nextReadTextEdit = (EditText) findViewById(R.id.editTextNextReading);
        init();
    }

    void init() {
        final Calendar c = Calendar.getInstance();
        long timeInMilliSec = c.getTimeInMillis();

        prevDate = sharedPref.getLong("PREV_DATE", timeInMilliSec);
        nextDate = sharedPref.getLong("NEXT_DATE", timeInMilliSec);
        showDate(prevDateTextView, prevDate);
        showDate(nextDateTextView, nextDate);

        long tmp = sharedPref.getLong("PREV_READING", 0);
        prevReadTextEdit.setText(""+tmp);
        tmp = sharedPref.getLong("NEXT_READING", 0);
        nextReadTextEdit.setText(""+tmp);
    }

    public void onPrevDateClicked(View v) {
        selectedDateView = prevDateTextView;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void onCalculateClicked(View v) {
        double price = getPrice(nextDate, prevDate);
        priceTextView.setText(String.format("%.0f",price));
        calcTextView.setText(calculationStr);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("PREV_DATE", prevDate);
        editor.putLong("NEXT_DATE", nextDate);
        editor.putLong("PREV_READING", Long.parseLong(prevReadTextEdit.getText().toString()));
        editor.putLong("NEXT_READING", Long.parseLong(nextReadTextEdit.getText().toString()));
        editor.commit();
    }

    public void onSaveClicked (View v) {
        elec_info eInfo = new elec_info();
        eInfo.prevDateInMilliSec = prevDate;
        eInfo.nextDateInMilliSec = nextDate;
        eInfo.prevReading = Long.parseLong(prevReadTextEdit.getText().toString());
        eInfo.nextReading = Long.parseLong(nextReadTextEdit.getText().toString());
        eInfo.price = String.format("%.0f",getPrice(nextDate, prevDate));
        eInfo.calculationString = calculationStr;
        dbHandler.addRecord(eInfo);
    }

    public void onHistoryClicked(View aa) {
        if(dbHandler.getRecordsCount() !=0) {
            //local history instead of site history
            Intent myIntent = new Intent(MainActivity.this, InfoListActivity.class);
            MainActivity.this.startActivity(myIntent);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("History")
                    .setMessage("There are no History records.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    static long get2016Jan1inMSec() {
        final Calendar c = Calendar.getInstance();
        c.set(2016, 0, 1);
        long msec = c.getTimeInMillis();
        return msec;
    }

    String calculationStr = "";
    private double getPrice(long nDate, long pDate) {
        long periodInDays= (nDate -pDate)/(24*60*60*1000);
        int     prevReading = Integer.parseInt(prevReadTextEdit.getText().toString());
        int     nextReading = Integer.parseInt(nextReadTextEdit.getText().toString());
        int readingDiff = nextReading - prevReading;
        long unitsPerPeriod;

        textViewUnits.setText(String.format("%d",readingDiff));
        textViewPeriod.setText(String.format("%d",periodInDays));

        if(nDate <= get2016Jan1inMSec()) {//nDate&pDate are before 2016-Jan-1
            unitsPerPeriod = Math.round(1.0 * unitsPerMonthX2016 * periodInDays / 30.0);
        } else if(pDate < get2016Jan1inMSec()){
            unitsPerPeriod = Math.round(1.0 * unitsPerMonthX2016 * periodInDays / 30.0);
        } else { //nDate&pDate are after 2016-Jan-1
            unitsPerPeriod = Math.round(1.0 * unitsPerMonth * periodInDays / 30.0);
        }
        double price = 0.0;
        int i = 0;
        calculationStr = "";
        while (readingDiff > 0) {
            if(i>0) {
                calculationStr += " + ";
            }
            double totalUnits = unitsPerPeriod;
            //int totalPrice4Units=price4units[i];
            while(i<price4units.length-1 && price4units[i]==price4units[i+1]){
                totalUnits += unitsPerPeriod;
                //totalPrice4Units+=price4units[i];
                i++;
            }
            if (readingDiff >= totalUnits) {
                price += totalUnits * price4units[i];
                readingDiff -= totalUnits;
                calculationStr += totalUnits + "x" + price4units[i];
            } else {
                price += readingDiff * price4units[i];
                calculationStr += readingDiff + "x" + price4units[i];
                break;
            }
            i++;
            if(i == price4units.length-1 && readingDiff > 0) {
                price += readingDiff * price4units[i];
                calculationStr += " + " + readingDiff + "x" + price4units[i];
                break;
            }
        }
        return price;
    }

    public void onMoveUpClicked(View v) {
        prevReadTextEdit.setText(nextReadTextEdit.getText().toString());
        prevDate = nextDate;
        showDate(prevDateTextView, prevDate);

    }

    public void onNextDateClicked(View v) {
        selectedDateView = nextDateTextView;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int y, int m, int d) {
            // Do something with the time chosen by the user
            final Calendar c = Calendar.getInstance();
            c.set(y, m, d);
            long msec = c.getTimeInMillis();
            if(selectedDateView == prevDateTextView) {
                prevDate = msec;
            } else {
                nextDate = msec;
            }
            showDate(selectedDateView, msec);
        }
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            final Calendar c = Calendar.getInstance();
            c.set(arg1, arg2, arg3);
            long msec = c.getTimeInMillis();
            showDate(selectedDateView, msec);
        }
    };
    static void showDate(TextView dateView, long msec) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(msec);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        month++;
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
    public void onImportClicked(View v) {
        importDB();
    }

    private void importDB(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String tmp = this.getPackageName();
        String currentDBPath = "/data/"+ tmp +"/databases/"+databaseHandler.DATABASE_NAME;
        String backupDBPath = databaseHandler.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(backupDB).getChannel();
            destination = new FileOutputStream(currentDB).getChannel();
            long s = source.size();
            destination.transferFrom(source, 0, s);
            source.close();
            destination.close();
            Toast.makeText(this, "DB Imported!", Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void onExportClicked(View v) {
        exportDB();
    }

    private void exportDB(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String tmp = this.getPackageName();
        String currentDBPath = "/data/"+ tmp +"/databases/"+databaseHandler.DATABASE_NAME;
        String backupDBPath = databaseHandler.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "DB Exported!"+backupDB.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
