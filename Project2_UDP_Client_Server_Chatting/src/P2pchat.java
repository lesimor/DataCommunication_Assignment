import java.io.*;

import java.net.*;

import java.util.*;
import javax.swing.JOptionPane;

public class P2pchat
{

	public static void main(String[] args) throws Exception
	{	
		int PORT = 8080;
		Server s = new Server(PORT);
        s.start();
        
        
        DatagramSocket socket = new DatagramSocket();
        MessageReceiver r = new MessageReceiver(socket);
        MessageSender ms = new MessageSender(socket, "localhost", PORT);
        Thread rt = new Thread(r);
        Thread st = new Thread(ms);
        rt.start(); st.start();
	}  
}