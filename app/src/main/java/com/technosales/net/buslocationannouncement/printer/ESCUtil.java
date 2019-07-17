package com.technosales.net.buslocationannouncement.printer;

import java.io.ByteArrayOutputStream;

//常用指令封装
public class ESCUtil {

	public static final byte ESC = 0x1B;// Escape
	public static final byte FS =  0x1C;// Text delimiter
	public static final byte GS =  0x1D;// Group separator
	public static final byte DLE = 0x10;// data link escape
	public static final byte EOT = 0x04;// End of transmission
	public static final byte ENQ = 0x05;// Enquiry character
	public static final byte SP =  0x20;// Spaces
	public static final byte HT =  0x09;// Horizontal list
	public static final byte LF =  0x0A;//Print and wrap (horizontal orientation)
	public static final byte CR =  0x0D;// Home key
	public static final byte FF =  0x0C;// Carriage control (print and return to the standard mode (in page mode))
	public static final byte CAN = 0x18;// Canceled (cancel print data in page mode)

	//初始化打印机
	public static byte[] init_printer() {
		byte[] result = new byte[2];
		result[0] = ESC;
		result[1] = 0x40;
		return result;
	}

	//打印浓度指令
	public static byte[] setPrinterDarkness(int value){
		byte[] result = new byte[9];
		result[0] = GS;
		result[1] = 40;
		result[2] = 69;
		result[3] = 4;
		result[4] = 0;
		result[5] = 5;
		result[6] = 5;
		result[7] = (byte) (value >> 8);
		result[8] = (byte) value;
		return result;
	}

	/**
	 * 打印单个二维码 sunmi自定义指令
	 * @param code:			二维码数据
	 * @param modulesize:	二维码块大小(单位:点, 取值 1 至 16 )
	 * @param errorlevel:	二维码纠错等级(0 至 3)
	 *                0 -- 纠错级别L ( 7%)
	 *                1 -- 纠错级别M (15%)
	 *                2 -- 纠错级别Q (25%)
	 *                3 -- 纠错级别H (30%)
	 */
	public static byte[] getPrintQRCode(String code, int modulesize, int errorlevel){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(setQRCodeSize(modulesize));
			buffer.write(setQRCodeErrorLevel(errorlevel));
			buffer.write(getQCodeBytes(code));
			buffer.write(getBytesForPrintQRCode(true));
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}

	public static byte[] underlineWithOneDotWidthOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 1;
		return result;
	}

	//取消下划线
	public static byte[] underlineOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 0;
		return result;
	}

	// ------------------------bold-----------------------------
	/**
	 * 字体加粗
	 */
	public static byte[] boldOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0xF;
		return result;
	}

	/**
	 * 取消字体加粗
	 */
	public static byte[] boldOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0;
		return result;
	}

	// ------------------------character-----------------------------
	/*
	*单字节模式开启
	 */
	public static byte[] singleByte(){
		byte[] result = new byte[2];
		result[0] = FS;
		result[1] = 0x2E;
		return result;
	}

	/*
	*单字节模式关闭
 	*/
	public static byte[] singleByteOff(){
		byte[] result = new byte[2];
		result[0] = FS;
		result[1] = 0x26;
		return result;
	}

	/**
	 * 设置单字节字符集
	 */
	public static byte[] setCodeSystemSingle(byte charset){
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 0x74;
		result[2] = charset;
		return result;
	}

	/**
	 * 设置多字节字符集
	 */
	public static byte[] setCodeSystem(byte charset){
		byte[] result = new byte[3];
		result[0] = FS;
		result[1] = 0x43;
		result[2] = charset;
		return result;
	}

	// ------------------------Align-----------------------------

	/**
	 * 居左
	 */
	public static byte[] alignLeft() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 0;
		return result;
	}

	/**
	 * 居中对齐
	 */
	public static byte[] alignCenter() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 1;
		return result;
	}

	/**
	 * 居右
	 */
	public static byte[] alignRight() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 2;
		return result;
	}

	//切刀
	public static byte[] cutter() {
		byte[] data = new byte[]{0x1d, 0x56, 0x01};
		return data;
	}

	//走纸到黑标
	public static byte[] gogogo(){
		byte[] data = new byte[]{0x1C, 0x28, 0x4C, 0x02, 0x00, 0x42, 0x31};
		return data;
	}


	////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////          private                /////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	private static byte[] setQRCodeSize(int modulesize){
		//二维码块大小设置指令
		byte[] dtmp = new byte[8];
		dtmp[0] = GS;
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x43;
		dtmp[7] = (byte)modulesize;
		return dtmp;
	}

	private static byte[] setQRCodeErrorLevel(int errorlevel){
		//二维码纠错等级设置指令
		byte[] dtmp = new byte[8];
		dtmp[0] = GS;
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x45;
		dtmp[7] = (byte)(48+errorlevel);
		return dtmp;
	}


	private static byte[] getBytesForPrintQRCode(boolean single){
		//打印已存入数据的二维码
		byte[] dtmp;
		if(single){		//同一行只打印一个QRCode， 后面加换行
			dtmp = new byte[9];
			dtmp[8] = 0x0A;
		}else{
			dtmp = new byte[8];
		}
		dtmp[0] = 0x1D;
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x51;
		dtmp[7] = 0x30;
		return dtmp;
	}

	private static byte[] getQCodeBytes(String code) {
		//二维码存入指令
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			byte[] d = code.getBytes("GB18030");
			int len = d.length + 3;
			if (len > 7092) len = 7092;
			buffer.write((byte) 0x1D);
			buffer.write((byte) 0x28);
			buffer.write((byte) 0x6B);
			buffer.write((byte) len);
			buffer.write((byte) (len >> 8));
			buffer.write((byte) 0x31);
			buffer.write((byte) 0x50);
			buffer.write((byte) 0x30);
			for (int i = 0; i < d.length && i < len; i++) {
				buffer.write(d[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
}