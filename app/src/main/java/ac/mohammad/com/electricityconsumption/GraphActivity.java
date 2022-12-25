package ac.mohammad.com.electricityconsumption;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GraphActivity extends AppCompatActivity {
    GraphView graph1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);

        graph1 = (GraphView) findViewById(R.id.graph);
        initGraph();
    }

    public void initGraph(/*final GraphView graph*/) {
        int mLabels =3;
        databaseHandler dbHandler = new databaseHandler(this);
        List<elec_info> values = dbHandler.getAllRecords(SortType.PrevDate, "where isItBill=1");
        int mSize = values.size();
        DataPoint dp[] = new DataPoint[mSize];
        DataPoint price[] = new DataPoint[mSize];

        Date d2 = timestamp2date(values.get(0).prevDateInMilliSec);
        Date d1 = timestamp2date(values.get(mSize-1).prevDateInMilliSec);
        double maxPrice=1.0;
        for(int j=mSize-1, k=0; j>=0; j--, k++) {
            double readings = values.get(j).nextReading-values.get(j).prevReading+1;
            if(readings <= 0) continue;
            long days = (values.get(j).nextDateInMilliSec - values.get(j).prevDateInMilliSec) /(1000*60*60*24);
            if(days<=0) continue;
            Date dd = timestamp2date(values.get(j).prevDateInMilliSec);
            if(k==0) {
                d1=dd;
            }
            dp[k] =    new DataPoint(dd, readings*30/days);
            double thePrice = Double.parseDouble(values.get(j).price)*30.0/days;
            price[k] = new DataPoint(dd, thePrice);
            if(thePrice > maxPrice) maxPrice = thePrice;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp);
        // enable scrolling
        graph1.getViewport().setScrollable(true);
        // enable scaling
        graph1.getViewport().setScalable(true);

        series.setTitle(getString(R.string.avUnitsPerMonth));
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        graph1.addSeries(series);

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(price);
        series2.setTitle("Min");
        series2.setColor(Color.RED);
        series2.setDrawDataPoints(true);
        graph1.getSecondScale().addSeries(series2);
        graph1.getSecondScale().setMinY(0);
        graph1.getSecondScale().setMaxY(maxPrice*1.1);
        series2.setColor(Color.RED);

        GridLabelRenderer gridLabel = graph1.getGridLabelRenderer();
        graph1.getSecondScale().setVerticalAxisTitle(getString(R.string.avPricePerMonth));
        graph1.getSecondScale().setVerticalAxisTitleColor(Color.RED);

        gridLabel.setVerticalAxisTitle(getString(R.string.avUnitsPerMonth));
        //gridLabel.setVerticalLabelsSecondScaleColor(Color.RED);
        //gridLabel.reloadStyles();
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date d = new java.sql.Date((long) dataPoint.getX());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String formatted = format1.format(d.getTime());
                Toast.makeText(graph1.getContext(), String.format("%s,   %.2f", formatted, dataPoint.getY()), Toast.LENGTH_LONG).show();
            }
        });
        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date d = new java.sql.Date((long) dataPoint.getX());
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String formatted = format1.format(d.getTime());
                Toast.makeText(graph1.getContext(), String.format("%s,   %.2f", formatted, dataPoint.getY()), Toast.LENGTH_LONG).show();
            }
        });

        gridLabel.setLabelFormatter(new DateAsXAxisLabelFormatter(graph1.getContext()));
        gridLabel.setNumHorizontalLabels(mLabels);

        // set manual x bounds to have nice steps
        graph1.getViewport().setMinX(d1.getTime());
        graph1.getViewport().setMaxX(d2.getTime());
        graph1.getViewport().setXAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not nessecary
        gridLabel.setHumanRounding(false);
    }

    Date timestamp2date(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        return cal.getTime();
    }
}
