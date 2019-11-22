package ac.mohammad.com.electricityconsumption;

import android.app.ListActivity;
import android.content.Intent;
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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static ac.mohammad.com.electricityconsumption.Util.getDateString;

/*public class InfoListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_list);
    }
}*/

public class InfoListActivity extends ListActivity {
    public databaseHandler dbHandler;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        /*menu.setHeaderTitle("Menu:");
        menu.add(0, v.getId(), 0, "Use");
*/
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        MyInfoArrayAdapter adapter = (MyInfoArrayAdapter)getListView().getAdapter();

        switch (item.getItemId()) {
            case R.id.menu_use:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                long id = this.getListView().getItemIdAtPosition(info.position);

                elec_info eInfo = adapter.mobInfoArray.get((int)id);

                Intent myIntent = new Intent(InfoListActivity.this, MainActivity.class);
                myIntent.putExtra("eInfo", eInfo);
                setResult(2,myIntent);
                finish();//finishing activity
                break;
            case R.id.menu_orderbyprevdate:
                List<elec_info> values = dbHandler.getAllRecords(SortType.PrevDate);
                //adapter.addAll(values);
                adapter.setValues(values);
                //setListAdapter(adapter);
                //adapter.notifyDataSetChanged();
                break;
            case R.id.menu_orderbysavedate:
                values = dbHandler.getAllRecords(SortType.SaveDate);
                //adapter.addAll(values);
                adapter.setValues(values);
                //setListAdapter(adapter);
                //adapter.notifyDataSetChanged();
                break;
            case R.id.menu_none:
                values = dbHandler.getAllRecords(SortType.None);
                //adapter.addAll(values);
                adapter.setValues(values);
                //setListAdapter(adapter);
                //adapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new databaseHandler(this);
        List<elec_info> values = dbHandler.getAllRecords(SortType.None);
        MyInfoArrayAdapter adapter = new MyInfoArrayAdapter(this, values);
        setListAdapter(adapter);
        final ListView listView = getListView();
        registerForContextMenu(listView);
 
        /*listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {
                MyInfoArrayAdapter adapter = (MyInfoArrayAdapter)listView.getAdapter();
                elec_info eInfo = adapter.mobInfoArray.get(position);
                Toast.makeText(getApplicationContext(), "Long Clicked : "+position, Toast.LENGTH_LONG).show();
                ListActivity.this.openContextMenu(listView);
                return true;
            }
        });*/
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
        MyInfoArrayAdapter adapter = (MyInfoArrayAdapter)l.getAdapter();
        elec_info eInfo = adapter.mobInfoArray.get(position);
        //Toast.makeText(this, position + " selected", Toast.LENGTH_LONG).show();
    }*/

    public void onLocationClick(View paramView)
    {
        String tmp = ((TextView) paramView).getText().toString();
        String loc[] = tmp.split(",");
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

enum SortType {PrevDate, SaveDate, None};
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

class MyInfoArrayAdapter extends ArrayAdapter<elec_info> {
    private final InfoListActivity theListActivity;
    public List<elec_info> mobInfoArray;

    public MyInfoArrayAdapter(InfoListActivity aListActivity, List<elec_info> values) {
        super(aListActivity, R.layout.info_row_layout, values);
        this.theListActivity = aListActivity;
        this.mobInfoArray = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView txt_tmp;

        LayoutInflater inflater = (LayoutInflater) theListActivity
                .getSystemService(theListActivity.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.info_row_layout, parent, false);
        if(mobInfoArray.size() <= position) {
            showInfo(mobInfoArray.get(0), rowView);
            Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();
        }else {
            showInfo(mobInfoArray.get(position), rowView);
        }

        Button delBtn = (Button) rowView.findViewById(R.id.btn_del);

        delBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                theListActivity.dbHandler.delRecord(mobInfoArray.get(position));
                mobInfoArray.remove(position); //or some other task
                notifyDataSetChanged();

            }
        });        return rowView;
    }

    private void showInfo(elec_info info, View rowView) {
        TextView txt_tmp;
        txt_tmp = (TextView) rowView.findViewById(R.id.tvTime);
        txt_tmp.setText(getDate(info.time));
        String tmp = getDateString(info.prevDateInMilliSec);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvPrevDate);
        txt_tmp.setText(tmp);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvPrevReading);
        txt_tmp.setText(String.format("%d", info.prevReading));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvNextDate);
        tmp = getDateString(info.nextDateInMilliSec);
        txt_tmp.setText(tmp);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvNextReading);
        txt_tmp.setText(String.format("%d", info.nextReading));
        txt_tmp = (TextView) rowView.findViewById(R.id.tvPrice);
        txt_tmp.setText(info.price);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvCalcString);
        txt_tmp.setText(info.calculationString);
        txt_tmp = (TextView) rowView.findViewById(R.id.tvUnitsString);
        txt_tmp.setText(String.format("%d", info.nextReading-info.prevReading));
    }

    private String getDate(String time) {
        Timestamp timestamp = Timestamp.valueOf(time);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        TimeZone tz = TimeZone.getDefault();

        calendar.setTimeInMillis(timestamp.getTime());
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        Date currenTimeZone = (Date)calendar.getTime();
        String date = sdf.format(currenTimeZone);

        return date;
    }

    public void setValues(List<elec_info> listValues){
        mobInfoArray.clear();
        mobInfoArray = null;
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


