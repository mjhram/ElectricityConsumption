package ac.mohammad.com.electricityconsumption;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/*public class InfoListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_list);
    }
}*/

public class InfoListActivity extends ListActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHandler dbHandler = new databaseHandler(this);
        List<elec_info> values = dbHandler.getAllRecords();
        MyInfoArrayAdapter adapter = new MyInfoArrayAdapter(this, values);
        setListAdapter(adapter);
    }

    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, position + " selected", Toast.LENGTH_LONG).show();
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

class MyInfoArrayAdapter extends ArrayAdapter<elec_info> {
    private final Context context;
    private final List<elec_info> mobInfoArray;

    public MyInfoArrayAdapter(Context context, List<elec_info> values) {
        super(context, R.layout.info_row_layout, values);
        this.context = context;
        this.mobInfoArray = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView txt_tmp;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.info_row_layout, parent, false);

        showInfo(mobInfoArray.get(position), rowView);

        return rowView;
    }

    private String getDateString(long msec) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(msec);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        month++;
        String tmp = day + "/" + month + "/"+ year;
        return tmp;
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


    }

    private String getDate(String time) {
        Timestamp timestamp = Timestamp.valueOf(time);
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();

        calendar.setTimeInMillis(timestamp.getTime());
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
        Date currenTimeZone = (Date)calendar.getTime();
        String date = sdf.format(currenTimeZone);

        return date;
    }
}


