package lecho.lib.hellocharts.samples;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * Created by guess on 2015/9/25.
 */
public class MyBluetoothService extends Service {


    public boolean threadFlag = true;
    //MyThread myThread;
    CommandReceiver cmdReceiver;//继承自BroadcastReceiver对象，用于得到Activity发送过来的命令

    /**************service 命令*********/

    static final int CMD_START_DISCOVERY = 0x00;
    static final int CMD_STOP_DISCOVERY = 0x01;
    static final int CMD_TRY_LINK = 0x02;
    static final int CMD_CLOSE_SOCKET = 0x03;
    static final int CMD_GET_DATA = 0x04;
    static final int CMD_SETTING_RECORD= 0x05;
    static final int CMD_REQUES_RECORD= 0x06;

    static final int CMD_TRYLINK_RETURN = 0x00;
    static final int CMD_RETURN_DATA = 0x01;
    static final int CMD_FAILED_GET_DATA = 0x02;
    static final int CMD_RETURN_RECORD_DATA = 0x03;

    static final int RETURN_VALUE_SUCCESSED = 1;
    static final int RETURN_VALUE_FAILED = 2;

    static final int THREAD_SLEEP_TIME=1000;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    public  boolean bluetoothFlag  = true;
    private boolean testComunnicationFlag;

   // private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String nowSenserAddress; // <==要连接的蓝牙设备MAC地址
    private String nowSenserName; // <==要连接的蓝牙设备MAC地址

    private int nowValue;

    private int CMD_RECORD_TIME_VALUE;

    private boolean IsSettingRecordData;

    private boolean IsGettingData;
    private boolean IsGettingRecordData;


    private  int[] recordMsgBuffer=new int[0];
    private  int[] stateBuffer = new int[0];

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //初始化蓝牙
    private void initBluetooth()
    {
        //得到BluetoothAdapter对象
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter!=null)
        {
            //判断蓝牙是否可用
            if(!mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.enable();
        }
        else
        {
            //没有蓝牙
//            new AlertDialog.Builder(Main2Activity.this)
//                    .setTitle("错误")
//                    .setMessage("您的设备没有蓝牙")
//                    .setPositiveButton("好",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(
//                                        DialogInterface dialoginterface, int i) {
//                                }
//                            })
//                    .show();
        }
    }

    @Override
    public void onCreate() {
        initBluetooth();


        IsSettingRecordData=false;
        IsGettingData=false;
        IsGettingRecordData=false;


        super.onCreate();
        System.out.println("MyBlueServiceOnCreate----->");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("MyBlueServiceonStartCommand----->");

        registerReceiver(new CommandReceiver(), new IntentFilter("android.intent.action.cmd"));

//        doJob();//调用方法启动线程
        return super.onStartCommand(intent, flags, startId);
    }


//    public void doJob(){
//
//        threadFlag = true;
//        myThread = new MyThread();
//        myThread.start();
//
//    }
//
//
//    public class MyThread extends Thread{
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            super.run();
//
//            // System.out.println("readThread...connectDevice");
//            while(threadFlag){
//
//                System.out.println("readThread...");
//                int value = readByte();
//                System.out.println(value);
//                if(value != -1){
//                    //showToast(value + "");
//
//                }
//
//                try{
//                    Thread.sleep(5);
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//

    public void sendCmd(int cmd)//串口发送数据
    {
        System.out.print("hello ");
        if(!bluetoothFlag){
            return;
        }
        System.out.print("sendCmd ");

        byte[] msgBuffer=new byte[0];
        if(cmd==1||cmd==2)
        {
            msgBuffer= new byte[22];
            msgBuffer[0] = 0x1A;
            msgBuffer[1] = 0x00;
            msgBuffer[2] = 0x00;
            msgBuffer[3] = 0x00;
            msgBuffer[4] = 0x00;
            if(cmd==1) {
                msgBuffer[5] = 0x0F;
                msgBuffer[6] = 0x0F;
                msgBuffer[7] = 0x0F;
                msgBuffer[8] = 0x0F;
                msgBuffer[9] = 0x00;
                msgBuffer[10] = 0x00;
                msgBuffer[11] = 0x00;
                msgBuffer[12] = 0x01;
                msgBuffer[13] = 0x00;
                msgBuffer[14] = 0x00;
                msgBuffer[15] = 0x00;
                msgBuffer[16] = 0x00;
                msgBuffer[17] = 0x01;
                msgBuffer[18] = 0x01;
                msgBuffer[19] = 0x0d;
                msgBuffer[20] = 0x00;
            }
            else {//2
                msgBuffer[5] = 0x00;
                msgBuffer[6] = 0x00;
                msgBuffer[7] = 0x00;
                msgBuffer[8] = 0x01;
                msgBuffer[9] = 0x01;
                msgBuffer[10] = 0x00;
                msgBuffer[11] = 0x00;
                msgBuffer[12] = 0x03;
                msgBuffer[13] = 0x00;
                msgBuffer[14] = 0x00;
                msgBuffer[15] = 0x00;
                msgBuffer[16] = 0x00;
                msgBuffer[17] = 0x08;
                msgBuffer[18] = 0x09;
                msgBuffer[19] = 0x00;
                msgBuffer[20] = 0x0B;
            }
            msgBuffer[21] = 0x1D;
        }
        else if(cmd==CMD_SETTING_RECORD)
        {
            msgBuffer = new byte[30];
            msgBuffer[0] = 0x1A;
            msgBuffer[1] = 0x00;
            msgBuffer[2] = 0x00;
            msgBuffer[3] = 0x00;
            msgBuffer[4] = 0x00;
            msgBuffer[5] = 0x00;
            msgBuffer[6] = 0x00;
            msgBuffer[7] = 0x00;
            msgBuffer[8] = 0x01;

            msgBuffer[9] = 0x01;
            msgBuffer[10] = 0x00;
            msgBuffer[11] = 0x00;
            msgBuffer[12] = 0x09;

            msgBuffer[13] = 0x00;
            msgBuffer[14] = 0x00;
            msgBuffer[15] = 0x00;
            msgBuffer[16] = 0x04;

            if(CMD_RECORD_TIME_VALUE==0)
            {
                System.out.println("开始设置请求参数1-->sendCmd");
                msgBuffer[17] = 0x0C;
                msgBuffer[18] = 0x00;
                msgBuffer[19] = 0x05;
                msgBuffer[20] = 0x0D;
                msgBuffer[21] = 0x00;
                msgBuffer[22] = 0x00;
                msgBuffer[23] = 0x00;
                msgBuffer[24] = 0x00;
                msgBuffer[25] = 0x00;
                msgBuffer[26] = 0x00;
                msgBuffer[27] = 0x00;
                msgBuffer[28] = 0x05;
            }
            msgBuffer[29] = 0x1D;
        }
        else if(cmd==CMD_REQUES_RECORD)
        {
            System.out.println("开始设接收数据-->sendCmd");
            msgBuffer= new byte[22];
                msgBuffer[0] = 0x1A;
                msgBuffer[1] = 0x00;
                msgBuffer[2] = 0x00;
                msgBuffer[3] = 0x00;
                msgBuffer[4] = 0x00;
                msgBuffer[5] = 0x00;
                msgBuffer[6] = 0x00;
                msgBuffer[7] = 0x00;
                msgBuffer[8] = 0x01;
                msgBuffer[9] = 0x01;
                msgBuffer[10] = 0x00;
                msgBuffer[11] = 0x00;
                msgBuffer[12] = 0x0B;
                msgBuffer[13] = 0x00;
                msgBuffer[14] = 0x00;
                msgBuffer[15] = 0x00;
                msgBuffer[16] = 0x00;
                msgBuffer[17] = 0x00;
                msgBuffer[18] = 0x08;
                msgBuffer[19] = 0x0C;
                msgBuffer[20] = 0x09;
                msgBuffer[21] = 0x1D;
        }
        try {
            outStream.write(msgBuffer, 0, msgBuffer.length);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSocket()
    {
        //IsLinkSenser="F";
        try {
            btSocket.close();
            bluetoothFlag = false;
        } catch (IOException e2) {
            System.out.println("连接没有建立，无法关闭套接字！"+e2);
        }
    }

    //蓝牙连接函数
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int tryLinkSenser()
    {
        int returnValue=0;

        System.out.println("正在尝试连接蓝牙设备，请稍后···· "+nowSenserAddress);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(nowSenserAddress);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            //btSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
        } catch (IOException e) {
            System.out.println("套接字创建失败！");
            bluetoothFlag = false;
        }
        System.out.println("成功连接蓝牙设备！");
        try {
            System.out.println("正在建立链接");

            mBluetoothAdapter.cancelDiscovery();
            btSocket.connect();

            System.out.println("连接成功建立，可以开始操控了!");
            bluetoothFlag = true;
            //returnValue=1;
            testComunnicationFlag=true;

        } catch (IOException e) {
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            try {
                btSocket.connect();
                System.out.println("连接成功建立，可以开始操控了!");
                bluetoothFlag = true;
                //returnValue=1;
                testComunnicationFlag=true;
            } catch (IOException e1) {
                e1.printStackTrace();
                System.out.println("WTF");
                System.out.println("sss "+e.toString());
                testComunnicationFlag=false;
                returnValue =2;
                closeSocket();
            }



        }
        if (bluetoothFlag) {
            try {
                inStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } //绑定读接口
            try {
                outStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            } //绑定写接口

            if(testComunnicationFlag==true) {
                returnValue=tryGetData(CMD_TRY_LINK);
                if(returnValue==2)
                    closeSocket();
            }
        }
        return returnValue;
    }

    public class MyLinkThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            //System.out.println("aa " + tryLinkSenser());

            sendBroadcastToActivity(CMD_TRYLINK_RETURN,tryLinkSenser());
        }
    }


    private  int getData()
    {
        int data = 0;
        try {
            data = inStream.read();
            System.out.print("("+data+") ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }



    private int tryGetData(int cmd) //请求实时数据
    {
        int returnValue=RETURN_VALUE_FAILED;
        System.out.println("in 求数据"+bluetoothFlag);
        if (bluetoothFlag) {

            System.out.print("SSen " + cmd);
            if(cmd==CMD_GET_DATA)
                sendCmd(2);//发送请求
            else if(cmd==CMD_TRY_LINK)
                sendCmd(1);//发送请求
            else if(cmd==CMD_SETTING_RECORD)
                sendCmd(CMD_SETTING_RECORD);
            else if(cmd==CMD_REQUES_RECORD)
                sendCmd(CMD_REQUES_RECORD);

            System.out.print("STS ");
            int i=0;
            int dataLenght=0;


            while (true)
            {
                //System.out.print("T");
                try {
                    if(inStream.available()>0)
                    {
                        int data = getData();

                        if(data==0x1A)
                           i=0;
                        else if(data==0x1D)//尾
                        {
                            returnValue= RETURN_VALUE_SUCCESSED;
                            break;
                        }
                        if(i==9)//命令字
                        {
                            int H1 = data;
                            int H2 = getData();
                            int L1 = getData();
                            int L2 = getData();
                            i+=3;
                            System.out.println("");
                            System.out.println(H1+" "+H2+" "+L1+" "+L2+" ");
                            int cmdText=(H1<<12|H2<<8|L1<<4|L2);

                            System.out.println("cmdText "+cmdText+" cmd "+cmd);
                            if(cmd==CMD_TRY_LINK) {
                                if (cmdText == 2)
                                    returnValue= RETURN_VALUE_SUCCESSED;
                                else
                                    returnValue= RETURN_VALUE_FAILED;
                                //break;
                            }
                            else if(cmd==CMD_SETTING_RECORD)
                            {
                                if (cmdText == 4106)
                                    returnValue= RETURN_VALUE_SUCCESSED;
                                else
                                    returnValue= RETURN_VALUE_FAILED;
                                //break;
                            }
                            else if(cmd==CMD_REQUES_RECORD)
                            {
                                if (cmdText == 4108)
                                    returnValue= RETURN_VALUE_SUCCESSED;
                                else
                                    returnValue= RETURN_VALUE_FAILED;
                                //break;
                            }
                        }
                        else if(i==13)//数据长度
                        {
                            int H1 = data;
                            int H2 = getData();
                            int L1 = getData();
                            int L2 = getData();
                            i+=3;
                            dataLenght=(H1<<12|H2<<8|L1<<4|L2);

                            if(cmd==CMD_REQUES_RECORD)
                                dataLenght=dataLenght*2;
                            System.out.println("DL "+dataLenght);
                        }
                        else if(i==17)//CRC
                        {
                            int H1 = data;
                            int H2 = getData();
                            int L1 = getData();
                            int L2 = getData();
                            i+=3;
                            System.out.println("CRC ");
                        }
                        else if(i==21)//数据
                        {
                            if (cmd==CMD_TRY_LINK)
                            {
                                stateBuffer = new int[4];
                                int H1,H2,L1,L2 ;
                                for(int j=0;j<stateBuffer.length;j++)
                                {
                                    if(j==0)
                                    {
                                        H1 = data;
                                        H2 = getData();
                                        L1 = getData();
                                        L2 = getData();
                                    }
                                    else {
                                        H1 = getData();
                                        H2 = getData();
                                        L1 = getData();
                                        L2 = getData();
                                    }
                                    stateBuffer[j]=(H1<<12|H2<<8|L1<<4|L2);
                                }
                            }
                            else if(cmd==CMD_REQUES_RECORD)
                            {
                                recordMsgBuffer=new int[dataLenght/4];
                                int H1,H2,L1,L2 ;
                                for(int j=0;j<recordMsgBuffer.length;j++)
                                {
                                    if(j==0)
                                    {
                                         H1 = data;
                                         H2 = getData();
                                         L1 = getData();
                                         L2 = getData();
                                    }
                                    else {
                                        H1 = getData();
                                        H2 = getData();
                                        L1 = getData();
                                        L2 = getData();
                                    }
                                    recordMsgBuffer[j]=(H1<<12|H2<<8|L1<<4|L2);
                                    System.out.print("[" + recordMsgBuffer[j]+"] ");
                                }
                                System.out.println();
                            }
                            else {
                                getData();
                                getData();
                                getData();
                                int H1=getData();
                                int H2 = getData();
                                int L1 = getData();
                                int L2 = getData();
                                i += 3;
                                nowValue = (H1 << 12 | H2 << 8 | L1 << 4 | L2);
                                System.out.println("value " + nowValue + " ");
                            }
                        }
                        i++;
                    }
                    else
                    {
                       // System.out.print("k");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return returnValue;
    }

    public class MyGetDataThread extends Thread{//取得事实数据线程
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            //System.out.println("aa " + tryLinkSenser());

            while (IsSettingRecordData==true || IsGettingRecordData==true) {
            }
                    IsGettingData=true;
                    try {
                        Thread.sleep(THREAD_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (tryGetData(CMD_GET_DATA) == RETURN_VALUE_SUCCESSED)
                        sendBroadcastToActivity(CMD_RETURN_DATA, nowValue);
                    else
                        sendBroadcastToActivity(CMD_FAILED_GET_DATA,0);
                    IsGettingData=false;
               // sendBroadcastToActivity(CMD_FAILED_GET_DATA,0);
        }
    }

    public class MySettingRecordThread extends Thread{//设置记录值线程
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();

            System.out.println("IsGettingData-->MySettingRecordThread "+IsGettingData);

            while (IsGettingData==true) {
            }
            IsSettingRecordData=true;
            if (tryGetData(CMD_SETTING_RECORD) == RETURN_VALUE_SUCCESSED)
               System.out.println("TTTS");
            else
                System.out.println("FFFS");
            IsSettingRecordData=false;
        }
    }


    public class MyRequestRecordThread extends Thread{     //请求数据线程
        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();

            System.out.println("IsGettingData-->MyRequestRecordThread " + IsGettingData);

            while (IsGettingData==true) {
            }
            IsGettingRecordData=true;
            if (tryGetData(CMD_REQUES_RECORD) == RETURN_VALUE_SUCCESSED) {
                System.out.println("TTTR");
                for(int j=0;j<recordMsgBuffer.length;j++)
                    System.out.print("<" + recordMsgBuffer[j]+"> ");
                System.out.println();
                sendBroadcastToActivity(CMD_RETURN_RECORD_DATA, 0);
            }
            else
                System.out.println("FFFR");
            IsGettingRecordData=false;
        }
    }


    //发送命令到LineColumn
    public void sendBroadcastToActivity(int cmd,int value){

        // System.out.println("showToast");
        Intent intent = new Intent();
        if(cmd==0)
        {
            intent.putExtra("cmd", CMD_TRYLINK_RETURN);
            Bundle stateB = new Bundle();
            stateB.putIntArray("state", stateBuffer);
            intent.putExtras(stateB);
//            intent.putExtra("state", value);
            intent.setAction("android.intent.action.ServiceToMain");
        }

        else if(cmd==CMD_RETURN_DATA)
        {
            intent.putExtra("cmd", CMD_RETURN_DATA);
            intent.putExtra("value", value);
            intent.setAction("android.intent.action.ServiceToLineChart");
        }
        else if(cmd==CMD_RETURN_RECORD_DATA)
        {
            intent.putExtra("cmd", CMD_RETURN_RECORD_DATA);
            Bundle b=new Bundle();
            b.putIntArray("buffer", recordMsgBuffer);
            intent.putExtras(b);
            intent.setAction("android.intent.action.ServiceToLineChart");
        }
        sendBroadcast(intent);
    }

    //接收Activity传送过来的命令
    private class CommandReceiver extends BroadcastReceiver {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("android.intent.action.cmd")){
                int cmd = intent.getIntExtra("cmd", -1);//获取Extra信息

                if(cmd==CMD_START_DISCOVERY)
                {
//                    mBluetoothAdapter=null;
//                    mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
                   if(btSocket!=null)
                    closeSocket();
//                    if(mBluetoothAdapter.isDiscovering())
//                        mBluetoothAdapter.cancelDiscovery();
                    mBluetoothAdapter.startDiscovery();
                }
                else if(cmd==CMD_STOP_DISCOVERY)
                {
                    if(mBluetoothAdapter.isDiscovering())
                        mBluetoothAdapter.cancelDiscovery();
                }
                else if(cmd==CMD_TRY_LINK)
                {
                    nowSenserAddress = intent.getStringExtra("address");
                    nowSenserName =  intent.getStringExtra("name");
                    new MyLinkThread().start();
                }
                else if(cmd==CMD_CLOSE_SOCKET)
                    closeSocket();

                else if(cmd==CMD_GET_DATA)
                {
                    System.out.println("GGeeetttt-->CommandReceiver");
                    new MyGetDataThread().start();
                }
                else if(cmd==CMD_SETTING_RECORD)
                {
                    System.out.println("开始设置请求时间-->CommandReceiver");
                    CMD_RECORD_TIME_VALUE = intent.getIntExtra("value", -1);//获取Extra信息
                    new MySettingRecordThread().start();
                }
                else if(cmd==CMD_REQUES_RECORD)
                {
                    System.out.println("开始取得数据-->CommandReceiver");
                    new MyRequestRecordThread().start();
                }
            }
        }
    }



    @Override
    public void onDestroy() {

        System.out.println("ddsssttt");
        super.onDestroy();
        //this.unregisterReceiver(cmdReceiver);//取消注册的CommandReceiver
//        threadFlag = false;
//        boolean retry = true;
//        while(retry){
//            try{
//                //myThread.join();
//                retry = false;
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
    }
}
