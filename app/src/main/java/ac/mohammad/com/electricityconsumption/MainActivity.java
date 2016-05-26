package ac.mohammad.com.electricityconsumption;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    static TextView selectedDateView, prevDateTextView, nextDateTextView,
            priceTextView, calcTextView;
    static long prevDate, nextDate;
    EditText prevReadTextEdit, nextReadTextEdit;
    int unitsPerMonth = 500;
    int price4units[] = {10, 20, 40, 80};
    SharedPreferences sharedPref;
    databaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        dbHandler = new databaseHandler(this);

        prevDateTextView = (TextView) findViewById(R.id.textViewPrevDate);
        nextDateTextView = (TextView) findViewById(R.id.textViewNextDate);
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
        double price = getPrice();
        priceTextView.setText(String.valueOf(price));
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
        eInfo.price = String.format("%.2f",getPrice());
        eInfo.calculationString = calculationStr;
        dbHandler.addRecord(eInfo);
    }

    public void onHistoryClicked(View v) {
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

    String calculationStr = "";
    private double getPrice() {
        long periodInDays= (nextDate -prevDate)/(24*60*60*1000)+1;
        int     prevReading = Integer.parseInt(prevReadTextEdit.getText().toString());
        int     nextReading = Integer.parseInt(nextReadTextEdit.getText().toString());
        int readingDiff = nextReading - prevReading;
        long unitsPerPeriod = Math.round(1.0 * unitsPerMonth * periodInDays / 30.0);
        double price = 0.0;
        int i = 0;
        calculationStr = "";
        while (readingDiff > 0) {
            if(i>0) {
                calculationStr += " + ";
            }
            if (readingDiff >= unitsPerPeriod) {
                price += unitsPerPeriod * price4units[i];
                readingDiff -= unitsPerPeriod;
                calculationStr += unitsPerPeriod + "x" + price4units[i];
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
}
