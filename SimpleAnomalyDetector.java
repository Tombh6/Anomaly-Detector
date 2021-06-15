package test;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {
	private ArrayList<CorrelatedFeatures> correlatedFeatures = new ArrayList<CorrelatedFeatures>();

	@Override
	public void learnNormal(TimeSeries ts) {
		float threshold = ts.getThreshold();
		int numProperties = ts.getProperties().size();

		for(int i = 0; i < numProperties; i++) {
			String maxProperty = "";
			float maxPropertyPearsonValue = 0;
			float[] values1 = ts.getValuesOfPropertyAtIndex(i);
			Point[] points = new Point[values1.length];

			for(int j = i + 1; j < numProperties; j++) {
				float[] values2 = ts.getValuesOfPropertyAtIndex(j);
				float pearsonResult = StatLib.pearson(values1, values2);

				if(pearsonResult > threshold && pearsonResult > maxPropertyPearsonValue) {
					maxPropertyPearsonValue = pearsonResult;
					maxProperty = ts.getPropertyAtIndex(j);

					for(int k = 0; k < ts.getNumValues(); k++) {
						points[k] = new Point(values1[k],values2[k]);
					}
				}
			}

			if(maxProperty != "") {
				Line line = StatLib.linear_reg(points);

				float max_deviation = 0;
				for(Point p: points) {
					float dev_result = StatLib.dev(p, line);

					if(dev_result > max_deviation) {
						max_deviation = dev_result;
					}
				}

				CorrelatedFeatures cf = new CorrelatedFeatures(ts.getPropertyAtIndex(i), maxProperty, maxPropertyPearsonValue, line, max_deviation * 1.1f);
				this.correlatedFeatures.add(cf);
			}
		}
	}


	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		ArrayList<AnomalyReport> anomalyReports = new ArrayList<AnomalyReport>();

		int numLines = ts.getNumValues();

		for(CorrelatedFeatures cf: this.correlatedFeatures) {
			float[] feature1_values = ts.getValuesOfProperty(cf.feature1);
			float[] feature2_values = ts.getValuesOfProperty(cf.feature2);

			for (int i = 0; i < numLines; i++) {
				Point point = new Point(feature1_values[i], feature2_values[i]);
				float point_deviation = StatLib.dev(point,cf.lin_reg);

				if(point_deviation > cf.threshold) {
					String description = cf.feature1 + "-" + cf.feature2;
					anomalyReports.add(new AnomalyReport(description, i+1));
				}
			}
		}

		return anomalyReports;
	}
	
	public List<CorrelatedFeatures> getNormalModel(){
		return this.correlatedFeatures;
	}
}
