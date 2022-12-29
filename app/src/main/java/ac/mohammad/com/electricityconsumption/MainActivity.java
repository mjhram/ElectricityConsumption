package ac.mohammad.com.electricityconsumption;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

//import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    ArrayList<PriceModel> priceModels = new ArrayList<>();

    public class PriceModelDateComparator implements Comparator<PriceModel>
    {
        public int compare(PriceModel left, PriceModel right) {
            int res = (int)(right.appliedDate - left.appliedDate);
            return res;
        }
    }
    long getUnitsPerPeriod(long unitsInMonth, double periodInDays) {
        long res = Math.round(1.0 * unitsInMonth * periodInDays / 30.0);
        return res;
    }

    class PriceModel {
        String description;
        boolean applied; //used to indicate whether to include it in calc.
        long appliedDate;
        long pDate, nDate;  //prev & next dates
        int prevReading, nextReading;
        ArrayList<Integer> unitPerMonth;
        ArrayList<Integer> pricePerUnit;

        String calculationString;
        double price;

        PriceModel(String desc, long date, int[] units, int[] prices){
            int arraySize = units.length;
            if(units.length != prices.length) {
                //should print error
            }
            description = desc;
            appliedDate = date;
            unitPerMonth = new ArrayList<Integer>();
            pricePerUnit = new ArrayList<Integer>();
            for (int i =0; i<arraySize; i++) {
                unitPerMonth.add(units[i]);
                pricePerUnit.add(prices[i]);
            }
        }
        //String calculationStr
        //nDate&pDate are in milliSec
        public void getPrice(/*long pDate, long nDate, long prevReading, long nextReading*/) {
            double periodInDays= 1.0 * (nDate - pDate)/(24*60*60*1000);
            long readingDiff = nextReading - prevReading;
            long unitsPerPeriod;

            //textViewUnits.setText(String.format("%d",readingDiff));
            //textViewPeriod.setText(String.format("%d",periodInDays));

            /*if(nDate <= get2016Jan1inMSec()) {//nDate&pDate are before 2016-Jan-1
                unitsPerPeriod = Math.round(1.0 * unitsPerMonthX2016 * periodInDays / 30.0);
            } else if(pDate < get2016Jan1inMSec()){
                unitsPerPeriod = Math.round(1.0 * unitsPerMonthX2016 * periodInDays / 30.0);
            } else { //nDate&pDate are after 2016-Jan-1
                unitsPerPeriod = Math.round(1.0 * unitsPerMonth * periodInDays / 30.0);
            }*/
            //double
            price = 0.0;
            //int i = 0;
            calculationString = "";
            for (int i=0; i<unitPerMonth.size() && readingDiff > 0; i++) {
                if(i>0) {
                    calculationString += " + ";
                }
                double totalUnits = getUnitsPerPeriod(unitPerMonth.get(i), periodInDays);//unitsPerPeriod;
                //int totalPrice4Units=price4units[i];
                /*while(i<price4units.length-1 && price4units[i]==price4units[i+1]){
                    totalUnits += unitsPerPeriod;
                    //totalPrice4Units+=price4units[i];
                    i++;
                }*/
                if (readingDiff >= totalUnits) {
                    price += totalUnits * pricePerUnit.get(i);
                    readingDiff -= totalUnits;
                    calculationString += totalUnits + "x" + pricePerUnit.get(i);
                } else {
                    price += readingDiff * pricePerUnit.get(i);
                    calculationString += readingDiff + "x" + pricePerUnit.get(i);
                    break;
                }
                //i++;
                /* //All remaining units falls in last range
                if(i == price4units.length-1 && readingDiff > 0) {
                    price += readingDiff * price4units[i];
                    calculationStr += " + " + readingDiff + "x" + price4units[i];
                    break;
                }*/
            }
            //return price;
        }
    }

    TextView prevDateTextView, nextDateTextView,
            priceTextView, calcTextView, textViewUnits, textViewPeriod;
    Spinner spinneryn;
    CheckBox saveCheckBox;
    //static Button btnCalc;
    long prevDate, nextDate;
    EditText prevReadTextEdit, nextReadTextEdit;
    //int unitsPerMonthX2016 = 1000;
    //int unitsPerMonth = 500;
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
                importDbFromFile();
                return true;
            case R.id.menu_graph:
                showGraph(false);
                return true;
            case R.id.menu_graph_all:
                showGraph(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    DatePickerDialog picker;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        dbHandler = new databaseHandler(this);

        saveCheckBox = (CheckBox) findViewById(R.id.checkBoxSave);
        //btnCalc = (Button) findViewById(R.id.buttonCalculate);
        prevDateTextView = (TextView) findViewById(R.id.textViewPrevDate);
        nextDateTextView = (TextView) findViewById(R.id.textViewNextDate);
        textViewUnits = (TextView) findViewById(R.id.textViewUnits);
        textViewPeriod = (TextView) findViewById(R.id.textViewPeriod);
        priceTextView = (TextView) findViewById(R.id.textViewPrice);
        calcTextView = (TextView) findViewById(R.id.textViewCalc);
        spinneryn = (Spinner) findViewById(R.id.spinneryn);

        prevReadTextEdit = (EditText) findViewById(R.id.editTextPrevReading);
        nextReadTextEdit = (EditText) findViewById(R.id.editTextNextReading);
        init();
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result != null && result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent theIntent = result.getData();
                            if(theIntent != null) {
                                fillActivityFieldsFromList(theIntent);
                            }
                        }
                    }
                });

        prevDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                c.setTimeInMillis(prevDate);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int y, int m, int d) {
                                c.set(y, m, d, 0, 0, 0);
                                c.set(Calendar.MILLISECOND, 0);
                                long msec = c.getTimeInMillis();
                                prevDate = msec;
                                showDate(prevDateTextView, msec);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        nextDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                c.setTimeInMillis(nextDate);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int y, int m, int d) {
                                c.set(y, m, d, 0, 0, 0);
                                c.set(Calendar.MILLISECOND, 0);
                                long msec = c.getTimeInMillis();
                                nextDate = msec;
                                showDate(nextDateTextView, msec);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
    }

    void init() {
        //fill price model
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        //instantiate old Model
        PriceModel pm1;
        int units4Months1[] = {1000, 1000, 1000, Integer.MAX_VALUE};//last units range is limitless
        int price4units1[] =  {10,   20,   40,   80};
        pm1 = new PriceModel("Old Model", 0, units4Months1, price4units1);
        priceModels.add(pm1);

        //Model started on 2016
        c.set(2016, Calendar.JANUARY,1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        long appliedDate = c.getTimeInMillis();
        int units4Months[] = {1000, 500, 500, 1000, 1000, Integer.MAX_VALUE};//last units range is limitless
        int price4units[] =  {10,   20,  40,  80,   120, 200};
        pm1 = new PriceModel("Initial Model", appliedDate, units4Months, price4units);
        priceModels.add(pm1);

        //Model started on 2018
        c.set(2018, Calendar.FEBRUARY,1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        appliedDate = c.getTimeInMillis();
        int units4Months2[] = {1500, 1500, 1000, Integer.MAX_VALUE};//last units range is limitless
        int price4units2[] =  {10,   35,   80,    120};
        pm1 = new PriceModel("2018 Model", appliedDate, units4Months2, price4units2);
        priceModels.add(pm1);
        //sort the priceModels according to applied date
        //Collections.sort(priceModels, new PriceModelDateComparator());
        //load config
        loadSettings();
    }

    void setPriceModelsPeriods(long prevDate, long nextDate, int prevReading, int nextReading){
        int k;
        PriceModel pm1;
        for(k =0; k<priceModels.size()-1; k++) {
            pm1 = priceModels.get(k);
            PriceModel pm2 = priceModels.get(k+1);
            //prevDate must be < nextDate
            if(prevDate >= pm1.appliedDate){
                if(prevDate <pm2.appliedDate) {
                    pm1.pDate = prevDate;
                    if (nextDate < pm2.appliedDate) {
                        pm1.nDate = nextDate;
                    } else {
                        pm1.nDate = pm2.appliedDate /*- 1000 * 3600 * 24*/;
                    }
                    pm1.applied = true;
                } else {
                    pm1.applied = false;
                }
            } else {
                if (nextDate >= pm1.appliedDate) {
                    pm1.pDate = pm1.appliedDate;
                    if(nextDate < pm2.appliedDate) {
                        pm1.nDate = nextDate;
                    } else {
                        pm1.nDate = pm2.appliedDate /*- 1000 * 3600 * 24*/;
                    }
                    pm1.applied = true;
                } else {
                    pm1.applied = false;
                }
            }
        }
        pm1 = priceModels.get(k);
        if(prevDate >= pm1.appliedDate){
            //if(prevDate <pm2.appliedDate)
            {
                pm1.pDate = prevDate;
                pm1.nDate = nextDate;
                pm1.applied = true;
            }
        } else {
            if (nextDate >= pm1.appliedDate) {
                pm1.pDate = pm1.appliedDate;
                pm1.nDate = nextDate;
                pm1.applied = true;
            } else {
                pm1.applied = false;
            }
        }
        //distribute readings between applied models
        int reading =0;
        for(k =0; k<priceModels.size(); k++) {
            PriceModel pm = priceModels.get(k);
            if(!pm.applied) {
                continue;
            }
            long totalPeriod = nextReading - prevReading;
            double delta = 1.0 * totalPeriod * (pm.nDate - pm.pDate+1000*3600*24)/(nextDate - prevDate+1000*3600*24);
            pm.prevReading =  reading;
            pm.nextReading = (int) (1.0 * reading + delta);
            reading = pm.nextReading + 1;
            if(pm.nextReading < pm.prevReading) {
                //pm.nextReading = pm.prevReading;
            }
        }
    }

    public void onCalculateClicked(View v) {
        {
            int prevReading = Integer.parseInt(prevReadTextEdit.getText().toString());

            int nextReading = Integer.parseInt(nextReadTextEdit.getText().toString());
            setPriceModelsPeriods(prevDate, nextDate, prevReading, nextReading);

            //show the whole period and units:
            long periodInDays = (long) (1.0 * (nextDate - prevDate)/(24*60*60*1000));
            long readingDiff = nextReading - prevReading;
            textViewUnits.setText(String.format("%d",readingDiff));
            textViewPeriod.setText(String.format("%d", (int) periodInDays));
        }
        double price = 0;
        String calcString = "";
        for(int i=0; i<priceModels.size(); i++) {
            PriceModel pm = priceModels.get(i);
            if(pm.applied) {
                pm.getPrice();
                price += pm.price;//getPrice(nextDate, prevDate);
                if(!calcString.isEmpty()){
                    calcString +="+";
                }
                calcString += "("+pm.calculationString+")";
                //double periodInDays= 1.0 * (pm.nDate - pm.pDate)/(24*60*60*1000);
                //readingDiff = pm.nextReading - pm.prevReading;
            }
        }

        priceTextView.setText(String.format("%.0f",price));
        calcTextView.setText(calcString);

        storeSettings();
        if(saveCheckBox.isChecked()) {
            saveValues(price, calcString);
        }
    }

    void loadSettings() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long timeInMilliSec = c.getTimeInMillis();

        prevDate = sharedPref.getLong("PREV_DATE", timeInMilliSec);
        nextDate = sharedPref.getLong("NEXT_DATE", timeInMilliSec);
        showDate(prevDateTextView, prevDate);
        showDate(nextDateTextView, nextDate);

        long tmp = sharedPref.getLong("PREV_READING", 0);
        prevReadTextEdit.setText(""+tmp);
        tmp = sharedPref.getLong("NEXT_READING", 0);
        nextReadTextEdit.setText(""+tmp);

        int isitBill = sharedPref.getInt("ISIT_BILL", 1);
        spinneryn.setSelection(isitBill);
        boolean checked = sharedPref.getBoolean("SAVE_VALUES", true);
        saveCheckBox.setChecked(checked);
    }

    void storeSettings() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("PREV_DATE", prevDate);
        editor.putLong("NEXT_DATE", nextDate);
        editor.putLong("PREV_READING", Long.parseLong(prevReadTextEdit.getText().toString()));
        editor.putLong("NEXT_READING", Long.parseLong(nextReadTextEdit.getText().toString()));
        editor.putInt("ISIT_BILL", spinneryn.getSelectedItemPosition());
        editor.putBoolean("SAVE_VALUES", saveCheckBox.isChecked());
        editor.commit();
    }

    void saveValues(double price, String calculationStr){
        elec_info eInfo = new elec_info();
        eInfo.prevDateInMilliSec = prevDate;
        eInfo.nextDateInMilliSec = nextDate;
        eInfo.prevReading = Long.parseLong(prevReadTextEdit.getText().toString());
        eInfo.nextReading = Long.parseLong(nextReadTextEdit.getText().toString());
        eInfo.price = String.format("%.0f",price);
        eInfo.calculationString = calculationStr;
        eInfo.isItBill = spinneryn.getSelectedItemPosition()==0?1:0;
        dbHandler.addRecord(eInfo);
    }

   /* public void onSaveClicked (View v) {
        elec_info eInfo = new elec_info();
        eInfo.prevDateInMilliSec = prevDate;
        eInfo.nextDateInMilliSec = nextDate;
        eInfo.prevReading = Long.parseLong(prevReadTextEdit.getText().toString());
        eInfo.nextReading = Long.parseLong(nextReadTextEdit.getText().toString());
        eInfo.price = String.format("%.0f",getPrice(nextDate, prevDate));
        eInfo.calculationString = calculationStr;
        dbHandler.addRecord(eInfo);
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent theIntent)
    {
        super.onActivityResult(requestCode, resultCode, theIntent);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2 && theIntent != null)
        {
            fillActivityFieldsFromList(theIntent);
            /*elec_info eInfo = theIntent.getParcelableExtra("eInfo");
            prevDate = eInfo.prevDateInMilliSec;
            nextDate = eInfo.nextDateInMilliSec;
            showDate(prevDateTextView, prevDate);
            showDate(nextDateTextView, nextDate);

            prevReadTextEdit.setText(Long.toString(eInfo.prevReading));
            nextReadTextEdit.setText(Long.toString(eInfo.nextReading));
            textViewUnits.setText("---");
            textViewPeriod.setText("---");
            priceTextView.setText("---");
            calcTextView.setText("---");
            spinneryn.setSelection(eInfo.isItBill==1?0:1);
            //btnCalc.performClick();
            //onCalculateClicked(MainActivity.this.getBaseContext());


        }
    }*/

    void fillActivityFieldsFromList(Intent theIntent) {
        elec_info eInfo = theIntent.getParcelableExtra("eInfo");
        prevDate = eInfo.prevDateInMilliSec;
        nextDate = eInfo.nextDateInMilliSec;
        showDate(prevDateTextView, prevDate);
        showDate(nextDateTextView, nextDate);

        prevReadTextEdit.setText(Long.toString(eInfo.prevReading));
        nextReadTextEdit.setText(Long.toString(eInfo.nextReading));
        textViewUnits.setText("---");
        textViewPeriod.setText("---");
        priceTextView.setText("---");
        calcTextView.setText("---");
        spinneryn.setSelection(eInfo.isItBill==1?0:1);
        //btnCalc.performClick();
        //onCalculateClicked(MainActivity.this.getBaseContext());
            /*String message=data.getStringExtra("MESSAGE");
            textView1.setText(message);*/
    }

    public void onHistoryClicked(View aa) {
        if(dbHandler.getRecordsCount("") !=0) {
            //local history instead of site history
            Intent myIntent = new Intent(MainActivity.this, InfoListActivity2.class);
            someActivityResultLauncher.launch(myIntent);
            //MainActivity.this.startActivityForResult(myIntent,2);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("معلومة")
                    .setMessage("لا توجد بيانات سابقة")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    void showGraph(boolean showAllRecords) {
        if((showAllRecords && dbHandler.getRecordsCount("") !=0) ||
                (showAllRecords==false && dbHandler.getRecordsCount("where isItBill=1") !=0)) {
            //local history instead of site history
            Intent myIntent = new Intent(MainActivity.this, GraphActivity.class);
            myIntent.putExtra("isShowAll",showAllRecords);
            MainActivity.this.startActivity(myIntent);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("معلومة")
                    .setMessage("لا توجد بيانات سابقة")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }


    /*static long get2016Jan1inMSec() {
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.set(2016, 0, 1);
        long msec = c.getTimeInMillis();
        return msec;
    }

    //String calculationStr = "";
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
    }*/

    public void onMoveUpClicked(View v) {
        prevReadTextEdit.setText(nextReadTextEdit.getText().toString());
        prevDate = nextDate;
        showDate(prevDateTextView, prevDate);

    }

    /*public void onPrevDateClicked(View v) {
        selectedDateView = prevDateTextView;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
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
            final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            if(selectedDateView == prevDateTextView) {
                c.setTimeInMillis(prevDate);
            } else {
                c.setTimeInMillis(nextDate);
            }
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int y, int m, int d) {
            // Do something with the time chosen by the user
            final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.set(y, m, d, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
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
            final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.set(arg1, arg2, arg3);
            long msec = c.getTimeInMillis();
            showDate(selectedDateView, msec);
        }
    };*/

    void showDate(TextView dateView, long msec) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTimeInMillis(msec);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        month++;
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    private String _path;
    public void importDbFromFile() {
        fn = 1;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
            return;
        }
        new ChooserDialog().with(this)
                .withStartFile(_path)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        importDB(path);
                        //Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        _path = path;

                    }
                })
                .build()
                .show();
    }

    public static String getTime() {
        String timezone="GMT+3";

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        Date date = c.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd");
        String strDate = df.format(date);
        return strDate;
    }

    public static String getFileNameWithoutExtension(String _path, String _pathSeperator, String _extensionSeperator) {
        try {
            int dot = _path.lastIndexOf(_extensionSeperator);
            int sep = _path.lastIndexOf(_pathSeperator);
            return _path.substring(sep + 1, dot);
        } catch (Exception ex) {
            return "Unknown";
        }
    }

    final private int MY_PERMISSIONS_REQUEST = 100;
    private int fn; //1=import, 2=export
    private void importDB(String backupDB){
        //File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String tmp = this.getPackageName();
        String currentDBPath = "/data/"+ tmp +"/databases/"+databaseHandler.DATABASE_NAME;
        //String backupDBPath = databaseHandler.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        //File backupDB = new File(sd, backupDBPath);
        try {
            dbHandler.close();
            MainActivity.this.deleteDatabase(databaseHandler.DATABASE_NAME);
            File dir = new File("/data/"+ tmp +"/databases/");
            //String backupDBPath = getFileNameWithoutExtension(databaseHandler.DATABASE_NAME,"\\", ".");
            //String extension = databaseHandler.DATABASE_NAME.substring(databaseHandler.DATABASE_NAME.lastIndexOf("."));
            File file = new File(dir, databaseHandler.DATABASE_NAME+"-shm");
            boolean b = file.delete();
            file = new File(dir, databaseHandler.DATABASE_NAME+"-wal");
            b = file.delete();

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

    /*public void onExportClicked(View v) {
        exportDB();
    }*/

    private void exportDB(){
        fn = 2;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
            return;
        }
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String tmp = this.getPackageName();
        String currentDBPath = "/data/"+ tmp +"/databases/"+databaseHandler.DATABASE_NAME;
        //Date currentTime = Calendar.getInstance().getTime();
        //SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        //String formatted = format1.format(currentTime.getTime());

        String backupDBPath = getFileNameWithoutExtension(databaseHandler.DATABASE_NAME,"\\", ".");
        backupDBPath = backupDBPath + "_"+getTime();
        String extension = databaseHandler.DATABASE_NAME.substring(databaseHandler.DATABASE_NAME.lastIndexOf("."));
        backupDBPath = backupDBPath + extension;
        //String backupDBPath = databaseHandler.DATABASE_NAME;
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if(fn == 1) importDbFromFile();
                    else if(fn==2) exportDB();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    fn=0;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
            default:
                fn = 0;
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
