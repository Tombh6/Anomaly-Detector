package test;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TimeSeries {
    private List<String> properties = new ArrayList<String>();
    private ArrayList<ArrayList<Float>> values = new ArrayList<ArrayList<Float>>();
    private float threshold = 0.9f;

    public TimeSeries(String csvFileName) {
        Scanner file_scanner = null, value_scanner = null;

        try {
            file_scanner = new Scanner(
                    new BufferedReader(
                            new FileReader(csvFileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (file_scanner.hasNextLine()) {
            String line = file_scanner.nextLine();
            value_scanner = new Scanner(line);
            value_scanner.useDelimiter(",");

            // add properties
            while (value_scanner.hasNext() && !value_scanner.hasNextFloat()) {
                this.properties.add(value_scanner.next());
            }

            // add values
            if (value_scanner.hasNextFloat()) {
                ArrayList table_line = new ArrayList<Float>();

                while (value_scanner.hasNextFloat()) {
                    table_line.add(value_scanner.nextFloat());
                }

                this.values.add(table_line);
            }
        }

        value_scanner.close();
        file_scanner.close();
    }

    public List<String> getProperties() {
		return this.properties;
    }

    public String getPropertyAtIndex(int index) {
        return this.properties.get(index);
    }

    public ArrayList<ArrayList<Float>> getValues() {
		return this.values;
    }

    public int getNumValues() {
        return this.values.size();
    }

	public float[] getValuesOfProperty(String property) {
		ArrayList<Float> propertyValues = new ArrayList<Float>();
		int propertyIndex = this.properties.indexOf(property);
		this.values.forEach(v -> propertyValues.add(v.get(propertyIndex)));

        float retArray[] = new float[propertyValues.size()];

        for(int i = 0; i < propertyValues.size(); i++) {
            retArray[i] = propertyValues.get(i);
        }

        return retArray;
	}

    public float[] getValuesOfPropertyAtIndex(int propertyIndex) {
    	ArrayList<Float> propertyValues = new ArrayList<Float>();
    	this.values.forEach(v -> propertyValues.add(v.get(propertyIndex)));

    	float retArray[] = new float[propertyValues.size()];

    	for(int i = 0; i < propertyValues.size(); i++) {
    	    retArray[i] = propertyValues.get(i);
        }

    	return retArray;
	}

    public void addProperty(String property) {
    	this.properties.add(property);
	}

	public void removeProperty(String property) {
    	this.properties.removeIf(p -> p.equals(property));
	}

	public void addLine(ArrayList<Float> value) {
    	this.values.add(value);
	}

	public void removeLine(ArrayList<Float> value) {
		this.values.removeIf(v -> v.equals(value));
	}

	public void removeLineAtIndex(int index) {
		this.values.remove(index);
	}

	public void setValueAtLine(int lineIndex, int valueIndex, Float value) {
		this.values.get(lineIndex).set(valueIndex, value);
	}

    public float getThreshold() {
        return this.threshold;
    }

	public void setThreshold(float newThreshold) {
        this.threshold = newThreshold;
    }

}
