package ac.mohammad.com.electricityconsumption;

import static ac.mohammad.com.electricityconsumption.Util.getDateString;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class InfoListActivity2 extends AppCompatActivity{//ListActivity {
    public databaseHandler dbHandler;
    private ListView listView;
    final int REQUEST_CODE_GALLERY = 999;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            elec_info eInfo = data.getParcelableExtra("eInfo");

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                //imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        MyInfoArrayAdapter2 adapter = (MyInfoArrayAdapter2)listView.getAdapter();
        List<elec_info> values;
        //switch (item.getItemId())
        if(item.getItemId()== R.id.menu_use) {
            //case R.id.menu_use:
            AdapterView.AdapterContextMenuInfo info;
            elec_info eInfo;
            long id;
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            id = listView.getItemIdAtPosition(info.position);

            eInfo = adapter.mobInfoArray.get((int) id);

            Intent myIntent = new Intent(InfoListActivity2.this, MainActivity.class);
            myIntent.putExtra("eInfo", eInfo);
            setResult(RESULT_OK, myIntent);
            finish();//finishing activity
            //break;
        } else if(item.getItemId()== R.id.menu_orderbyprevdate) {
            //case R.id.menu_orderbyprevdate:
            values = dbHandler.getAllRecords(SortType.PrevDate);
            //adapter.addAll(values);
            adapter.setValues(values);
            //setListAdapter(adapter);
            //adapter.notifyDataSetChanged();
            //break;
        } else if(item.getItemId()== R.id.menu_orderbysavedate) {
            //case R.id.menu_orderbysavedate:
            values = dbHandler.getAllRecords(SortType.SaveDate);
            //adapter.addAll(values);
            adapter.setValues(values);
            //setListAdapter(adapter);
            //adapter.notifyDataSetChanged();
            //break;
        } else if(item.getItemId()== R.id.menu_none) {
            //case R.id.menu_none:
                values = dbHandler.getAllRecords(SortType.None);
                //adapter.addAll(values);
                adapter.setValues(values);
                //setListAdapter(adapter);
                //adapter.notifyDataSetChanged();
                //break;
        }
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infolist);
        listView = (ListView) findViewById(R.id.infolist);

        dbHandler = new databaseHandler(this);
        List<elec_info> values = dbHandler.getAllRecords(SortType.None);
        MyInfoArrayAdapter2 adapter = new MyInfoArrayAdapter2(this, values);
        listView.setAdapter(adapter);
        //final ListView listView = getListView();
        registerForContextMenu(listView);
    }

    /*@Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getItemId() == CommonUtil.CONTEXT_MENU__DELETE_ID)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            long id = this.listView.getItemIdAtPosition(info.position);
            Log.d(TAG, "Item ID at POSITION:"+id);
        }
        else
        {
            return false;
        }
        return true;
    }*/
    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //String item = (String) getListAdapter().getItem(position);
        MyInfoArrayAdapter2 adapter = (MyInfoArrayAdapter2)l.getAdapter();
        elec_info eInfo = adapter.mobInfoArray.get(position);
        //Toast.makeText(this, position + " selected", Toast.LENGTH_LONG).show();
    }*/

    public void onLocationClick(View paramView)
    {
        String tmp = ((TextView) paramView).getText().toString();
        String[] loc = tmp.split(",");
        double lat = Double.parseDouble(loc[0]);
        double lon = Double.parseDouble(loc[1]);

        if ((lat == 0.0D) && (lon == 0.0D)) {
            return;
        }
        Uri localUri = Uri.parse("geo:0,0?q=" + lat + "," + lon);
        Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
        startActivity(localIntent);
    }

}

enum SortType {PrevDate, SaveDate, None}
/*
class MyComparatorB implements Comparator<elec_info> {
    private SortType orderType;
    public MyComparatorB(SortType type) {
        this.orderType = type;
    }

    public int compare(elec_info lhs, elec_info rhs) {
        int res = 0;
        if (orderType == SortType.PrevDate) {
            res = (int) (lhs.prevDateInMilliSec-rhs.prevDateInMilliSec);
        }
        else if (orderType == SortType.SaveDate) {
            long lhsTime, rhsTime;
            lhsTime = Timestamp.valueOf(lhs.time).getTime();
            rhsTime = Timestamp.valueOf(rhs.time).getTime();
            res = (int)(lhsTime - rhsTime);
        }
        return res;
    }

}*/

class MyInfoArrayAdapter2 extends ArrayAdapter<elec_info> {
    private final InfoListActivity2 theListActivity;
    public List<elec_info> mobInfoArray;

    public MyInfoArrayAdapter2(InfoListActivity2 aListActivity, List<elec_info> values) {
        super(aListActivity, R.layout.info_row_layout, values);
        this.theListActivity = aListActivity;
        this.mobInfoArray = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView txt_tmp;

        LayoutInflater inflater = (LayoutInflater) theListActivity
                .getSystemService(InfoListActivity2.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.info_row_layout, parent, false);
        if(mobInfoArray.size() <= position) {
            showInfo(mobInfoArray.get(0), rowView);
            Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();
        }else {
            showInfo(mobInfoArray.get(position), rowView);
        }

        Button delBtn = (Button) rowView.findViewById(R.id.btn_del);

        delBtn.setOnClickListener(v -> {
            //do something
            theListActivity.dbHandler.delRecord(mobInfoArray.get(position));
            mobInfoArray.remove(position); //or some other task
            notifyDataSetChanged();
        });
        return rowView;
    }

    private void showInfo(elec_info info, View rowView) {
        TextView txt_tmp;
        txt_tmp = (TextView) rowView.findViewById(R.id.tvTime);
        txt_tmp.setText(getDate(info.time));
        String tmp = getDateString(info.prevDateInMilliSec);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvPrevDate);
        txt_tmp.setText(tmp);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvPrevReading);
        txt_tmp.setText(String.format(Locale.ENGLISH,"%d", info.prevReading));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvNextDate);
        tmp = getDateString(info.nextDateInMilliSec);
        txt_tmp.setText(tmp);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvNextReading);
        txt_tmp.setText(String.format(Locale.ENGLISH,"%d", info.nextReading));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvPrice);
        txt_tmp.setText(String.format(Locale.ENGLISH,"%.0f", info.pricenum));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvCalcString);
        txt_tmp.setText(info.calculationString);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvUnitsString);
        txt_tmp.setText(String.format(Locale.ENGLISH,"%d", info.nextReading-info.prevReading));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvDaysString);
        long days = (long) (1.0 * (info.nextDateInMilliSec - info.prevDateInMilliSec) /(1000*60*60*24));
        txt_tmp.setText(String.format(Locale.ENGLISH,"%d", (int) days));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvIsItBillString);
        txt_tmp.setText(info.isItBill==1?"نعم":"لا");


    }

    private String getDate(String time) {
        Timestamp timestamp = Timestamp.valueOf(time);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        TimeZone tz = TimeZone.getDefault();

        calendar.setTimeInMillis(timestamp.getTime());
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        //SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd", Locale.ENGLISH);
        Date currenTime = calendar.getTime();
        return sdf.format(currenTime);
    }

    public void setValues(List<elec_info> listValues){
        mobInfoArray.clear();
        mobInfoArray = listValues;
        clear();
        addAll(listValues);
        notifyDataSetChanged();

    }
    /*public void sort(SortType sortType) {
        super.sort(new MyComparatorB(sortType));
        notifyDataSetChanged();
    }*/
}


