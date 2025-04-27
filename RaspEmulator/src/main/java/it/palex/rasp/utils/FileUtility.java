package it.palex.rasp.utils;

import java.io.File;

public class FileUtility {

	/**
	 * 
	 * @param path
	 * @return true if is a file
	 */
	public static boolean checkIfIsAFile(String path){
		if(path==null){
			throw new NullPointerException();
		}
		File file = new File(path);
        return file.isFile();
	}
	
	/**
	 * 
	 * @param path
	 * @param ext
	 * @return true if the name of file ends with extension false if file not exist or extension is invalid
	 * <br>Example: <b>ext:</b> .bat, .txt, bat, txt
	 */
	public static boolean checkIfIsAFileWithExtension(File file, String ext){
		if(file==null || ext==null){
			return false;
		}
		String extToCheck = ext.toUpperCase();
		if(ext.startsWith(".")) {
			extToCheck = ext.substring(1);
		}
		boolean isAFile=checkIfIsAFile(file);
		if(isAFile){
		      return file.getName().toUpperCase().endsWith("."+extToCheck);
		}
		return false;
	}
	
	/**
	 * 
	 * @param path
	 * @param ext
	 * @return true if the name of file ends with extension
	 * <br>Example: <b>ext:</b> .bat, .txt, bat, txt
	 */
	public static boolean checkFileExtension(File file, String ext){
		if(file==null || ext==null){
			return false;
		}
		String extToCheck = ext.toUpperCase();
		if(ext.startsWith(".")) {
			extToCheck = ext.substring(1);
		}
		
		return file.getName().toUpperCase().endsWith("."+extToCheck);
	}
	
	/**
	 * 
	 * @param path
	 * @return true if is a file
	 */
	public static boolean checkIfIsAFile(File file){
		if(file==null){
			return false;
		}

		return file.isFile();
	}
	
}
