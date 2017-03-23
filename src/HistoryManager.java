import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.PriorityQueue;

public class HistoryManager {

	private static final String TANGIBLE_INTERNET_DB = "/Users/sebastianhunkeler/TangibleInternet.db";
	private static final int MAX_ELEMENTS = 3;
	private PriorityQueue<String> numbers;
	private SpeechPlayer speechPlayer;
	
	private long lastTimeHistoryRead = 0;
	private static final int HISTORY_MESSAGE_INTERVAL = 30000;

	public HistoryManager(SpeechPlayer speechPlayer) {
		this.speechPlayer = speechPlayer;
		numbers = new PriorityQueue<>();
		readData();
	}

	public PriorityQueue<String> getLastDialledNumbers() {
		return numbers;
	}
	
	public void addNumber(String number){
		numbers.add(number);
		if(numbers.size() > MAX_ELEMENTS){
			numbers.poll();
		}
		saveData();
	}

	private void saveData() {
		try {
			FileOutputStream fos = new FileOutputStream(TANGIBLE_INTERNET_DB);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(numbers);
			oos.close();
			fos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void readData() {
		PriorityQueue<String> deserializedNumbers = new PriorityQueue<String>();
		try {
			FileInputStream fis = new FileInputStream(TANGIBLE_INTERNET_DB);
			ObjectInputStream ois = new ObjectInputStream(fis);
			deserializedNumbers = (PriorityQueue<String>) ois.readObject();
			ois.close();
			fis.close();
			numbers = deserializedNumbers;
		} catch (IOException ioe) {
			System.out.println("History file not found.");
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return;
		}
	}
	
	public void readHistory(int delay){
		
		long currentTime = System.currentTimeMillis();
		if((currentTime > HISTORY_MESSAGE_INTERVAL) && (currentTime - lastTimeHistoryRead < HISTORY_MESSAGE_INTERVAL)) return;
		lastTimeHistoryRead  = currentTime;
		
		String content;
		
		if(numbers.isEmpty()) {
			content = "There are no recently dialed numbers.";
		} else {			
			StringBuffer historyContent = new StringBuffer();
			historyContent.append("The last dialed numbers are as follows:      . ");
			int counter = 1;
			for (String number : numbers) {
				historyContent.append(counter + ": " + formatNumberForSpeech(number) + ". ");
				++counter;
			}
			
			historyContent.append(".           . ");
			historyContent.append("Dial the number that you want to call again.");
			
			content = historyContent.toString();
		}
		
		speechPlayer.say(content, "Alex" , delay);
	}

	private String formatNumberForSpeech(String number) {
		StringBuffer output = new StringBuffer();
		output.append(number.substring(0, 3));
		String delimiter = ".";
		output.append(delimiter);
		output.append(number.substring(3, 6));
		output.append(delimiter);
		output.append(number.substring(6, 9));
		output.append(delimiter);
		output.append(number.substring(9));
		return output.toString();
	}
}
