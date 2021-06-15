package test;

public class StatLib {

	// simple average
	public static float avg(float[] x){
		float sum = 0;

		for (int i = 0; i < x.length; i++) {
			sum += x[i];
		}

		return sum/x.length;
	}

	// returns the variance of X and Y
	public static float var(float[] x){
		// calculate the u var
		float sum = 0;

		for(int i = 0; i < x.length; i++) {
			sum += x[i];
		}

		float u = sum / x.length;

		// calculate the variance
		sum = 0;

		for(int i = 0; i < x.length; i++) {
			sum += Math.pow((x[i] - u), 2);
		}

		float variance = sum / x.length;
		return variance;
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y){
		// x and y lengths are the same so
		// it doesn't matter which length is chosen
		float[] mul_xy = new float[x.length];

		// averages calculation
		for(int i = 0; i < x.length; i++) {
			mul_xy[i] = x[i] * y[i];
		}

		float average_xy = avg(mul_xy);
		float average_x = avg(x);
		float average_y = avg(y);

		// covariance calculation
		float covariance = average_xy - average_x * average_y;
		return covariance;
	}


	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y){
		float covariance_xy = cov(x,y);
		float deviation_x = (float) Math.sqrt(var(x));
		float deviation_y = (float) Math.sqrt(var(y));

		return covariance_xy / (deviation_x * deviation_y);
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points){
		float[] x_values = new float[points.length];
		float[] y_values = new float[points.length];
		int i = 0;

		for (Point point : points) {
			x_values[i] = point.x;
			y_values[i] = point.y;
			i++;
		}

		float a = cov(x_values, y_values) / var(x_values);
		float b = avg(y_values) - a * avg(x_values);

		Line line = new Line(a,b);
		return line;
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points){
		Line line = linear_reg(points);
		return Math.abs(line.f(p.x) - p.y);
	}

	// returns the deviation between point p and the line
	public static float dev(Point p,Line l){
		return Math.abs(l.f(p.x) - p.y);
	}
	
}
