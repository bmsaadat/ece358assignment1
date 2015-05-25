package ece358assignment1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;

/*
 * Decryption Approach:
 * 
 * 1. Determine the key length by finding the period using the index of coincidence test.
 * 		- Starting with a period of 1, determine average I.C. of each period
 * 		- Find the anomalous I.C., which will be the key length
 * 2. Determine the exact key by using the Chi-squared statistic.
 * 		- Since we know the period / key size, we can just break the caesar cypher at each period
 * 		- Chi-squared statistic allows us to do this easier by determining how similiar two probability distributions are
 * 		
 */


public class VigenereCipher {
	// The english language frequencies of every letter as a percentage.
	private static final HashMap<Character, Double> englishFrequencies;
    static {
        HashMap<Character, Double> aMap = new HashMap<Character, Double>();
        aMap.put('a', 8.167);
        aMap.put('b', 1.492);
        aMap.put('c', 2.782);
        aMap.put('d', 4.253);
        aMap.put('e', 12.702);
        aMap.put('f', 2.228);
        aMap.put('g', 2.015);
        aMap.put('h', 6.094);
        aMap.put('i', 6.966);
        aMap.put('j', 0.153);
        aMap.put('k', 0.772);
        aMap.put('l', 4.025);
        aMap.put('m', 2.406);
        aMap.put('n', 6.749);
        aMap.put('o', 7.507);
        aMap.put('p', 1.929);
        aMap.put('q', 0.095);
        aMap.put('r', 5.987);
        aMap.put('s', 6.327);
        aMap.put('t', 9.056);
        aMap.put('u', 2.758);
        aMap.put('v', 0.978);
        aMap.put('w', 2.361);
        aMap.put('x', 0.150);
        aMap.put('y', 1.974);
        aMap.put('z', 0.074);

        englishFrequencies = aMap;
    }
	
	public static void main(String[] args) throws IOException {
		String cipherText = readCipherText("ciphertext");
		/*
		for (int i = 1; i < 25; i++) {
			double avg = averageIndexOfCoincidence(cipherText, i);
			System.out.println("Period: " + i + " Avg: " + avg);
		}	
		
		chiSquaredStatistic(cipherText, 7);*/
		
		
		System.out.println(cipherText);
		String decryptedString = decrypt(cipherText, "jqjsghc");
		
		
		String encryptedString = encrypt(decryptedString, "jqjsghc");
		System.out.println(encryptedString);
		
		
		// Output the deciphered text to a file
		PrintWriter writer = new PrintWriter("plaintext.txt", "UTF-8");
		writer.println(decryptedString);
		writer.close();
		
		outputCipherTextLetterFrequencies(cipherText, 7);
	}

	public static String readCipherText (String fileName) throws IOException {
		String filePath = new File("").getAbsolutePath();
		filePath = filePath + "/" + fileName;
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String text = "";
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        
	        text = sb.toString();
	    } catch (IOException e) {
			e.printStackTrace();
		} finally {
	        br.close();	        
	    }
	    return text;
	}
	
	public static double averageIndexOfCoincidence(String cipherText, int period) {
		double avgIndex = 0.0;
		
		// Splitting the larger cipherText into the different period sections
		for (int i = 0; i < period; i++) {
			// This hash table will contain the frequencies of each letter
	        HashMap<Character, Integer> frequencyTable = new HashMap<Character, Integer>();
	        // This count will keep track of how many letters in this period section
	        int textCount = 0;
	        // Iterate through this period section and fill the hash table
			for (int k = 0; k < cipherText.length(); k++) {
				int position = i + k * period;
				if (position > cipherText.length() - 1) break;
				textCount++;
				Character c = Character.valueOf(cipherText.charAt(position));
				if (c.charValue() == '\n') continue;
				if (frequencyTable.containsKey(c)) {
					Integer freq = frequencyTable.get(c);
					int freqValue = freq.intValue() + 1;
					frequencyTable.put(c, Integer.valueOf(freqValue));
				} else {
					frequencyTable.put(c, Integer.valueOf(1));
				}
			}
			// Determine the index of coincidence for this specific period section
			// To calculate the index of coincidence: sum{freq * (freq - 1)} / (textCount * (textCount - 1))
			int freqSum = 0;
			for (Character key : frequencyTable.keySet()) {
				int frequency = frequencyTable.get(key).intValue();
				freqSum += frequency * (frequency - 1);
			}
			avgIndex += (double)freqSum / (textCount * (textCount - 1));
		}
		
		avgIndex = avgIndex / period;
		
		return avgIndex;
	}
	
	public static void chiSquaredStatistic(String cipherText, int period) {
		// The chiSquared calculation is as follows: chiSquared = sum { (freq - expected)^2 / expected } 
		// Splitting the larger cipherText into the different period sections
		for (int i = 0; i < period; i++) {	
			System.out.println("--------------");
			System.out.println("Key Index: " + i);
			// Iterate through all the potential letters to determine what the letter is at this key index
			int currentMinLetter = 0;
			double currentMinChiSquared = Double.POSITIVE_INFINITY;
			for (int l = 0; l < 26; l++) {
				// This hash table will contain the frequencies of each letter
				HashMap<Character, Integer> frequencyTable = new HashMap<Character, Integer>();
				// This count will keep track of how many letters in this period
				// section
				int textCount = 0;
				// Iterate through this period section and fill the hash table
				for (int k = 0; k < cipherText.length(); k++) {
					int position = i + k * period;
					if (position > cipherText.length() - 1)
						break;
					textCount++;
					// Rotating the character with the correct potential letter that it could be
					char originalChar = cipherText.charAt(position);
					if (originalChar == '\n') continue;
					Character c = Character.valueOf(rotateCharacterLeft(originalChar, l));
					if (frequencyTable.containsKey(c)) {
						Integer freq = frequencyTable.get(c);
						int freqValue = freq.intValue() + 1;
						frequencyTable.put(c, Integer.valueOf(freqValue));
					} else {
						frequencyTable.put(c, Integer.valueOf(1));
					}
				}
				// Calculate chi-squared
				double chiSum = 0;
				for (Character key : frequencyTable.keySet()) {
					int frequency = frequencyTable.get(key).intValue();
					double expectedFrequency = textCount
							* (englishFrequencies.get(key).doubleValue() / 100);
					chiSum += Math.pow((frequency - expectedFrequency), 2)
							/ expectedFrequency;
				}
				currentMinChiSquared = Math.min(currentMinChiSquared, chiSum);
				if (currentMinChiSquared == chiSum) {
					currentMinLetter = l;
				}
				System.out.println("Potential Key: " + (char)('a' + l) + " Chi Squared: " + chiSum);
			}
			System.out.println("--------------");
			System.out.println("Minimum Chi-squared: " + currentMinChiSquared);
			System.out.println("Resulting Key letter: " + (char)('a' + currentMinLetter));
			System.out.println("--------------");
		}
	}

	public static void outputCipherTextLetterFrequencies(String cipherText,
			int period) {
		// Splitting the larger cipherText into the different period sections
		for (int i = 0; i < period; i++) {
			// This hash table will contain the frequencies of each letter
			HashMap<Character, Integer> frequencyTable = new HashMap<Character, Integer>();
			// This count will keep track of how many letters in this period
			// section
			// This count will keep track of how many letters in this period section
	        int textCount = 0;
			// Iterate through this period section and fill the hash table
			for (int k = 0; k < cipherText.length(); k++) {
				int position = i + k * period;
				if (position > cipherText.length() - 1)
					break;
				
				textCount++;
				Character c = Character.valueOf(cipherText.charAt(position));
				if (c.charValue() == '\n')
					continue;
				if (frequencyTable.containsKey(c)) {
					Integer freq = frequencyTable.get(c);
					int freqValue = freq.intValue() + 1;
					frequencyTable.put(c, Integer.valueOf(freqValue));
				} else {
					frequencyTable.put(c, Integer.valueOf(1));
				}
			}
			System.out.println();
			System.out.println("-------------");
			System.out.println("Period: " + i);
			for (Character key : frequencyTable.keySet()) {
				double frequency = (double)frequencyTable.get(key).intValue() / textCount;
				String formattedFrequency = String.format("Frequency: %.2f%%", frequency * 100);
				System.out.println("Letter: " + key + " " + formattedFrequency);
			}
			System.out.println("-------------");
			System.out.println();

		}
	}
	
	// Rotate left to decipher (right to cipher)
	public static char rotateCharacterLeft(char c, int amt) {
		char newChar = c;
		if ((c - amt) < 'a') {
			newChar = (char) ('z' + 1 - ('a' - (c - amt)));
		} else {
			newChar = (char) (c - amt);
		}
		return newChar;
	}
	
	// Rotate right to cipher
	public static char rotateCharacterRight(char c, int amt) {
		char newChar = c;
		if ((c + amt) > 'z') {
			newChar = (char) ('a' - 1 + (c + amt) - 'z');
		} else {
			newChar = (char) (c + amt);
		}
		return newChar;
	}
	
	public static String decrypt(String cipherText, String key) {
		String plainText = "";
		int currentKeyIndex = 0;
		for (int i = 0; i < cipherText.length(); i++) {
			char originalChar = cipherText.charAt(i);
			char rotatedChar = rotateCharacterLeft(originalChar, key.charAt(currentKeyIndex) - 'a');
			currentKeyIndex = (currentKeyIndex + 1) % key.length();
			plainText += rotatedChar;
		}
		return plainText;
	}
	
	public static String encrypt(String text, String key) {
		String cipherText = "";
		int currentKeyIndex = 0;
		for (int i = 0; i < text.length(); i++) {
			char originalChar = text.charAt(i);
			char rotatedChar = rotateCharacterRight(originalChar, key.charAt(currentKeyIndex) - 'a');
			currentKeyIndex = (currentKeyIndex + 1) % key.length();
			cipherText += rotatedChar;
		}
		return cipherText;
	}
}
