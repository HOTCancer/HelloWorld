package com.longfly_example.e_scale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import casio.serial.SerialPort;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	TextView	  WEIGHT_DISPLAY;
	Button		  BTN_LINGDIAN,BTN_RENYIDIAN,BTN_RENYIDIAN_5;
	TextView	  STX,CMD,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,CRC,GET_WEIGHT;
	TextView	  TX_AD,TX_KG,TX_DATE;
	
	
// 	final String  TTY_DEV = "/dev/ttymxc4";
 	final String  TTY_DEV = "/dev/ttymxc3";
 	final int 	  bps = 115200;
	SerialPort  mSerialPort = null;		//串口设备描述
	protected   OutputStream mOutputStream;		//串口输出描述
	private     InputStream  mInputStream;
	Auto_Weight weight_thread = new Auto_Weight();	
	boolean     auto_start = true;
	int         search_interval = 800;	//延时时间
	
	int	 LDBD =	   0x02;  //零点标定命令
    int  RYDB =    0x03;  //任意点标定命令
    int  PYGL =    0x04;  //平移归零命令
    int  ZCDQ =    0x05;  // 正常数据输出
	SharedPreferences settings; 
	final String key = "key";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
 	    settings = getSharedPreferences("escale_SharedPreferences", 0);	

		find_id();
		key_things();
		init_data();
		weight_thread.start();
		BTN_RENYIDIAN_5.setVisibility(View.GONE);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		auto_start = false;
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		auto_start = true;
		super.onResume();
	}
	private void init_data() {
		// TODO Auto-generated method stub
		
	}

	private void key_things() {
		// TODO Auto-generated method stub
		BTN_LINGDIAN.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//发送零点标定     aa 01 02 00 00 00 00 ae
		   		byte[] CMD_DATA = {(byte) 0xaa ,0x01 ,0x02 ,0x00 ,0x00 ,0x00 ,0x00 ,(byte) 0xae };
		   		print_String(CMD_DATA);

			}
		});
		
		BTN_RENYIDIAN.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//发送任意点标定     (标定重量25.分度值1）： 0xaa 0x1 0x3 0x19 0x0 0xe8 0x3 0xb3
		   //		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0xe8, 0x3 ,(byte) 0xb3 };
				//任意点标定(标定重量25.分度值0.001）： 0xaa 0x1 0x3 0x19 0x0 0x1 0x0 0xc9  //25 0.001		
		   //		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0x1, 0x0 ,(byte) 0xc9 };

				//以1kg为例!进行设置
//		   		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0x1, 0x0 ,(byte) 0xc9 };
//		   		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x01, 0x0 ,(byte) 0xe8, 0x3 ,(byte) 0x9b };
		   		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x01, 0x0 ,(byte) 0x01, 0x0 ,(byte) 0xB1 };
		   		
		   		print_String(CMD_DATA);	
//		        SharedPreferences.Editor editor = settings.edit(); 
//		        editor.putInt(key, 1);
//		        editor.commit();    	//很重要！用于保存数据！不用commit保存是写不进文件的！   
			}
		});
		
		BTN_RENYIDIAN_5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//发送任意点标定     (标定重量25.分度值1）： 0xaa 0x1 0x3 0x19 0x0 0xe8 0x3 0xb3
		   //		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0xe8, 0x3 ,(byte) 0xb3 };
				//任意点标定(标定重量25.分度值0.001）： 0xaa 0x1 0x3 0x19 0x0 0x1 0x0 0xc9  //25 0.001		
		   //		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0x1, 0x0 ,(byte) 0xc9 };
	   		
		   		//以5kg为例!进行设置
		   		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x05, 0x0 ,(byte) 0xe8, 0x3 ,(byte) 0x9f };
		   		print_String(CMD_DATA);
//		        SharedPreferences.Editor editor = settings.edit(); 
//		        editor.putInt(key, 5);
//		        editor.commit();    	//很重要！用于保存数据！不用commit保存是写不进文件的！     

			}
		});
	}

	//发送命令
	protected void print_String(byte[] prt_code_buffer) {
		try {
			byte[] buffer = prt_code_buffer;
			mOutputStream.write(buffer);
		} catch (IOException e) {
		}		
	}
	
	private void find_id() {
		// TODO Auto-generated method stub
		WEIGHT_DISPLAY = (TextView) findViewById(R.id.weight);
		BTN_LINGDIAN	=	(Button) findViewById(R.id.btn_biaoling);
		BTN_RENYIDIAN	=	(Button) findViewById(R.id.btn_renyi);
		BTN_RENYIDIAN_5	=	(Button) findViewById(R.id.btn_renyi_5);
		STX	= (TextView) findViewById(R.id.get_weight_stx);
		CMD	= (TextView) findViewById(R.id.get_weight_cmd);
		DATA_1	= (TextView) findViewById(R.id.get_weight_data_1);
		DATA_2	= (TextView) findViewById(R.id.get_weight_data_2);
		DATA_3	= (TextView) findViewById(R.id.get_weight_data_3);
		DATA_4	= (TextView) findViewById(R.id.get_weight_data_4);
		DATA_5	= (TextView) findViewById(R.id.get_weight_data_5);
		CRC	= (TextView) findViewById(R.id.get_weight_data_crc);
		GET_WEIGHT = (TextView) findViewById(R.id.get_weight_data);
		
		TX_AD = (TextView) findViewById(R.id.get_ad);
		TX_KG = (TextView) findViewById(R.id.get_base);
//		TX_DATE= (TextView) findViewById(R.id.get_ad);
	}

	
	//数据格式解析
	// 接收消息 共19个字节
	// 数据头 1字节                   数据命令CMD 1字节              任意点标定的时AD值  4字节      实时AD值 4字节     任意点标定时的重量值（单位KG） 2字节         转换分度数（实际值） 4字节        转换分度数（乘以倍率后的值，支持3位小数时候，倍率是1000） 2字节       校验值 1字节     
	//    0xAA          0x22-> 未标定0点
    //                  0x33-> 未标定任一点
    //                  0x55-> 正常数据输出
    //                  0x66-> 0点标定成功
    //                  0x88-> 任一点标定成功

	//发送消息
	//数据头 2字节             数据命令  1字节                               标定重量值2字节         标定分度值，方便传输扩大1000倍 2字节          校验值 1字节
	//0xAA  0x01      0x02-> 零点标定命令
    //				  0x03-> 任意点标定命令
    //				  0x04-> 平移归零命令
	//				      其它 -> 正常数据输出
	
	//示例
	//零点标定：                                                                   0xaa 0x1 0x2 0x0  0x0 0x0  0x0 0xae   
	//任意点标定(标定重量75.分度值1）：        0xaa 0x1 0x3 0x4b 0x0 0xe8 0x3 0xe5
	//任意点标定(标定重量25.分度值1）：        0xaa 0x1 0x3 0x19 0x0 0xe8 0x3 0xb3  //25 1
	//任意点标定(标定重量25.分度值0.05）：0xaa 0x1 0x3 0x19 0x0 0x32 0x0 0xfa  //25 0.05
	//任意点标定(标定重量25.分度值0.001）  0xaa 0x1 0x3 0x19 0x0 0x1  0x0 0xc9  //25 0.001
    class Auto_Weight extends Thread{
        @Override
		public void run(){
  			try {
				mSerialPort = new SerialPort(new File(TTY_DEV), bps, 0);
    			mOutputStream = mSerialPort.getOutputStream();		
    			mInputStream  = mSerialPort.getInputStream(); 
  			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  			int buf_num = 0;
			byte[] receive_buf = new byte[20]; 			//组织接收
			boolean receive_state = false;

  			while(true)
        	{
        		if (auto_start) {
        			//发送19字节数据
        			/*	       			
        			byte[] buffer = {(byte) 0xAA,0x22,0x00,0x00,0x00,0x00,0x45,0x46,0x47,0x48,0x00,0x00,(byte) 0x88};
        			try {
						mOutputStream.write(buffer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			*/   
        			//每次接收一个字节,然后甩出去!累计13个字节,组成一个字符串
        			byte[] buf = new byte[1]; 			//组织接收
					int size;
					try {
						size = mInputStream.read(buf);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//如果获取到了0xAA,开始记录接下来的13个字节,最后组成字符串
//        			System.out.println(buf[0]+"");			
					if (receive_state || buf[0]== -86 ) {
//	        	        String clear_buffer = transter_byte(buf[0]);	//用于把字节转化成无符号型16进制					
						receive_buf[buf_num] = (byte) (0xff&buf[0]);
//						receive_buf[buf_num] = transter_byte(buf[0]);
						buf_num ++;
						receive_state = true;
						if (buf_num == 19) {
		        	        String receice_string = byte2HexStr(receive_buf);
		        	        
		        //			System.out.println(receice_string);		
		        	        
		        	        //计算实时重量的算法,
		        	        //参数1   实时AD值
		        	        //参数2   校验点AD值
		        	        String my_num_string = null;
		     //   	        int my_num = Integer.parseInt(Integer.toHexString(receive_buf[9]& 0xFF));
		     //  	        if (my_num < 16) {
		     //   	        	my_num_string = "0"+ Integer.parseInt(Integer.toHexString(receive_buf[9]& 0xFF));
			//				}
		        	        String get_weight = e_scale_Algorithm(
		        	        				Integer.toHexString(receive_buf[9]& 0xFF)+
		        	        				Integer.toHexString(receive_buf[8]& 0xFF)+
		        	        				Integer.toHexString(receive_buf[7]& 0xFF)+
		        	        				Integer.toHexString(receive_buf[6]& 0xFF),

		        	        				Integer.toHexString(receive_buf[5]& 0xFF)+
		        	        				Integer.toHexString(receive_buf[4]& 0xFF)+
		        	        				Integer.toHexString(receive_buf[3]& 0xFF)+
		        	        				Integer.toHexString(receive_buf[2]& 0xFF));
		   	        		Message msg = new Message();  
		   	        		
		                    //该部分是传参并更新控件  
		                    Bundle bundle = new Bundle();  
		                    msg.what = 0;  
		                    bundle.putString("weight_value", receice_string );  
		                    bundle.putString("stx", Integer.toHexString(receive_buf[0]& 0xFF) );  
		                    bundle.putString("cmd", Integer.toHexString(receive_buf[1]& 0xFF) );  
		                    bundle.putString("data_1", Integer.toHexString(receive_buf[2]& 0xFF)+" "+Integer.toHexString(receive_buf[3]& 0xFF)+" "+Integer.toHexString(receive_buf[4]& 0xFF)+" "+ Integer.toHexString(receive_buf[5]& 0xFF) );  
		                    bundle.putString("data_2", Integer.toHexString(receive_buf[6]& 0xFF)+" "+Integer.toHexString(receive_buf[7]& 0xFF)+" "+Integer.toHexString(receive_buf[8]& 0xFF)+" "+ Integer.toHexString(receive_buf[9]& 0xFF) ); 
		                    bundle.putString("data_3", Integer.toHexString(receive_buf[10]& 0xFF)+" "+Integer.toHexString(receive_buf[11]& 0xFF) ); 
		                    bundle.putString("data_4", Integer.toHexString(receive_buf[12]& 0xFF)+" "+Integer.toHexString(receive_buf[13]& 0xFF)+" "+Integer.toHexString(receive_buf[14]& 0xFF)+" "+ Integer.toHexString(receive_buf[15]& 0xFF) );    
		                    bundle.putString("data_5", Integer.toHexString(receive_buf[16]& 0xFF)+" "+Integer.toHexString(receive_buf[17]& 0xFF) );   
		                    bundle.putString("crc", Integer.toHexString(receive_buf[18]& 0xFF) );  
		                    bundle.putString("get_weight", get_weight);  
		                    
		                    msg.setData(bundle);  
		                    //发送消息到Handler  
		                    handler.sendMessage(msg); 
		                    
		                    buf_num =0;
		                    receive_state = false;
						} else {

						}						
					} else {

					}

				}
        	}
        }
    }
    
    public Handler handler = new Handler()  
    {  
        @Override  
        public void handleMessage(Message msg)  
        {  
            switch (msg.what)  
            {  
	            case 0:  
		            {  
		                //取出参数更新控件  
//		            	WEIGHT_DISPLAY.setText(msg.getData().getString("weight_value"));      
		            	STX.setText(   "数据头                                   :"+msg.getData().getString("stx"));      
		            	CMD.setText(   "数据命令                              :"+msg.getData().getString("cmd"));      
		            	DATA_1.setText("任意点标定的时AD值     :"+msg.getData().getString("data_1"));      
		            	DATA_2.setText("实时AD值                              :"+msg.getData().getString("data_2"));      
		            	DATA_3.setText("任意点标定时的重量值:"+msg.getData().getString("data_3"));      
		            	DATA_4.setText("转换分度数（实际值）:"+msg.getData().getString("data_4"));      
		            	DATA_5.setText("转换分度数                         :"+msg.getData().getString("data_5"));      
		            	CRC.setText(   "校验和                                   :"+msg.getData().getString("crc"));          
		            	GET_WEIGHT.setText("重量                :"+msg.getData().getString("get_weight")+" 克");          
		            }  
		            break; 
	            case 1:  
		            {  
		            	TX_AD.setText(" 实时AD值   :"+msg.getData().getString("get_AD"));  
		            	TX_KG.setText("1kg参照值   :"+msg.getData().getString("get_kg_AD"));  
		                //取出参数更新控件  
		            	WEIGHT_DISPLAY.setText("0.00");  
		            	
		            }  
		            break; 		            
	            default:  
	                break;  
            }  
            super.handleMessage(msg);  
        }   
    };

    
    public void wanglei_loop(int search_interval)
    {
		try {
			Thread.sleep(search_interval);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public byte transter_byte(byte buf) {
		// TODO Auto-generated method stub
    	String to_16 = Integer.toHexString(buf);
    	if(to_16.length()>1){
			byte[] to_16_byte = hexStr2Bytes(to_16); 			//组织接收
			byte to_2_byte = to_16_byte[3];
//			to_2_byte[1] = to_16_byte[7];
 //   		return byte2HexStr(to_2_byte);
    		return to_2_byte;
    	}else{
    		return buf;    		
    	}
	}

    /**  
     * bytes字符串转换为Byte值  
     * @param String src Byte字符串，每个Byte之间没有分隔符  
     * @return byte[]  
     */    
    public static byte[] hexStr2Bytes(String src)    
    {    
        int m=0,n=0;    
        int l=src.length()/2;    
        System.out.println(l);    
        byte[] ret = new byte[l];    
        for (int i = 0; i < l; i++)    
        {    
            m=i*2+1;    
            n=m+1;    
            ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));    
        }    
        return ret;    
    } 

    
/**  
 * bytes转换成十六进制字符串  
 * @param byte[] b byte数组  
 * @return String 每个Byte值之间空格分隔  
 */    
public static String byte2HexStr(byte[] b)    
{    
    String stmp="";    
    StringBuilder sb = new StringBuilder("");    
    for (int n=0;n<b.length;n++)    
    {    
        stmp = Integer.toHexString(b[n] & 0xFF);    
        sb.append((stmp.length()==1)? "0"+stmp : stmp);    
        sb.append(" ");    
    }    
    return sb.toString().toUpperCase().trim();    
} 

public int getWordCount(String s)
{
    int length = 0;
    for(int i = 0; i < s.length(); i++)
    {
        int ascii = Character.codePointAt(s, i);
        if(ascii >= 0 && ascii <=255)
            length++;
        else
            length += 2;
            
    }
    return length;
    
}

	//计算实时重量的算法
	//参数1   实时AD值
	//参数2   校验点AD值
	public String e_scale_Algorithm(String string, String string2) {
		// TODO Auto-generated method stub
		double value = 0;
		String p = ""+0;
		try {
			double AD = Integer.parseInt(string,16);
			double kg_AD = Integer.parseInt(string2,16);
//			int key_valye = 1;
	/*		try {
				key_valye = settings.getInt(key, 0);
			} catch (Exception e) {
				// TODO: handle exception
			}
	*/		
			
			value=(float) AD/kg_AD * 1000;
			DecimalFormat decimalFormat=new DecimalFormat(".");//构造方法的字符格式这里如果小数不足2位,会以0补足.
			p = decimalFormat.format(value);//format 返回的是字符串
//			float abc = Integer.parseInt(p);
//			int i = (int) abc;
//			p = ""+i;
			
	   		Message msg = new Message();  
	   		
	        //该部分是传参并更新控件  
	        Bundle bundle = new Bundle();  
	        msg.what = 1;   
	        bundle.putString("get_AD", string+"\\"+AD);  
	        bundle.putString("get_kg_AD", string2+"\\"+kg_AD);  
	        bundle.putString("get_value", ""+value);  
	        
	        msg.setData(bundle);  
	        //发送消息到Handler  
	        handler.sendMessage(msg); 		
		} catch (Exception e) {
			// TODO: handle exception
			DecimalFormat decimalFormat=new DecimalFormat(".0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
			p = decimalFormat.format(value);//format 返回的是字符串
			
		}


		return  ""+p;
	}
}
