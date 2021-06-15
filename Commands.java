package test;

import java.io.*;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commands {

    // Default IO interface
    public interface DefaultIO {
        public String readText();

        public void write(String text);

        public float readVal();

        public void write(float val);

        // you may add default methods here
        default void upload(String uploaded_csv_file_name, String endString) {
            try {
                String line = "";
                PrintWriter fw = fw = new PrintWriter(new FileWriter(uploaded_csv_file_name));

                while (!(line = readText()).equals(endString)) {
                    fw.println(line);
                }

                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // the default IO to be used in all commands
    DefaultIO dio;

    public Commands(DefaultIO dio) {
        this.dio = dio;
    }

    // you may add other helper classes here


    // the shared state of all commands
    private class SharedState {
        // implement here whatever you need
        HashMap<String, Object> sharedState = new HashMap<String, Object>();

        public SharedState() {
            this.sharedState.put("threshold", 0.9f);
        }

        public String[] getKeys() {
            return (String[]) this.sharedState.keySet().toArray();
        }

        public Object[] getValues() {
            return this.sharedState.values().toArray();
        }

        public Object getValue(String key) {
            return this.sharedState.get(key);
        }

        public boolean checkKeyExists(String key) {
            return this.sharedState.containsKey(key);
        }

        public boolean checkValueExists(Object value) {
            return this.sharedState.containsValue(value);
        }

        public void setValue(String key, Object newValue) {
            this.sharedState.replace(key, newValue);
        }

        public void addEntry(String key, Object value) {
            this.sharedState.put(key, value);
        }

        public void removeEntry(String key) {
            this.sharedState.remove(key);
        }

        public int getSize() {
            return this.sharedState.size();
        }
    }

    private SharedState sharedState = new SharedState();


    // Command abstract class
    public abstract class Command {
        protected String description;

        public Command(String description) {
            this.description = description;
        }

        public abstract void execute();
    }

    // Command class for example:
    public class ExampleCommand extends Command {

        public ExampleCommand() {
            super("this is an example of command");
        }

        @Override
        public void execute() {
            dio.write(description);
        }
    }

    // implement here all other commands
    public class UploadCSV extends Command {
        public UploadCSV() {
            super("Uploads the csv file");
        }

        @Override
        public void execute() {
            String uploadedCsvFileNameTrain = "anomalyTrain.csv";
            String uploadedCsvFileNameTest = "anomalyTest.csv";
            String endString = "done";

            dio.write("Please upload your local train CSV file.\n");
            //System.out.print("Please upload your local train CSV file.\n");
            dio.upload(uploadedCsvFileNameTrain, endString);
            sharedState.addEntry("uploadedCsvFileNameTrain", uploadedCsvFileNameTrain);
            dio.write("Upload complete.\n");
            //System.out.print("Upload complete.\n");

            dio.write("Please upload your local test CSV file.\n");
            //System.out.print("Please upload your local test CSV file.\n");
            dio.upload(uploadedCsvFileNameTest, endString);
            sharedState.addEntry("uploadedCsvFileNameTest", uploadedCsvFileNameTest);
            dio.write("Upload complete.\n");
            //System.out.print("Upload complete.\n");
        }
    }

    public class Threshold extends Command {

        public Threshold() {
            super("Changes or shows the current correlation threshold");
        }

        @Override
        public void execute() {
            String threshold_key = "threshold";
            dio.write("The current correlation threshold is " + sharedState.getValue(threshold_key) + "\n");
            dio.write("Type a new threshold\n");

            //System.out.print("The current correlation threshold is " + sharedState.getValue(threshold_key) + "\n");
            //System.out.print("Type a new threshold\n");
            String threshold_input = dio.readText();

            if (!threshold_input.isEmpty()) {
                boolean ok = false;
                while (!ok && !threshold_input.isEmpty()) {
                    if (Float.parseFloat(threshold_input) > 0 && Float.parseFloat(threshold_input) < 1) {
                        sharedState.setValue(threshold_key, Float.parseFloat(threshold_input));
                        ok = true;
                    } else {
                        dio.write("please choose a value between 0 and 1.\n");
                        dio.write("The current correlation threshold is " + sharedState.getValue(threshold_key) + "\n");
                        dio.write("Type a new threshold or just press enter to exit without changing\n");

                        //System.out.print("please choose a value between 0 and 1.\n");
                        //System.out.print("The current correlation threshold is " + sharedState.getValue(threshold_key) + "\n");
                        //System.out.print("Type a new threshold or just press enter to exit without changing\n");
                        threshold_input = dio.readText();
                    }
                }
            }
        }
    }

    public class Algorithm extends Command {

        public Algorithm() {
            super("Runs the algorithm on the csv file");
        }

        @Override
        public void execute() {
            if (sharedState.checkKeyExists("uploadedCsvFileNameTrain") && sharedState.checkKeyExists("uploadedCsvFileNameTest")) {
                SimpleAnomalyDetector sad = new SimpleAnomalyDetector();
                TimeSeries tsTrain = new TimeSeries(sharedState.getValue("uploadedCsvFileNameTrain").toString());
                TimeSeries tsTest = new TimeSeries(sharedState.getValue("uploadedCsvFileNameTest").toString());
                tsTrain.setThreshold((float) sharedState.getValue("threshold"));
                tsTest.setThreshold((float) sharedState.getValue("threshold"));
                sharedState.addEntry("numTimesteps", tsTest.getNumValues());
                sad.learnNormal(tsTrain);
                String correlatedFeaturesKey = "correlatedFeatures";

                if (sharedState.checkKeyExists(correlatedFeaturesKey)) {
                    sharedState.setValue(correlatedFeaturesKey, sad.detect(tsTest));
                } else {
                    sharedState.addEntry(correlatedFeaturesKey, sad.detect(tsTest));
                }

                ArrayList<AnomalyReport> correlatedFeatures = (ArrayList<AnomalyReport>) sharedState.getValue(correlatedFeaturesKey);
                HashMap<String, ArrayList<Integer>> anomaliesRangesProduced = new HashMap<>();
                ArrayList<Integer> anomaliesValues = new ArrayList<>();
                String currentDescription = "";

                // delete the for loop if the below one is needed
                for (AnomalyReport correlatedFeature : correlatedFeatures) {
                    if (!currentDescription.equals(correlatedFeature.description)) {
                        // start of loop
                        if (currentDescription.equals("")) {
                            currentDescription = correlatedFeature.description;
                            anomaliesRangesProduced.put(currentDescription, null);
                            anomaliesValues = new ArrayList<>();
                        } else {
                            anomaliesRangesProduced.replace(currentDescription, anomaliesValues);
                            currentDescription = correlatedFeature.description;
                            anomaliesValues = new ArrayList<>();
                            anomaliesRangesProduced.put(currentDescription, null);
                        }
                    }

                    anomaliesValues.add((int) correlatedFeature.timeStep);
                }

                // manually add the last range because it will exit the loop before doing so
                anomaliesRangesProduced.put(currentDescription, anomaliesValues);
                sharedState.addEntry("anomaliesRangesProduced", anomaliesRangesProduced);

                dio.write("anomaly detection complete.\n");
                //System.out.print("anomaly detection complete.\n");


            } else {
                dio.write("you must upload a CSV file first.\n");
                //System.out.print("you must upload a CSV file first.\n");
            }
        }
    }

    public class PrintResults extends Command {

        public PrintResults() {
            super("Print algorithm results");
        }

        @Override
        public void execute() {
            String correlatedFeaturesKey = "correlatedFeatures";

            if (sharedState.checkKeyExists(correlatedFeaturesKey)) {
                ArrayList<AnomalyReport> correlatedFeatures = (ArrayList<AnomalyReport>) sharedState.getValue(correlatedFeaturesKey);

                // delete the for loop if the below one is needed
                for (AnomalyReport correlatedFeature : correlatedFeatures) {
                    dio.write(correlatedFeature.timeStep + "	" + correlatedFeature.description + "\n");
                    //System.out.print(correlatedFeature.timeStep + "	  " + correlatedFeature.description + "\n");
                }

                dio.write("Done.\n");
                //System.out.print("Done.\n");
            } else {
                dio.write("you must first apply the algorithm (option 3 in the main menu).");
                //System.out.print("you must first apply the algorithm (option 3 in the main menu).\n");
            }
        }
    }

    public class AnalyzeResults extends Command {

        public AnalyzeResults() {
            super("Uploads an anomalies file and analyzes its results");
        }

        @Override
        public void execute() {
            String line = "";
            String endString = "done";

            String correlatedFeaturesKey = "correlatedFeatures";
            ArrayList<AnomalyReport> correlatedFeatures = (ArrayList<AnomalyReport>) sharedState.getValue(correlatedFeaturesKey);

            String anomaliesRangesProducedKey = "anomaliesRangesProduced";
            HashMap<String, ArrayList<Integer>> anomaliesRangesProduced = (HashMap<String, ArrayList<Integer>>) sharedState.getValue(anomaliesRangesProducedKey);

            int P = 0;
            int N = (int) sharedState.getValue("numTimesteps");
            int FP = 0;
            int TP = 0;
            ArrayList<ArrayList<Integer>> anomaliesRangesGiven = new ArrayList<>();

            dio.write("Please upload your local anomalies file.\n");
            //System.out.print("Please upload your local anomalies file.\n");

            dio.write("Analyzing...\n");
            //System.out.print("Analyzing...\n");

            while (!(line = dio.readText()).equals(endString)) {
                int startTimestep = Integer.parseInt(line.split(",")[0]);
                int endTimestep = Integer.parseInt(line.split(",")[1]);
                ArrayList<Integer> range = new ArrayList<>();
                range.add(startTimestep);
                range.add(endTimestep);
                anomaliesRangesGiven.add(range);

                P++;
                N -= (endTimestep - startTimestep + 1);
            }

            for (Map.Entry mapElement : anomaliesRangesProduced.entrySet()) {
                ArrayList<Integer> timesteps = (ArrayList<Integer>) mapElement.getValue();

                boolean contains = false;
                for (Integer timestamp : timesteps) {
                    for (ArrayList<Integer> anomaliesRangeGiven : anomaliesRangesGiven) {
                        if (timestamp >= anomaliesRangeGiven.get(0) && timestamp <= anomaliesRangeGiven.get(1)) {
                            contains = true;
                        }
                    }
                }

                if (contains) {
                    TP++;
                } else {
                    FP++;
                }
            }

            float truePositiveRate = (float) TP / P;
            float falsePositiveRate = (float) FP / N;
            DecimalFormat df = new DecimalFormat("#.###");
            df.setRoundingMode(RoundingMode.DOWN);
            String truePositiveString;
            String falsePositiveString;

            if (truePositiveRate % 1 == 0) {
                truePositiveString = String.format("%.1f", truePositiveRate);
            } else {
                truePositiveString = df.format(truePositiveRate);
            }

            if (falsePositiveRate % 1 == 0) {
                falsePositiveString = String.format("%.1f", falsePositiveRate);
            } else {
                falsePositiveString = df.format(falsePositiveRate);
            }

            dio.write("True Positive Rate: " + truePositiveString + "\n");
            dio.write("False Positive Rate: " + falsePositiveString + "\n");
            //System.out.print("True Positive Rate: " + truePositiveString + "\n");
            //System.out.print("False Positive Rate: " + falsePositiveString + "\n");
        }
    }
}

