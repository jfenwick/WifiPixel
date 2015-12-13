import com.cycling74.max.*;
import com.cycling74.jitter.*;
import java.net.*;
import java.io.IOException;
import java.io.*;
import java.util.*;

public class DmxOverUdp extends MaxObject {

	public int port = 6038;	 // default port number
	public String host = "127.0.0.1";	 // "localhost" - change this to the desired ip address
	public boolean wall = false;	// whether this is the wall that uses a pds-480ca
	private InetAddress servAddr;
	private int packSize = 536;
	byte[] buffer = new byte[packSize];
	private int headerSize = 21;
	private DatagramSocket sock = null;
	private static final String[] INLET_ASSIST = new String[] {"(list) list of bytes to send"};
	private static final String[] OUTLET_ASSIST = new String[] {"bang when packet sent"};
	JitterMatrix jm = new JitterMatrix();

	private void initSocket(){
		if ( sock == null ){
			try { sock = new DatagramSocket(); }
			catch(IOException e) {
				System.err.println(e);
			}
		}
	}
	public DmxOverUdp(Atom[] args) {
		declareIO(2, 1);
		createInfoOutlet(false);	
		setInletAssist(INLET_ASSIST);
		setOutletAssist(OUTLET_ASSIST);
		if(args.length>0) {
			if(args[0].isString())
				host = args[0].getString();
			else {
				error("UdpSendRaw: first argument should be IP address\n");
				host = "127.0.0.1";
			}
			if(args[1].isInt())
				port = args[1].getInt();	 // argument sets port number
			else {
				error("UdpSendRaw: second argument should be port number (int)\n--- setting to default port");
				port = 7777;
			}
			if(args[2].isString())
				if (args[2].getString().equals("wall"))
					wall = true;
			else
				wall = false;
		}
		// pds-480ca
		post("UdpSendRaw: sending to "+host+" on port "+port);	

	}

	public void inlet(int wallport) {
		buffer[16] = (byte)wallport;
	}
	//public void list(int[] input) {
	public void jit_matrix(String dmx) {
		jm.frommatrix(dmx);
		int dim[] = jm.getDim();
		int count = 0;
		int planecount = jm.getPlanecount();
		int offset[] = new int[]{0,0};
		//int row_r[] = new int[dim[0]*planecount];
		int row_r[] = new int[dim[0]];
		int row_g[] = new int[dim[0]];
		int row_b[] = new int[dim[0]];
		if (wall)
			headerSize = 24;
		//int[] header = {4, 1, 220, 74, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 255, 255, 255, 255, 0};
		buffer[0] = 0x04;
		buffer[1] = 0x01;
		buffer[2] = (byte)0xDC; 
		buffer[3] = 0x4A;
		buffer[4] = 0x01;
		buffer[7] = 0x01;
		if (wall) {
			buffer[6] = 0x08;
			buffer[21] = 0x02;
			buffer[22] = (byte)0xF0;
			buffer[23] = (byte)0xFF;
		}
		else {
			buffer[6] = 0x01;
			buffer[16] = (byte)0xFF;
			buffer[17] = (byte)0xFF;
			buffer[18] = (byte)0xFF;
			buffer[19] = (byte)0xFF;
		}
		initSocket();

		// fill byte buffer
		for (int i=0; i<dim[1]; i++) {
			offset[1] = i;
			jm.copyVectorToArrayPlanar(1, 0, offset, row_r, dim[0], 0);

			jm.copyVectorToArrayPlanar(2, 0, offset, row_g, dim[0], 0);
			jm.copyVectorToArrayPlanar(3, 0, offset, row_b, dim[0], 0);
			for (int j=0; j<dim[0]; j++) {
				buffer[headerSize+(j*3)] = (byte)row_r[j];
				buffer[headerSize+(j*3)+1] = (byte)row_g[j];
				buffer[headerSize+(j*3)+2] = (byte)row_b[j];
			}
		}

		/*
		try {
			OutputStream fi = new FileOutputStream(new File("/Users/artcoreface/Desktop/out.txt"));
			for (byte bh : header) {
				fi.write((int)bh);
			}
		} catch (Exception e) {
		}
		*/

		try {
			servAddr = InetAddress.getByName(host);

			// create UDP-Packet to send
			DatagramPacket packet = new DatagramPacket(buffer, packSize, servAddr, port);
			sock.send(packet);
			outletBang(0);	 // output bang when packet sent
			// sock.disconnect();
		} 
		catch(IOException e) {
			System.err.println(e);
		}	
	}

	// change port number
	public void port(int p) {
		port = p;
	}

	// change host address
	public void host(String s) {
		host = s;
	}
}
