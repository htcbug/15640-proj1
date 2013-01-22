package ProcessMigration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessManager {

	public void run() throws IOException {
		String curLine = ""; // Line read from standard in
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		while (!(curLine.equals("quit"))) {

			curLine = in.readLine();
			
			if (!(curLine.equals("quit"))) {
				System.out.println("You typed: " + curLine);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ProcessManager manager = new ProcessManager();
		try {
			manager.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
