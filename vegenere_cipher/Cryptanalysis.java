import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.PrintWriter;

public class Cryptanalysis{

    private static final double ENGLISH_INDEX =  0.065;

        private static double frequencySquareSum(double[] a, double lettres){
            double sumF = 0;
            for (double f : a)
                sumF+= Math.pow((f/lettres), 2);
            return sumF;
        }

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static String decrypt(String text, String key){
            StringBuilder result = new StringBuilder(text);
            int j = 0;
            for (int i = 0; i < result.length(); i++) {
                int c = (int)result.charAt(i);
                if(c > 96 && c < 123){
                    int d =  (int)key.charAt(j++%key.length());
                    result.setCharAt(i,(char)((c - d) >= 0 ? c - d + 97 : c - d + 123));
                }
            }
            return result.toString();
	}

	public static int getKeySize(String text, double tolerance){
                boolean found = false;
                int keySize = 1;
                text = text.replaceAll("[^A-Za-z]+", "");
                while (!found && keySize<text.length()){
                    keySize++;
                    double[] letterFrequency = new double [26];
                    int lettre = 0;
                    for (int i = 0 ; i < text.length() - 1; i += keySize){
                        letterFrequency [(int)text.charAt(i)%97]++;
                        lettre++;
                    }
                    double frequencySquareSum = frequencySquareSum(letterFrequency, lettre);
                    if(frequencySquareSum <= (ENGLISH_INDEX + ENGLISH_INDEX*tolerance) && frequencySquareSum >= (ENGLISH_INDEX - ENGLISH_INDEX*tolerance))
                        found = true;
                }
		return keySize;
	}

	public static String getKey(String text, int keySize){
		String result = "";
		//Fréquences théorique des lettres en anglais: f[0]=a, f[1]=b, etc.
		double[] f = new double[]{0.082,0.015,0.028,0.043,0.127,0.022,0.02,
			0.061,0.07,0.02,0.08,0.04,0.024,0.067,0.015,0.019,0.001,0.06,
			0.063,0.091,0.028,0.02,0.024,0.002,0.02,0.001};
                text = text.replaceAll("[^A-Za-z]+", "");
                for (int i = 0; i < keySize; i++) {
                    double[] letterFrequency = new double [26];
                    int lettre = 0;
                    for (int j = i; j < text.length(); j+= keySize) {
                        letterFrequency [(int)text.charAt(j)%97]++;
                        lettre++;
                    }
                    boolean found = false;
                    int key = -1;
                    while (!found && key < 26) {
                        key++;
                        double sumF = 0;
                        for (int k = 0; k < letterFrequency.length; k++)
                            sumF += (f[k])*(letterFrequency[(k+key)%26]/lettre);
                        if(sumF <= (ENGLISH_INDEX + ENGLISH_INDEX*0.1) && sumF >= (ENGLISH_INDEX - ENGLISH_INDEX*0.1))
                            found = true;
                    }
                    result += (char)(key+97);
                }
		return result;
	}

	public static void main(String args[]){
		String text = "";
		try{
			text += readFile(args[0], StandardCharsets.UTF_8); // changer ca pour le chemain de fichier
		}catch(IOException e) {
			System.out.println("Can't load file");
		}
		double tolerance = 0.12;
		int keySize = getKeySize(text, tolerance);
                System.out.println("KeySize = "+keySize);
		String key = getKey(text, keySize);
                System.out.println("Key = "+key);
                text = decrypt(text, key);
                System.out.println("PlainText = \n"+text);
		try (PrintWriter out = new PrintWriter("text.txt")) {
		    out.println("KeySize = "+keySize);
		    out.println("Key = "+key);
		    out.println("PlainText =");
		    out.println(text);
		}catch(IOException e) {
			System.out.println("Can't write file");
		}
	}


}
