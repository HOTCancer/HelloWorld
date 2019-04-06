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
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Scale_Activity extends Activity {
	
	TextView	WEIGHT_DISPLAY;
	Button		BTN_QUPI,BTN_BIAODING,BTN_DEBUG;
		
//	final String  TTY_DEV = "/dev/ttymxc4";
 	final String  TTY_DEV = "/dev/ttymxc3";
 	final int 	  bps = 115200;
	SerialPort  mSerialPort = null;		//�����豸����
	protected   OutputStream mOutputStream;		//�����������
	private     InputStream  mInputStream;
	Auto_Weight weight_thread = new Auto_Weight();	
	boolean     auto_start = true;
	int         search_interval = 800;	//��ʱʱ��
	
	int	 LDBD =	   0x02;  //���궨����
    int  RYDB =    0x03;  //�����궨����
    int  PYGL =    0x04;  //ƽ�ƹ�������
    int  ZCDQ =    0x05;  // �����������
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_uesr);

		find_id();
		key_things();
		init_data();
		weight_thread.start();
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
		BTN_QUPI.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//�������궨     aa 01 02 00 00 00 00 ae
		   		byte[] CMD_DATA = {(byte) 0xaa ,0x01 ,0x02 ,0x00 ,0x00 ,0x00 ,0x00 ,(byte) 0xae };
		   		print_String(CMD_DATA);
			}
		});
		
		BTN_BIAODING.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//���������궨     (�궨����25.�ֶ�ֵ1���� 0xaa 0x1 0x3 0x19 0x0 0xe8 0x3 0xb3
		   //		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0xe8, 0x3 ,(byte) 0xb3 };
				//�����궨(�궨����25.�ֶ�ֵ0.001���� 0xaa 0x1 0x3 0x19 0x0 0x1 0x0 0xc9  //25 0.001		
		   //		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0x1, 0x0 ,(byte) 0xc9 };

				//��1kgΪ��!��������
//		   		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x19, 0x0 ,(byte) 0x1, 0x0 ,(byte) 0xc9 };
//		   		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x01, 0x0 ,(byte) 0xe8, 0x3 ,(byte) 0x9b };
		   		byte[] CMD_DATA = {(byte) 0xaa ,0x1 ,0x3 ,0x01, 0x0 ,(byte) 0x01, 0x0 ,(byte) 0xB1 };
		   		
		   		print_String(CMD_DATA);	
			}
		});
		
		BTN_DEBUG.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//�������Խ���,�ر����Activity
			 	Intent intent_page_new = new Intent();
				intent_page_new.setClass(Scale_Activity.this, MainActivity.class);	 		
				startActivity(intent_page_new);			
				finish();
			}
		});
	}

	//��������
	protected void print_String(byte[] prt_code_buffer) {
		try {
			byte[] buffer = prt_code_buffer;
			mOutputStream.write(buffer);
		} catch (IOException e) {
		}		
	}
	
	private void find_id() {
		// TODO Auto-generated method stub
		WEIGHT_DISPLAY 	= 	(TextView) findViewById(R.id.date);
		BTN_QUPI		=	(Button) findViewById(R.id.qupi);
		BTN_BIAODING	=	(Button) findViewById(R.id.biaoding);
		BTN_DEBUG		=	(Button) findViewById(R.id.debug);
	}

	
	//���ݸ�ʽ����
	// ������Ϣ ��19���ֽ�
	// ����ͷ 1�ֽ�                   ��������CMD 1�ֽ�              �����궨��ʱADֵ  4�ֽ�      ʵʱADֵ 4�ֽ�     �����궨ʱ������ֵ����λKG�� 2�ֽ�         ת���ֶ�����ʵ��ֵ�� 4�ֽ�        ת���ֶ��������Ա��ʺ��ֵ��֧��3λС��ʱ�򣬱�����1000�� 2�ֽ�       У��ֵ 1�ֽ�     
	//    0xAA          0x22-> δ�궨0��
    //                  0x33-> δ�궨��һ��
    //                  0x55-> �����������
    //                  0x66-> 0��궨�ɹ�
    //                  0x88-> ��һ��궨�ɹ�

	//������Ϣ
	//����ͷ 2�ֽ�             ��������  1�ֽ�                               �궨����ֵ2�ֽ�         �궨�ֶ�ֵ�����㴫������1000�� 2�ֽ�          У��ֵ 1�ֽ�
	//0xAA  0x01      0x02-> ���궨����
    //				  0x03-> �����궨����
    //				  0x04-> ƽ�ƹ�������
	//				      ���� -> �����������
	
	//ʾ��
	//���궨��                                                                   0xaa 0x1 0x2 0x0  0x0 0x0  0x0 0xae   
	//�����궨(�궨����75.�ֶ�ֵ1����        0xaa 0x1 0x3 0x4b 0x0 0xe8 0x3 0xe5
	//�����궨(�궨����25.�ֶ�ֵ1����        0xaa 0x1 0x3 0x19 0x0 0xe8 0x3 0xb3  //25 1
	//�����궨(�궨����25.�ֶ�ֵ0.05����0xaa 0x1 0x3 0x19 0x0 0x32 0x0 0xfa  //25 0.05
	//�����궨(�궨����25.�ֶ�ֵ0.001��  0xaa 0x1 0x3 0x19 0x0 0x1  0x0 0xc9  //25 0.001
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
			byte[] receive_buf = new byte[20]; 			//��֯����
			boolean receive_state = false;

  			while(true)
        	{
        		if (auto_start) {
        			//����19�ֽ�����   
        			//ÿ�ν���һ���ֽ�,Ȼ��˦��ȥ!�ۼ�13���ֽ�,���һ���ַ���
        			byte[] buf = new byte[1]; 			//��֯����
					int size;
					try {
						size = mInputStream.read(buf);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//�����ȡ����0xAA,��ʼ��¼��������13���ֽ�,�������ַ���
					if (receive_state || buf[0]== -86 ) {
						receive_buf[buf_num] = (byte) (0xff&buf[0]);
						buf_num ++;
						receive_state = true;
						if (buf_num == 19) {
		        	        String receice_string = byte2HexStr(receive_buf);	        	        
		        //			System.out.println(receice_string);				        	        
		        	        //����ʵʱ�������㷨,
		        	        //����1   ʵʱADֵ
		        	        //����2   У���ADֵ
		        	        String my_num_string = null;

		        	        String get_weight = e_scale_Algorithm(
		        	        		Add_0(Integer.toHexString(receive_buf[9]& 0xFF))+
		        	        		Add_0(Integer.toHexString(receive_buf[8]& 0xFF))+
		        	        		Add_0(Integer.toHexString(receive_buf[7]& 0xFF))+
		        	        		Add_0(Integer.toHexString(receive_buf[6]& 0xFF)),

		        	        		Add_0(Integer.toHexString(receive_buf[5]& 0xFF))+
		        	        		Add_0(Integer.toHexString(receive_buf[4]& 0xFF))+
		        	        		Add_0(Integer.toHexString(receive_buf[3]& 0xFF))+
		        	        		Add_0(Integer.toHexString(receive_buf[2]& 0xFF)));
		   	        		Message msg = new Message();  
		   	        		
		                    //�ò����Ǵ��β����¿ؼ�  
		                    Bundle bundle = new Bundle();  
		                    msg.what = 0;  
		                    bundle.putString("get_weight", get_weight);  
		                    
		                    msg.setData(bundle);  
		                    //������Ϣ��Handler  
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
    
	private String Add_0(String String) {
		// TODO Auto-generated method stub
		String myString = String;
	    int my_num = Integer.parseInt(myString,16);
	    if (my_num < 16) {
	    	myString = "0"+ String;
		}
		return myString;
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
		                //ȡ���������¿ؼ�  
		            	WEIGHT_DISPLAY.setText(msg.getData().getString("get_weight"));          
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
			byte[] to_16_byte = hexStr2Bytes(to_16); 			//��֯����
			byte to_2_byte = to_16_byte[3];
    		return to_2_byte;
    	}else{
    		return buf;    		
    	}
	}

    /**  
     * bytes�ַ���ת��ΪByteֵ  
     * @param String src Byte�ַ�����ÿ��Byte֮��û�зָ���  
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
 * bytesת����ʮ�������ַ���  
 * @param byte[] b byte����  
 * @return String ÿ��Byteֵ֮��ո�ָ�  
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

	//����ʵʱ�������㷨
	//����1   ʵʱADֵ
	//����2   У���ADֵ
	public String e_scale_Algorithm(String string, String string2) {
		// TODO Auto-generated method stub
		double value = 0;
		String p = ""+0;
		try {
			double AD = Integer.parseInt(string,16);
			double kg_AD = Integer.parseInt(string2,16);
			
			value=(float) AD/kg_AD;//* 1000;
			DecimalFormat decimalFormat=new DecimalFormat(".000");//���췽�����ַ���ʽ�������С������2λ,����0����.
			p = decimalFormat.format(value);//format ���ص����ַ���		
		} catch (Exception e) {
			// TODO: handle exception
			DecimalFormat decimalFormat=new DecimalFormat(".0");//���췽�����ַ���ʽ�������С������2λ,����0����.
			p = decimalFormat.format(value);//format ���ص����ַ���
		}

		return  ""+p;
	}
}
