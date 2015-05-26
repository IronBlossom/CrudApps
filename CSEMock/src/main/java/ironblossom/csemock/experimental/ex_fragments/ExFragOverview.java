package ironblossom.csemock.experimental.ex_fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ironblossom.csemock.R;

public class ExFragOverview extends Fragment {
    SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yy hh:mm:ss a");
    TextView timeAndStatTv;
    LineChart lineChart;

    public ExFragOverview() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ex_frag_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        timeAndStatTv = (TextView) view.findViewById(R.id.timeAndStat);
        lineChart = (LineChart) view.findViewById(R.id.lineChart);


        timeAndStatTv.setText("" + dateFormater.format(new Date()) + "\nMarket Closed");
        lineChart.animateXY(2500, 2500);
        lineChart.setPinchZoom(false);

        setData(40, 5000);
    }

    private void setData(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            xVals.add("                09:30AM");
            xVals.add("11:40AM");
            xVals.add("01:50PM");
            xVals.add("04:00PM");
        }

        /*for (int i = 0; i < count; i++) {
            xVals.add((i) + "");
        }*/

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 3;// + (float)
            // ((mult *
            // 0.1) / 10);
            yVals.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "Market Overview");
        // set1.setFillAlpha(110);
//         set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        // set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(Color.parseColor("#548BD4"));
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(3f);
        set1.setCircleSize(1f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.CYAN);
        set1.setDrawFilled(true);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

      /*  LimitLine ll1 = new LimitLine(130f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.POS_RIGHT);
        ll2.setTextSize(10f);*/

        YAxis rightAxis = lineChart.getAxisRight();
        // rightAxis.addLimitLine(ll1);
        // rightAxis.addLimitLine(ll2);
        rightAxis.setAxisMaxValue(18008.05f);
        rightAxis.setAxisMinValue(17855.11f);
        rightAxis.setStartAtZero(false);

        lineChart.getAxisLeft().setEnabled(false);

        // set data
        lineChart.setData(data);

    }
}