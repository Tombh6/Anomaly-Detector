package test;


import test.Commands.DefaultIO;
import test.Server.ClientHandler;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class AnomalyDetectionHandler implements ClientHandler{
	@Override
	public void handleClient(InputStream inFromClient, OutputStream outToClient) {
		SocketIO socket = new SocketIO(inFromClient,outToClient);
		CLI cli = new CLI(socket);
		cli.start();
		socket.write("bye");
		try {
			socket.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class SocketIO implements DefaultIO{
		BufferedReader in;
		BufferedWriter out;

		public SocketIO(InputStream inFromClient, OutputStream outToClient) {
			in = new BufferedReader(new InputStreamReader(inFromClient));
			out = new BufferedWriter(new OutputStreamWriter(outToClient));
		}

		@Override
		public String readText() {
			try {
				return in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		public void write(String text) {
			try {
				out.write(text);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public float readVal() {
			try {
				return Float.parseFloat(in.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return 0;
		}

		@Override
		public void write(float val) {
			try {
				out.write(String.valueOf(val));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
