package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import test.Commands.Command;
import test.Commands.DefaultIO;

public class CLI {

	ArrayList<Command> commands;
	DefaultIO dio;
	Commands c;

	public CLI(DefaultIO dio) {
		this.dio=dio;
		c=new Commands(dio);
		commands=new ArrayList<>();
		// example: commands.add(c.new ExampleCommand());
		// implement
		commands.add(c.new UploadCSV());
		commands.add(c.new Threshold());
		commands.add(c.new Algorithm());
		commands.add(c.new PrintResults());
		commands.add(c.new AnalyzeResults());
	}

	public void start() {
		int option_selected = 0;

		// commands executor
		while(option_selected != 6) {
			dio.write("Welcome to the Anomaly Detection Server.\n");
			dio.write("Please choose an option:\n");
			dio.write("1. upload a time series csv file\n");
			dio.write("2. algorithm settings\n");
			dio.write("3. detect anomalies\n");
			dio.write("4. display results\n");
			dio.write("5. upload anomalies and analyze results\n");
			dio.write("6. exit\n");

			/*
			System.out.print("Welcome to the Anomaly Detection Server.\n");
			System.out.print("Please choose an option:\n");
			System.out.print("1. upload a time series csv file\n");
			System.out.print("2. algorithm settings\n");
			System.out.print("3. detect anomalies\n");
			System.out.print("4. display results\n");
			System.out.print("5. upload anomalies and analyze results\n");
			System.out.print("6. exit\n");
			 */

			option_selected = Integer.parseInt(dio.readText());

			switch (option_selected) {
				case 1:
					commands.get(0).execute();
					break;
				case 2:
					commands.get(1).execute();
					break;
				case 3:
					commands.get(2).execute();
					break;
				case 4:
					commands.get(3).execute();
					break;
				case 5:
					commands.get(4).execute();
					break;
				case 6:
				default:
					break;
			}
		}
	}
}
