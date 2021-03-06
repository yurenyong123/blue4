package lecho.lib.hellocharts.samples;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.ComboLineColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;

public class ComboLineColumnChartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combo_line_column_chart);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A fragment containing a combo line/column chart view.
     */
    public static class PlaceholderFragment extends Fragment {

        private ComboLineColumnChartView chart;
        private ComboLineColumnChartData data;

        private int numberOfLines = 1;
        private int maxNumberOfLines = 4;
        private int numberOfPoints = 20;

        float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

        private boolean hasAxes = true;
        private boolean hasAxesNames = true;
        private boolean hasPoints = true;
        private boolean hasLines = true;
        private boolean isCubic = true;
        private boolean hasLabels = false;
        private boolean isFilled = false;

        private int i = 0;
        private int TIME = 1000;
        private TextView myTempleTextView;
        private LineChartData lineData;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_combo_line_column_chart, container, false);

            chart = (ComboLineColumnChartView) rootView.findViewById(R.id.chart);
            chart.setOnValueTouchListener(new ValueTouchListener());

            //generateValues();
            //generateData();

            myTempleTextView=(TextView)rootView.findViewById(R.id.TempleTextView);
            handler.postDelayed(runnable, TIME); //每隔1s执行
            return rootView;
        }

        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.combo_line_column_chart, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_reset) {
                reset();
                //generateData();
                return true;
            }
            if (id == R.id.action_add_line) {
                addLineToData();
                return true;
            }
            if (id == R.id.action_toggle_lines) {
                toggleLines();
                return true;
            }
            if (id == R.id.action_toggle_points) {
                togglePoints();
                return true;
            }
            if (id == R.id.action_toggle_cubic) {
                toggleCubic();
                return true;
            }
            if (id == R.id.action_toggle_labels) {
                toggleLabels();
                return true;
            }
            if (id == R.id.action_toggle_axes) {
                toggleAxes();
                return true;
            }
            if (id == R.id.action_toggle_axes_names) {
                toggleAxesNames();
                return true;
            }
            if (id == R.id.action_animate) {
                prepareDataAnimation();
                chart.startDataAnimation();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


        Handler handler = new Handler();

        Runnable runnable = new Runnable() {

            @Override
            public void run()
            {
                // handler自带方法实现定时器
                try {
                    handler.postDelayed(this, TIME);
                    //tvShow.setText(Integer.toString(i++));

                    int nowColor=ChartUtils.pickColor();
                    double textTemple= Math.random()*35+20;

                     //System.out.println("TTT "+textTemple);
                    textTemple=Math.floor(textTemple*10d)/10;

                    generateData((float)textTemple);

                    //generateLineData(nowColor, i,(float)textTemple);

                   myTempleTextView.setText("当前温度 " + textTemple + "℃");
                    myTempleTextView.setTextColor(nowColor);

                    i++;
                    if(i==20)
                        i=0;

                  //  System.out.println("do...");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("exception...");
                }
            }
        };


        private void generateValues() {
            for (int i = 0; i < maxNumberOfLines; ++i) {
                for (int j = 0; j < numberOfPoints; ++j) {
                    randomNumbersTab[i][j] = (float) Math.random() * 50f + 5;
                }
            }
        }


        private void generateData(float textTemple) {
            // Chart looks the best when line data and column data have similar maximum viewports.
            data = new ComboLineColumnChartData(generateColumnData(textTemple), generateLineData(textTemple));

            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                if (hasAxesNames) {
                    axisX.setName("时间间隔");
                    axisY.setName("温度");
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }
            chart.setComboLineColumnChartData(data);
        }

        private LineChartData generateLineData(float textTemple) {

//            chart.cancelDataAnimation();

            List<Line> lines = new ArrayList<Line>();
            for (int i = 0; i < numberOfLines; ++i) {

//                List<PointValue> values = new ArrayList<PointValue>();
//                for (int j = 0; j < numberOfPoints; ++j) {
//                    values.add(new PointValue(j, randomNumbersTab[i][j]));
//                }

//                Line line = new Line(values);
//                line.setColor(ChartUtils.COLORS[i]);
//                line.setCubic(isCubic);
//                line.setHasLabels(hasLabels);
//                line.setHasLines(hasLines);
//                line.setHasPoints(hasPoints);
//                lines.add(line);

                Line line = lineData.getLines().get(numberOfLines);// For this example there is always only one line.
                line.setColor(ChartUtils.COLORS[i]);
                line.setFilled(isFilled);
                line.setHasLabels(hasLabels);
                line.setHasLines(hasLines);
                line.setHasPoints(hasPoints);
                line.setCubic(isCubic);


                List<PointValue> lineList=line.getValues();

                for (int j=lineList.size()-1;j>-1;j--)
                {
                    PointValue value=lineList.get(j);
                    if (j<1)
                        value.setTarget(value.getX(), textTemple);
                    else
                    {
                        PointValue nextValue=lineList.get(j-1);
                        value.setTarget(value.getX(), nextValue.getY());
                    }
                }
                lines.add(line);
            }

            LineChartData lineChartData = new LineChartData(lines);

            return lineChartData;

        }

        private ColumnChartData generateColumnData(float textTemple) {
            int numSubcolumns = 1;
            int numColumns = 1;
            // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
            List<Column> columns = new ArrayList<Column>();
            List<SubcolumnValue> values;
            for (int i = 0; i < numColumns; ++i) {

                values = new ArrayList<SubcolumnValue>();
                for (int j = 0; j < numSubcolumns; ++j) {
                    values.add(new SubcolumnValue(textTemple, Color.rgb(200,200,200)));
                }

                columns.add(new Column(values));
            }

            ColumnChartData columnChartData = new ColumnChartData(columns);
            return columnChartData;
        }







        private void reset() {
            numberOfLines = 1;

            hasAxes = true;
            hasAxesNames = true;
            hasLines = true;
            hasPoints = true;
            hasLabels = false;
            isCubic = true;

        }

        private void toggleFilled() {
            isFilled = !isFilled;

            //generateData();
        }

        private void addLineToData() {
            if (data.getLineChartData().getLines().size() >= maxNumberOfLines) {
                Toast.makeText(getActivity(), "Samples app uses max 4 lines!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ++numberOfLines;
            }

           // generateData();
        }

        private void toggleLines() {
            hasLines = !hasLines;

            //generateData();
        }

        private void togglePoints() {
            hasPoints = !hasPoints;

            //generateData();
        }

        private void toggleCubic() {
            isCubic = !isCubic;

            //generateData();
        }

        private void toggleLabels() {
            hasLabels = !hasLabels;

            //generateData();
        }

        private void toggleAxes() {
            hasAxes = !hasAxes;

            //generateData();
        }

        private void toggleAxesNames() {
            hasAxesNames = !hasAxesNames;

            //generateData();
        }

        private void prepareDataAnimation() {

            // Line animations
            for (Line line : data.getLineChartData().getLines()) {
                for (PointValue value : line.getValues()) {
                    // Here I modify target only for Y values but it is OK to modify X targets as well.
                    value.setTarget(value.getX(), (float) Math.random() * 50 + 5);
                }
            }

            // Columns animations
            for (Column column : data.getColumnChartData().getColumns()) {
                for (SubcolumnValue value : column.getValues()) {
                    value.setTarget((float) Math.random() * 50 + 5);
                }
            }
        }

        private class ValueTouchListener implements ComboLineColumnChartOnValueSelectListener {

            @Override
            public void onValueDeselected() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onColumnValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                Toast.makeText(getActivity(), "Selected column: " + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPointValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Toast.makeText(getActivity(), "Selected line point: " + value, Toast.LENGTH_SHORT).show();
            }

        }
    }
}
