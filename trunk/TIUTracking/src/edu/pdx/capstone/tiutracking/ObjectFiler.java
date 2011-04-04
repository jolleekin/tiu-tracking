package edu.pdx.capstone.tiutracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This utility class provides two useful methods for writing an object to or
 * reading an object from a file.
 * 
 * @author Kin
 * 
 */
public class ObjectFiler {

	/**
	 * Reads an object from a file
	 * 
	 * @param fileName
	 *            Name of the file to be read
	 * @return The object read from the file or <b>null</b> if the file does not
	 *         exist.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object readObjectFromFile(String fileName)
			throws IOException, ClassNotFoundException {

		File f = new File(fileName);
		if (f.exists()) {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
			Object result = in.readObject();
			in.close();
			return result;
		}
		return null;
	}

	/**
	 * Writes an object to a file
	 * 
	 * @param fileName
	 *            Name of the file to be written
	 * @param obj
	 *            The object to be written
	 * @throws IOException
	 */
	public static void writeObjectToFile(String fileName, Object obj)
			throws IOException {

		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				fileName));
		out.writeObject(obj);
		out.close();
	}

}
