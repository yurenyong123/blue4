package lecho.lib.hellocharts.samples;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {


    ///Scan Button
    private TasksCompletedView mTasksView;
    private int mTotalProgress;
    private int mCurrentProgress;
    private Thread scanDeviceThread;
    private int scanDeviceNum;
    ///

    ///List
    private ExpandableListView senserList = null;
    //创建一级条目标题
    private Map<String, String> title_1;
    //创建一级条目容器
    private List<Map<String, String>> mySenserGroup ;
    //子条目内容
    private List<List<Map<String, String>>> mySenserChildName;
    private List<Map<String, String>> mySenserChildNameContaner;
    private List<List<Map<String, String>>> mySenserChildMac;
    private List<Map<String, String>> mySenserChildMacContaner;



    private String nowSenserAddress; // <==要连接的蓝牙设备MAC地址
    private String nowSenserName; // <==要连接的蓝牙设备MAC地址
    private String linkedSenserAddress;
    private String linkedSenserName;

    private String IsLinkSenser;
    private MyExpListAdapter senserListViewAdapter;

    static final int CMD_START_DISCOVERY = 0x00;
    static final int CMD_STOP_DISCOVERY = 0x01;
    static final int CMD_TRY_LINK = 0x02;
    static final int CMD_CLOSE_SOCKET = 0x03;

    static final int CMD_TRYLINK_RETURN = 0x00;


    //初始化列表
    private void initSenserList()
    {
        title_1 = new HashMap<>();
        title_1.put("group", String.valueOf("扫描到的传感器"));

        mySenserGroup = new ArrayList<>();
        mySenserGroup.add(title_1);
    }

    //初始化扫描按钮
    private void initScanButton()
    {
        mTotalProgress = 100;
        mCurrentProgress = 0;
        mTasksView = (TasksCompletedView) findViewById(R.id.tasks_view);
        mTasksView.setOnClickListener(new mTasksViewClickListener());
        mTasksView.setTextPaint("Scan");
        scanDeviceThread = new Thread(new ProgressRunable());
    }

    //初始化蓝牙接收器
    private void  initBluetoothReciver()
    {
        //设置设备被找到广播
        registerReceiver(new BluetoothReciver(), new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //设置扫描结束广播
        registerReceiver(new BluetoothReciver(), new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        registerReceiver(new BluetoothReciver(), new IntentFilter("android.intent.action.ServiceToMain"));
    }

    //方法2




    //初始化窗口
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //传感器列表
        senserList = (ExpandableListView) findViewById(R.id.expList);
        senserList.setOnChildClickListener(new mySenserListChildOnClickListener());

        IsLinkSenser="F";

        initSenserList(); //初始化列表
        initScanButton(); //初始化扫描按钮
        initBluetoothReciver(); //初始化蓝牙接收器

        System.out.println("start Seriver");
        Main2Activity.this.startService(new Intent(Main2Activity.this, MyBluetoothService.class));
    }

    private void showTips() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("提醒")
                .setMessage("是否退出程序")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        android.os.Process.killProcess(android.os.Process.myPid());

//                        System.runFinalizersOnExit(true);
//                        System.exit(0);
                    }

                }).setNegativeButton("取消",

                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).create(); // 创建对话框
        alertDialog.show(); // 显示对话框
    }
    @Override
    protected void onDestroy() {
        System.out.println("mian2dis");
        Main2Activity.this.stopService(new Intent(Main2Activity.this, MyBluetoothService.class));
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showTips();
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }



    //发送广播
    public void sendCmdBroadcast(int cmd ,String command, String value){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        if(cmd==CMD_START_DISCOVERY)
            intent.putExtra("cmd", CMD_START_DISCOVERY);

        else if(cmd==CMD_STOP_DISCOVERY)
            intent.putExtra("cmd", CMD_STOP_DISCOVERY);

        else if(cmd==CMD_TRY_LINK)
        {
            intent.putExtra("cmd", CMD_TRY_LINK);
            intent.putExtra("address", nowSenserAddress);
            intent.putExtra("name", nowSenserName);
        }
        else if(cmd==CMD_CLOSE_SOCKET)
            intent.putExtra("cmd",CMD_CLOSE_SOCKET);


        sendBroadcast(intent);//发送广播
    }


    //扫描 "按钮" 监听器
    private class mTasksViewClickListener implements OnClickListener
    {
        @Override
        public void onClick(View v) {
            //扫描线程停止,或者在连接中
            if(scanDeviceThread.isAlive()==false && IsLinkSenser!="L")
            {
                mySenserChildName = new ArrayList<List<Map<String,String>>>();
                mySenserChildNameContaner = new ArrayList<Map<String,String>>();
                mySenserChildMac = new ArrayList<List<Map<String,String>>>();
                mySenserChildMacContaner = new ArrayList<Map<String,String>>();
                if(IsLinkSenser!="T")
                {
                    scanDeviceNum=0;
                    senserList.setVisibility(View.GONE);
                }
                else if(IsLinkSenser=="T")
                {
                    senserList.setVisibility(View.GONE);
                    System.out.println("ttttttttttttttttt");
                    scanDeviceNum=1;
                    senserListViewAdapter = upDataListData(senserList, mySenserGroup, mySenserChildName, mySenserChildMac
                            , R.layout.groups_unpair, new int[]{R.id.groupUnpairedNameTo});
                    Map<String, String> content = new HashMap<String, String>();
                    content.put("child", linkedSenserName);
                    Map<String, String> contentMac = new HashMap<String, String>();
                    contentMac.put("childMac", linkedSenserAddress);

                    mySenserChildNameContaner.add(content);
                    mySenserChildName.add(mySenserChildNameContaner);
                    mySenserChildMacContaner.add(contentMac);
                    mySenserChildMac.add(mySenserChildMacContaner);
                    //senserList.setVisibility(View.VISIBLE);
                    senserListViewAdapter.notifyDataSetChanged();
                }


                scanDeviceThread = new Thread(new ProgressRunable());
                scanDeviceThread.start();


                sendCmdBroadcast(CMD_START_DISCOVERY,"","");
                //bluetoothAdapter.startDiscovery();
            }
            else
            {
                System.out.println("elelellelel");
            }
        }
    }

    //扫描ButtonThread
    class ProgressRunable implements Runnable
    {
        @Override
        public void run() {
            while (mCurrentProgress < mTotalProgress) {
                mCurrentProgress += 1;
                mTasksView.setProgress(mCurrentProgress);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendCmdBroadcast(CMD_STOP_DISCOVERY ,"", "");
        }
    }


    //处理返回数据
    private void dealReturnState(int[] state)
    {
        switch (state[1]) {
            case 1:
                IsLinkSenser="T";
                linkedSenserAddress=nowSenserAddress;
                linkedSenserName=nowSenserName;

                Toast.makeText(getApplicationContext(), "已建立与 “"+nowSenserName+"” 的链接。",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Main2Activity.this, LineColumnDependencyActivity.class);
                intent.putExtra("param1", "0");
                intent.putExtra("param2", "");

                Bundle stateB = new Bundle();
                stateB.putIntArray("trans_state", state);
                intent.putExtras(stateB);

                startActivity(intent);

//                new AlertDialog.Builder(Main2Activity.this)
//                        .setTitle("连接成功")
//                        .setMessage("已建立与 “"+nowSenserName+"” 的链接。")
//                        .setPositiveButton("好",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(
//                                            DialogInterface dialoginterface, int i) {
//
//                                        Intent intent = new Intent(Main2Activity.this, LineChartActivity.class);
//
//                                        startActivity(intent);
//                                    }
//                                })
//                        .show();
                break;

            case 2:
                IsLinkSenser="F";
                new AlertDialog.Builder(Main2Activity.this)
                        .setTitle("连接不成功")
                        .setMessage("请确定 “"+nowSenserName+"” 已打开而且在通信范围内。")
                        .setPositiveButton("好",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialoginterface, int i) {
                                    }
                                })
                        .show();
                break;
        }
    }


    //蓝牙广播接收器
    private class BluetoothReciver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                System.out.println("扫描到-->sss");
                BluetoothDevice device =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceName=device.getName();

//                try {
//                    if (deviceName.contains("HMSoft")) {
                        senserList.setVisibility(View.VISIBLE);
                        Map<String, String> content = new HashMap<String, String>();
                        content.put("child", device.getName());
                        Map<String, String> contentMac = new HashMap<String, String>();
                        contentMac.put("childMac", device.getAddress());

                        mySenserChildNameContaner.add(content);
                        mySenserChildName.add(mySenserChildNameContaner);
                        mySenserChildMacContaner.add(contentMac);
                        mySenserChildMac.add(mySenserChildMacContaner);

                        if (scanDeviceNum == 0) {
                            senserListViewAdapter = upDataListData(senserList, mySenserGroup, mySenserChildName, mySenserChildMac
                                    , R.layout.groups_unpair, new int[]{R.id.groupUnpairedNameTo});
                        } else
                            senserListViewAdapter.notifyDataSetChanged();

                        scanDeviceNum++;
//                    }
//                }
//                catch(Exception e)
//                {
//                    System.out.println("exceptionKKK "+e);
//                }
            }

            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                showScanedDialog();


            else if(intent.getAction().equals("android.intent.action.ServiceToMain")){
                System.out.println("returnnnn");
                Bundle bundle = intent.getExtras();
                int cmd = bundle.getInt("cmd");

                if(cmd == CMD_TRYLINK_RETURN) {
                    int[] stateBuffer = bundle.getIntArray("state");
                    dealReturnState(stateBuffer);
//                    dealReturnState(bundle.getInt("state"));
                }
            }
        }
    }

    //扫描结束对话框
    private void showScanedDialog()
    {
        System.out.println("扫描结束-->sss");
        if(scanDeviceNum!=0)
        {
            String text="扫描结束发现 "+scanDeviceNum +" 个传感器";

            Toast.makeText(getApplicationContext(), text,
                    Toast.LENGTH_SHORT).show();
            mTasksView.setProgress(0);
            mTasksView.setTextPaint("Scan");
            mCurrentProgress = 0;
        }
        else
        {
            new AlertDialog.Builder(Main2Activity.this)
                    .setTitle("扫描完成")
                    .setMessage("未发现可用传感器")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    System.out.println("vvvv");
                                    mTasksView.setProgress(0);
                                    mTasksView.setTextPaint("Scan");
                                    mCurrentProgress = 0;
                                }
                            }).show();

        }
    }


    //我的传感器配对按钮监听器
    class mySenserListChildOnClickListener implements ExpandableListView.OnChildClickListener
    {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            if(scanDeviceThread.isAlive()) {
                scanDeviceThread.interrupt();
                mCurrentProgress = 100;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendCmdBroadcast(1, "", "");
            }

            List<Object> myObjectList = senserListViewAdapter.getChild(groupPosition, childPosition);

            Map<String, String> SenserName = (Map<String, String>) myObjectList.get(0);
            Map<String, String> SenserAddress = (Map<String, String>) myObjectList.get(1);


            // selaPaired.setSenserState(v, "T", childPosition);

            if(IsLinkSenser=="F") {
                //是否使用传感器标识符
                nowSenserAddress=SenserAddress.get("childMac");
                nowSenserName=SenserName.get("child");
                IsLinkSenser = "L";
                Toast.makeText(getApplicationContext(), "正在尝试与 “"+nowSenserName+"” 建立连接，请稍后····",Toast.LENGTH_SHORT).show();
                sendCmdBroadcast(CMD_TRY_LINK,nowSenserAddress,nowSenserName);
            }
            else if(IsLinkSenser=="T")
            {
                nowSenserAddress=SenserAddress.get("childMac");
                nowSenserName=SenserName.get("child");
                if(!linkedSenserAddress.equals(SenserAddress.get("childMac")))
                {
                    new AlertDialog.Builder(Main2Activity.this)
                            .setTitle("警告")
                            .setMessage("已经与 “"+linkedSenserName+"”  建立连接，是否继续连接新传感器")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {


                                            IsLinkSenser = "L";
                                            Toast.makeText(getApplicationContext(), "正在尝试与 “"+nowSenserName+"” 建立连接，请稍后····",Toast.LENGTH_SHORT).show();
                                            sendCmdBroadcast(CMD_CLOSE_SOCKET, "", "");
                                            sendCmdBroadcast(CMD_TRY_LINK,nowSenserAddress,nowSenserName);
                                        }
                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {
                                        }
                                    })
                            .show();
                }
                else
                {
                    new AlertDialog.Builder(Main2Activity.this)
                            .setTitle("警告")
                            .setMessage("已经与 “" + linkedSenserName + "”  建立连接")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {
                                            Intent intent = new Intent(Main2Activity.this, LineColumnDependencyActivity.class);
                                            intent.putExtra("param1", "0");
                                            intent.putExtra("param2", "");
                                            int[] state = new int[4];
                                            Bundle stateB = new Bundle();
                                            stateB.putIntArray("trans_state", state);
                                            intent.putExtras(stateB);
                                            startActivity(intent);
                                        }
                                    })
                            .setNegativeButton ("取消配对",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialoginterface, int i) {
                                            sendCmdBroadcast(CMD_CLOSE_SOCKET, "", "");
                                            IsLinkSenser = "F";
                                        }
                                    })
                            .show();
                }
            }
            return false;
        }
    }


    //创建ExpListAdapter对象
    private MyExpListAdapter upDataListData(ExpandableListView thisExpList, List<Map<String, String>> thisGruops, List<List<Map<String, String>>> thisGruopsChilds, List<List<Map<String, String>>> thisGruopsChildsMac, int groupLayout, int[] groupTo)
    {
         MyExpListAdapter thisSelaPaired = new MyExpListAdapter (
                Main2Activity.this,
                thisGruops, groupLayout, new String[]{"group"}, groupTo,
                thisGruopsChilds, R.layout.childs, new String[]{"child"}, new int[]{R.id.childNameTo},
                thisGruopsChildsMac, new String[]{"childMac"}, new int[]{R.id.childMac}
        );
        thisExpList.setAdapter(thisSelaPaired);
        return  thisSelaPaired;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

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
                new AlertDialog.Builder(Main2Activity.this)
                        .setTitle("警告")
                        .setMessage("您没有记录过数据")
                        .setPositiveButton("确定", null)
                        .show();
            }
            else {
                final String[] finalFilePath = filePath;
                final String finalPath = path;
                new AlertDialog.Builder(Main2Activity.this).
                        setTitle("请选择文件")
                                //.setIcon(R.drawable.ic_launcher)
                        .setItems(fileName, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Main2Activity.this, finalFilePath[which], Toast.LENGTH_SHORT).show();


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

                                        Intent intent = new Intent(Main2Activity.this, LineColumnDependencyActivity.class);
                                        intent.putExtra("param1", "1");
                                        intent.putExtra("param2", finalFilePath[which]);
                                        int[] state = new int[4];
                                        Bundle stateB = new Bundle();
                                        stateB.putIntArray("trans_state", state);
                                        intent.putExtras(stateB);

                                        startActivity(intent);

                                        //showRecoedData(recordMsgBuffer);



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

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

}
