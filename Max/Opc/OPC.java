import com.cycling74.max.*;
import com.cycling74.jitter.*;
import java.net.*;
import java.io.IOException;
import java.io.*;
import java.util.*;

public class OPC extends MaxObject {

	public int port = 7890;	 // default port number
	public String host = "127.0.0.1";	 // "localhost" - change this to the desired ip address
	public boolean wall = false;	// whether this is the wall that uses a pds-480ca
	private InetAddress servAddr;
	private int packSize = 536;
	byte[] buffer = new byte[packSize];
	private int headerSize = 4;
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
	public OPC(Atom[] args) {
		declareIO(2, 1);
		createInfoOutlet(false);	
		setInletAssist(INLET_ASSIST);
		setOutletAssist(OUTLET_ASSIST);
		if(args.length>0) {
			if(args[0].isString())
				host = args[0].getString();
			else {
				error("OPC: first argument should be IP address\n");
				host = "127.0.0.1";
			}
			if(args[1].isInt())
				port = args[1].getInt();	 // argument sets port number
			else {
				error("OPC: second argument should be port number (int)\n--- setting to default port");
				port = 7890;
			}
		}
		post("OPC: sending to "+host+" on port "+port);	

	}

	//public void inlet(int wallport) {
	//	buffer[16] = (byte)wallport;
	//}
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
		buffer[0] = (byte)0x00;
		buffer[1] = (byte)0x00;
		buffer[2] = (byte)(dataSize & 0xff); 
		buffer[3] = (byte)((dataSize >> 8) & 0xff);
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
