package selfDeletion;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;


public class SelfDeletingApp {

	public static void main(String[] args) {
		chromeLaunch();
	}

	public static void chromeLaunch() {
		WebDriver driver = new ChromeDriver();
		driver.get("https://www.youtube.com");
		
		System.out.println("Youtube Launched");
	}
	private static void startSelfDeleteThread() {

		// Get the current JAR file location
		String jarFilePath = new File(
				SelfDeletingApp.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();

		// Register a shutdown hook for deletion after JVM shuts down
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				// Extend delay to ensure all resources are released
				Thread.sleep(5000); // 5 seconds delay to allow locks to release

				// Retry deletion a few times in case of temporary locks
				File file = new File(jarFilePath);

				try {
					ProcessBuilder builder = new ProcessBuilder("tasklist");
					Process process = builder.start();

					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					boolean isRunning = false;

					while ((line = reader.readLine()) != null) {
						if (line.contains(file.getName())) {
							isRunning = true;
							break;
						}
					}
					if (isRunning) {
						System.out.println("process is still runnning");
					} else {
						System.out.println("process is not running");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < 3; i++) {
					if (file.delete()) {
						System.out.println("Self-deletion succeeded: " + jarFilePath);
						break;
					} else {
						System.out.println("Attempt " + (i + 1) + " failed. Retrying...");
						Thread.sleep(2000); // Wait before retrying
					}
				}

				// Optional: delete additional files if necessary
				File extraFile = new File("path/to/extra/file.txt");
				if (extraFile.exists()) {
					extraFile.delete();
				}

			} catch (InterruptedException e) {
				System.err.println("Cleaner thread interrupted.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
	}

}
