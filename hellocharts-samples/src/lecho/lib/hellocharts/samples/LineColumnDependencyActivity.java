package lecho.lib.hellocharts.samples;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

public class LineColumnDependencyActivity extends AppCompatActivity  {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_column_dependency);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        public final String[] days = new String[]{"1", "2", "3", "4", "5", "6", "7","8", "9", "10", "11", "12", "13", "14","15", "16", "17", "18", "19","20"};

        private LineChartView realeChartTop;
        private LineChartData lineData;


        private LineChartView recordChart;
        private PreviewLineChartView recordPreviewChart;
        private LineChartData data;
        /**
         * Deep copy of data.
         */
        private LineChartData previewData;
        private Button realButtonRecord;
        private boolean buttonRecordState;

        private Button recordButtonReturnReal;
        private Button setTimeButton;
        private Button clearButton;
        private Button readButton;
        private Button lookRecordButton;

        //private int i = 0;
        //private int TIME = 1000;

        private TextView realTempleTextView;
        private boolean isFilled = false;
        private boolean hasLabels = false;
        private boolean hasLabelForSelected = false;
        private boolean hasAxesNames = true;

       // float[] templeValue = new float[20];
        static final int CMD_GET_DATA = 0x04;
        static final int CMD_SETTING_RECORD= 0x05;
        static final int CMD_REQUES_RECORD= 0x06;

        static final int CMD_RETURN_DATA = 0x01;
        static final int CMD_FAILED_GET_DATA = 0x02;
        static final int CMD_RETURN_RECORD_DATA = 0x03;


        int nowColor=ChartUtils.COLOR_BLUE;// .pickColor();

        float templeValue;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View rootView = inflater.inflate(R.layout.fragment_line_column_dependency, container, false);

            // *** TOP LINE recordChart ***
            realeChartTop = (LineChartView) rootView.findViewById(R.id.chart_top);
            generateInitialLineData();
            recordChart = (LineChartView) rootView.findViewById(R.id.chart_temple);
            recordPreviewChart = (PreviewLineChartView) rootView.findViewById(R.id.chart_preview_temple);

            realTempleTextView=(TextView)rootView.findViewById(R.id.TempletextView);
            realTempleTextView.setTextColor(nowColor);

            realButtonRecord=(Button)rootView.findViewById(R.id.buttonRecord);
            buttonRecordState=false;

            setTimeButton = (Button)rootView.findViewById(R.id.set_timespan);
            clearButton = (Button)rootView.findViewById(R.id.clear);
            readButton = (Button)rootView.findViewById(R.id.read);
            lookRecordButton=(Button)rootView.findViewById(R.id.look_record);

            recordButtonReturnReal=(Button)rootView.findViewById(R.id.buttonReturnReal);
            recordButtonReturnReal.setOnClickListener(new returnRealeButtonListener());

            Intent intent = getIntent();
            String string_data1 = intent.getStringExtra("param1");
            String string_data2 = intent.getStringExtra("param2");
            Bundle bundle = intent.getExtras();
            int[] stateBuffer = bundle.getIntArray("trans_state");

            if (stateBuffer[2] != 1)
            {
                buttonRecordState=false;
                realButtonRecord.setText("▶");
            }
            else {
                buttonRecordState=true;
                realButtonRecord.setText("●");
            }


            realButtonRecord.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (buttonRecordState == false) {

                        //选项数组
                        String[] choices = {"5秒", "10秒", "30秒", "60秒", "5分钟", "10分钟", "30分钟", "1小时", "2小时"};
                        //包含多个选项的对话框
                        AlertDialog dialog = new AlertDialog.Builder(LineColumnDependencyActivity.this)
                                //.setIcon(android.R.drawable.btn_star)
                                .setTitle("参数选择")
                                .setItems(choices, onselect).create();
                        dialog.show();
                    } else {
                        sendCmdBroadcast(CMD_REQUES_RECORD, 0);
                        buttonRecordState = false;
                        realButtonRecord.setText("▶");

                    }
                }
            });



            if (Integer.parseInt(string_data1) != 1){
                initBluetoothReciver();

                disReadHideRecord();

                sendCmdBroadcast(CMD_GET_DATA,0);//请求数据
            }
            else{

                File file = new File(string_data2);
                if (file.exists()) {
                    // System.out.println("binggo");

                    FileReader fr=null;
                    try {
                        fr = new FileReader(file);
                        BufferedReader br=new BufferedReader(fr);
                        String temp=null;
                        String s="";
                        while((temp=br.readLine())!=null)
                            s+=temp+"\n";
                        String [] ss=s.split("\n");

                        //System.out.println("LONG　"+ss.length);

                        int[] recordMsgBuffer =new int[ss.length];
                        for (int i = 0; i < ss.length; i++) {
                            // System.out.println(ss[i]);
                            recordMsgBuffer[i] = Integer.parseInt(ss[i]);
                        }

                        showRecoedData(recordMsgBuffer);



                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                disRecordHideReal();

            }

            return rootView;
        }

        DialogInterface.OnClickListener onselect = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                System.out.println("sssssssssssss " + which);

                realButtonRecord.setText("●");

                buttonRecordState=true;

               sendCmdBroadcast(CMD_SETTING_RECORD, which);
                switch (which) {
                    case 0:
                        Toast.makeText(LineColumnDependencyActivity.this, "5s",Toast.LENGTH_SHORT).show();
                        //sendCmdBroadcast(CMD_START_RECORD,0);
                        break;
                    case 1:
                        Toast.makeText(LineColumnDependencyActivity.this, "10s",Toast.LENGTH_SHORT).show();

                        break;
                    case 2:
                        Toast.makeText(LineColumnDependencyActivity.this, "30s",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(LineColumnDependencyActivity.this, "60s",Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(LineColumnDependencyActivity.this, "5min",Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(LineColumnDependencyActivity.this, "10min",Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(LineColumnDependencyActivity.this, "30min",Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(LineColumnDependencyActivity.this, "1hours",Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(LineColumnDependencyActivity.this, "2hours",Toast.LENGTH_SHORT).show();
                        break;
                }
            }

        };



        @Override
        public void onDestroy() {
            super.onDestroy();

            //handler.removeCallbacks(runnable);
            Toast.makeText(LineColumnDependencyActivity.this, "ddddddddddddddd",Toast.LENGTH_SHORT).show();
        }

        class returnRealeButtonListener implements View.OnClickListener
        {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String string_data1 = intent.getStringExtra("param1");

                if (Integer.parseInt(string_data1) == 0)
                {
                    disReadHideRecord();
                }
                else
                {
                    intent = new Intent(LineColumnDependencyActivity.this, Main2Activity.class);
                    startActivity(intent);
                }

            }
        }

        private  void disReadHideRecord()
        {
            realeChartTop.setVisibility(View.VISIBLE);
            realButtonRecord.setVisibility(View.VISIBLE);
            realTempleTextView.setVisibility(View.VISIBLE);


            recordChart.setVisibility(View.GONE);
            recordPreviewChart.setVisibility(View.GONE);
            recordButtonReturnReal.setVisibility(View.GONE);
        }

        private  void disRecordHideReal()
        {
            realeChartTop.setVisibility(View.GONE);
            realButtonRecord.setVisibility(View.GONE);
            realTempleTextView.setVisibility(View.GONE);
            

            recordChart.setVisibility(View.VISIBLE);
            recordPreviewChart.setVisibility(View.VISIBLE);
            recordButtonReturnReal.setVisibility(View.VISIBLE);
        }




        //产生数据 记录
        //////////////////////////////////////////////////////////////////////////////////////////////////////
        private void generateDefaultData(int[] recordMsgBuffer) {
            List<PointValue> values = new ArrayList<PointValue>();

            for(int j=0;j<recordMsgBuffer.length;j++) {
//                if(j==0)
//                    values.add(new PointValue(j, 0));
//                else
                values.add(new PointValue(j, (float)recordMsgBuffer[j] / 10f));
            }



            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_GREEN);
            line.setHasPoints(false);// too many values so don't draw points.
           // line.setFilled(true);

            List<Line> lines = new ArrayList<Line>();
            lines.add(line);

            data = new LineChartData(lines);
            data.setAxisXBottom(new Axis().setHasLines(true).setTextColor(ChartUtils.COLOR_GREEN));
            data.setAxisYLeft(new Axis().setHasLines(true).setTextColor(ChartUtils.COLOR_GREEN));



            // prepare preview data, is better to use separate deep copy for preview recordChart.
            // Set color to grey to make preview area more visible.
            previewData = new LineChartData(data);
            previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR);




        }

        private void previewY() {
            Viewport tempViewport = new Viewport(recordChart.getMaximumViewport());
            float dy = tempViewport.height() / 4;
            tempViewport.inset(0, dy);
            recordPreviewChart.setCurrentViewportWithAnimation(tempViewport);
            recordPreviewChart.setZoomType(ZoomType.VERTICAL);
        }

        private void previewX(boolean animate) {
           Viewport tempViewport = new Viewport(recordChart.getMaximumViewport());

            float dx = tempViewport.width() / 4;
            tempViewport.inset(dx, 0);
            if (animate) {
                recordPreviewChart.setCurrentViewportWithAnimation(tempViewport);
            } else {
                recordPreviewChart.setCurrentViewport(tempViewport);
            }
            recordPreviewChart.setZoomType(ZoomType.HORIZONTAL);

        }

        private void previewXY() {
            // Better to not modify viewport of any recordChart directly so create a copy.
            Viewport tempViewport = new Viewport(recordChart.getMaximumViewport());
            // Make temp viewport smaller.
            float dx = tempViewport.width() / 4;
            float dy = tempViewport.height() / 4;
            tempViewport.inset(dx, dy);
            recordPreviewChart.setCurrentViewportWithAnimation(tempViewport);
        }

        private class ViewportListener implements ViewportChangeListener {

            @Override
            public void onViewportChanged(Viewport newViewport) {
                // don't use animation, it is unnecessary when using preview recordChart.
                recordChart.setCurrentViewport(newViewport);
            }

        }

        /////////////////////////////////////////////////////////////////////////////////////////



        //产生数据 实时
        /////////////////////////////////////////////////////////////////////////////////////////
        private void generateInitialLineData() {
            int numValues = 19;

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            List<PointValue> values = new ArrayList<PointValue>();
            for (int i = 0; i < numValues; ++i) {
                values.add(new PointValue(i, 0));
                axisValues.add(new AxisValue(i).setLabel(days[i]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLOR_BLUE);
            line.setHasPoints(false);
            line.getShape();
            line.setCubic(false);//平滑
            List<Line> lines = new ArrayList<Line>();
            lines.add(line);



            Axis axisX = new Axis(axisValues);
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("时间间隔");
                //axisY.setName("温度");
                axisX.setTextColor(ChartUtils.HOLO_BLUE_DARK);
                axisY.setTextColor(ChartUtils.HOLO_BLUE_DARK);
            }

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(axisX.setHasLines(true));
            lineData.setAxisYLeft(axisY.setMaxLabelChars(3));



            realeChartTop.setLineChartData(lineData);

            // For build-up animation you have to disable viewport recalculation.
            //建立动画你必须禁用视图重新计算。
            realeChartTop.setViewportCalculationEnabled(false);

            // And set initial max viewport and current viewport- remember to set viewports after data.
            //设置初始值和当前视口视口记得要在数据集视图。
            Viewport v = new Viewport(0, 50, 20, -10);
            realeChartTop.setMaximumViewport(v);
            realeChartTop.setCurrentViewport(v);
            realeChartTop.setZoomType(ZoomType.HORIZONTAL);

            //toggleFilled();
            //toggleLabels();
            toggleAxesNames();
        }

        private void toggleAxesNames() {
            hasAxesNames = !hasAxesNames;


        }

        private void toggleFilled() {
            isFilled = !isFilled;

            //generateData();
        }

        private void toggleLabels() {
            hasLabels = !hasLabels;

            if (hasLabels) {
                hasLabelForSelected = false;
                realeChartTop.setValueSelectionEnabled(hasLabelForSelected);
            }

            // generateData();
        }

        private void generateLineData(int color, int range, float textTemple) {
            // Cancel last animation if not finished.
            //chartTop.cancelDataAnimation();

            // Modify data targets
            //修改数据目标
            Line line = lineData.getLines().get(0);// For this example there is always only one line.
            line.setColor(color);
            line.setFilled(true);
            line.setHasLabels(true);
            line.setHasPoints(false);
            List<PointValue> lineList=line.getValues();
//            for (int j=lineList.size()-1;j>-1;j--)
//            {
//                PointValue value=lineList.get(j);
//                if (j<1)
//                {
//                    value.setTarget(value.getX(), textTemple);
//                }
//                else
//                {
//                    PointValue nextValue=lineList.get(j-1);
//                    value.setTarget(value.getX(), nextValue.getY());
//                }
//            }

            for (int j=0;j<lineList.size();j++)
            {
                PointValue value=lineList.get(j);
                if (j==lineList.size()-1)
                {
                    value.setTarget(value.getX(), textTemple);
                }
                else
                {
                    PointValue nextValue=lineList.get(j+1);
                    value.setTarget (value.getX(), nextValue.getY());
                }
            }

            // Start new data animation with 300ms duration;
            realeChartTop.startDataAnimation(0);
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        //蓝牙广播接收器
        private class BluetoothReciver extends BroadcastReceiver
        {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action= intent.getAction();


                if(action.equals("android.intent.action.ServiceToLineChart")){

                    Bundle bundle = intent.getExtras();
                    int cmd = bundle.getInt("cmd");

                    if (cmd==CMD_RETURN_DATA)
                    {
                        templeValue=bundle.getInt("value");
                        templeValue=templeValue/(float)10;

                        realTempleTextView.setText("当前温度 " + templeValue + "℃");

                        System.out.println("当前温度 " + templeValue + "℃ " + realButtonRecord.getText());

                        generateLineData(nowColor, 0, (float) templeValue);



                        sendCmdBroadcast(CMD_GET_DATA, 0);//请求数据
                    }
                    else if(cmd==CMD_FAILED_GET_DATA) {
                        sendCmdBroadcast(CMD_GET_DATA, 0);//请求数据
                    }
                    else if(cmd==CMD_RETURN_RECORD_DATA)
                    {
                        System.out.println("ooooKK");
                        int[] recordMsgBuffer=bundle.getIntArray("buffer");
                        //showRecoedData(recordMsgBuffer);
                        saveRecordData(recordMsgBuffer);
                    }
                }
            }
        }


        private  void showRecoedData(int[] recordMsgBuffer)
        {
            generateDefaultData(recordMsgBuffer);

            recordChart.setLineChartData(data);
            recordChart.setZoomEnabled(false);
            recordChart.setScrollEnabled(false);

            recordPreviewChart.setLineChartData(previewData);
            recordPreviewChart.setViewportChangeListener(new ViewportListener());

            previewX(false);

            disRecordHideReal();

        }

        private void saveRecordData(int[] recordMsgBuffer)
        {
            SimpleDateFormat formatter =new    SimpleDateFormat    ("yyyy年MM月dd日HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str = formatter.format(curDate);
            //str+="\r\n";


            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

                File sdcardDir =Environment.getExternalStorageDirectory();
                //得到一个路径，内容是sdcard的文件夹路径和名字
                String path=sdcardDir.getPath()+"/wukongSenser/templeSenser";
                File path1 = new File(path);
                if (!path1.exists())
                    path1.mkdirs();


                File sdFile = new File(path, str+"温度数据.txt");


                try {
                    RandomAccessFile raf = new RandomAccessFile(sdFile, "rw");

                    for(int j=0;j<recordMsgBuffer.length;j++) {
                        // 将文件记录指针移动最后
                        raf.seek(sdFile.length());
                        // 输出文件内容
                        String nowString =Integer.toString(recordMsgBuffer[j])+"\r\n";
                        raf.write(nowString.getBytes());
                    }
                    raf.close();

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Toast.makeText(LineColumnDependencyActivity.this, "成功保存到sd卡", Toast.LENGTH_LONG).show();

            }


        }



        //初始化蓝牙接收器
        private void  initBluetoothReciver()
        {
            registerReceiver(new BluetoothReciver(), new IntentFilter("android.intent.action.ServiceToLineChart"));
        }

        //发送广播
        public void sendCmdBroadcast(int cmd,int value){
            Intent intent = new Intent();//创建Intent对象
            intent.setAction("android.intent.action.cmd");
            intent.putExtra("cmd", cmd);
            intent.putExtra("value", value);
            sendBroadcast(intent);//发送广播
        }

        //列
//        private void generateColumnData() {
//
//            int numSubcolumns = 1;
//            int numColumns = months.length;
//
//            List<AxisValue> axisValues = new ArrayList<AxisValue>();
//            List<Column> columns = new ArrayList<Column>();
//            List<SubcolumnValue> values;
//            for (int i = 0; i < numColumns; ++i) {
//
//
//
//                values = new ArrayList<SubcolumnValue>();
//                for (int j = 0; j < numSubcolumns; ++j) {
//                    //values.add(new SubcolumnValue((float) 2 * 50f + 5, Color.rgb(255,0,0)));//设置数据高度,随机颜色
//
//                    values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
//                }
//
//                axisValues.add(new AxisValue(i).setLabel(months[i]));
//
//                columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
//            }
//
//            columnData = new ColumnChartData(columns);
//
//            columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
//            columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(2));
//
//            chartBottom.setColumnChartData(columnData);
//
//            // Set value touch listener that will trigger changes for chartTop.
//            chartBottom.setOnValueTouchListener(new ValueTouchListener());
//
//            // Set selection mode to keep selected month column highlighted.
//            chartBottom.setValueSelectionEnabled(true);
//
//            chartBottom.setZoomType(ZoomType.HORIZONTAL);
//
//        }

        private class ValueTouchListener implements ColumnChartOnValueSelectListener {

            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {

                System.out.println("sss " + columnIndex + " " + subcolumnIndex + " "+ value);

               // generateLineData(value.getColor(), columnIndex);
            }

            @Override
            public void onValueDeselected() {

               // generateLineData(ChartUtils.COLOR_GREEN, 0);

            }
        }


        // MENU
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.temlpe_menu, menu);
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.show_temple_data) {

                //final String[] arrayFruit = new String[] { "1.txt", "2.txt", "3.txt", "4.txt" };
                String[] fileName= new String[0];
                String[] filePath= new String[0];
                String path="";

                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    File sdcardDir = Environment.getExternalStorageDirectory();
                    //得到一个路径，内容是sdcard的文件夹路径和名字

                     path = sdcardDir.getPath() + "/wukongSenser/templeSenser";
                    File path1 = new File(path);
                    if (!path1.exists())
                        path1.mkdirs();

                    File[] files = new File(path).listFiles();
                    fileName = new String[files.length];
                    filePath = new String[files.length];

                    for(int i=0;i<files.length;i++) {

                        if(files[i].isFile()){
                            filePath[i]=files[i].getAbsolutePath();
                            fileName[i] = filePath[i].substring(filePath[i].lastIndexOf("/") + 1);
                        }
                    }
                }

                if(fileName.length==0)
                {
                    new AlertDialog.Builder(LineColumnDependencyActivity.this)
                            .setTitle("警告")
                            .setMessage("您没有记录过数据")
                            .setPositiveButton("确定", null)
                            .show();
                }
                else {
                    final String[] finalFilePath = filePath;
                    final String finalPath = path;
                    new AlertDialog.Builder(LineColumnDependencyActivity.this).
                            setTitle("请选择文件")
                                    //.setIcon(R.drawable.ic_launcher)
                            .setItems(fileName, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(LineColumnDependencyActivity.this, finalFilePath[which], Toast.LENGTH_SHORT).show();


                                    //String filepath = finalPath + "/qq.txt";

                                    System.out.println("pS "+finalFilePath[which]);

                                    File file = new File(finalFilePath[which]);
                                    if (file.exists()) {
                                       // System.out.println("binggo");

                                        FileReader fr=null;
                                        try {
                                            fr = new FileReader(file);
                                            BufferedReader br=new BufferedReader(fr);
                                            String temp=null;
                                            String s="";
                                            while((temp=br.readLine())!=null)
                                                    s+=temp+"\n";
                                            String [] ss=s.split("\n");

                                            //System.out.println("LONG　"+ss.length);

                                            int[] recordMsgBuffer =new int[ss.length];
                                            for (int i = 0; i < ss.length; i++) {
                                               // System.out.println(ss[i]);
                                                recordMsgBuffer[i] = Integer.parseInt(ss[i]);
                                            }

                                            showRecoedData(recordMsgBuffer);



                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                        catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {

                                    }

                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
                return true;
            }
            else if (id == R.id.save_data)
            {
                sendCmdBroadcast(CMD_REQUES_RECORD, 0);
            }

            return super.onOptionsItemSelected(item);
        }
    }
}
