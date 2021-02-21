package src.Benchmarks;

public class PixelBenchmark extends Benchmark {

	private long time_start;
	private long time_stop;
	private long time_total;
	private long total;
	private long counter;
	private boolean started = false;

	public void start() {
		if (!started)
			time_start = System.nanoTime();
			started = true;
	}

	public void stop() {
		time_stop = System.nanoTime();
		time_total = time_stop - time_start;
		total += time_total;
		counter++;
		started = false;
	}

	public double getResult() {
		return (double) time_total / 1_000_000_000;
	}

	public double getAvg() {
		long tmp = total / counter;
		double avg = (double) tmp / 1_000_000_000;
		return avg;
	}

	public double getTotal() {
		return (double) total / 1_000_000_000;
	}

}
