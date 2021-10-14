package com.foxtrotfanatics.ftu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Encryptor
{
	public static final int GCM_TAG_LENGTH = 16;
	public static final int GCM_IV_LENGTH = 12;

	public static void main(String[] args)
	{
		System.out.println("Encryption Test");
		Scanner s = new Scanner(System.in);

		System.out.print("Passphrase for Encryptor: ");
		String phrase1 = s.nextLine();
		SecretKey key1 = generateKey(phrase1);

		System.out.print("Passphrase for Decryptor: ");
		String phrase2 = s.nextLine();
		SecretKey key2 = generateKey(phrase2);

		System.out.print("Number of Submissions: ");
		int attempts = s.nextInt();
		s.nextLine();

		ArrayList<String> normalText = new ArrayList<String>();
		ArrayList<byte[]> encryptedObjects = new ArrayList<byte[]>();
		ArrayList<String> returnedText = new ArrayList<String>();
		for (int x = 0; x < attempts; x++)
		{
			System.out.print("PlainText " + (x + 1) + ": ");
			normalText.add(s.nextLine());
		}

		for (int x = 0; x < attempts; x++)
		{
			encryptedObjects.add(encrypt(key1, normalText.get(x)));
			System.out.println("Encrypted Object " + (x + 1) + " :");
			for (byte i : encryptedObjects.get(x))
			{
				System.out.print(i);
			}
			System.out.println("\n/");
			try
			{
				returnedText.add(decrypt(key2, encryptedObjects.get(x)));
			}
			catch (BadPaddingException | IllegalBlockSizeException e)
			{
				System.out.println("Failed to Decypt \"" + normalText.get(x) + "\" properly");
				e.printStackTrace();
			}
		}
		int counter = 0;
		for (int x = 0; x < attempts; x++)
		{
			System.out.println("PlainText " + (x + 1) + ": " + normalText.get(x));
			System.out.println("ReturnedText " + (x + 1) + ": " + returnedText.get(x));
			if (normalText.get(x).equals(returnedText.get(x)))
			{
				counter++;
			}
		}
		if (counter == returnedText.size())
		{
			System.out.println("Encryption Method successful");
		}
		s.close();
	}

	public static SecretKey generateKey(String passphrase)
	{
		try
		{
			//Prepares Secret Key Factory
			//PBKDF2WithHmacSHA1 / Password based Key Derivation Function 2 with HmacSHA1 Signature
			SecretKeyFactory kgen = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			//Sets up a hashing method that takes a passphrase and a NONEMPTY SALT, which would do 65536 iterations of the algorithm, to generate a 256 bit key
			//NOTE: Most likely I will have to hard code in a salt, as it must be the same across Client and Server, and its not fair to ask the User for one since they already supplied a password
			KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), new byte[]
			{ (byte) 0xba, (byte) 0x8a, 0x0d, 0x45, 0x25, (byte) 0xad, (byte) 0xd0, 0x11, (byte) 0x98, (byte) 0xa8, 0x08, 0x00, 0x36, 0x1b, 0x11,
					0x03 }, 65536, 128);
			//finally generates a Key with PBKDF2
			SecretKey tmp = kgen.generateSecret(spec);
			//designates the Key to AES, for use in Encryption and Decryption
			return new SecretKeySpec(tmp.getEncoded(), "AES");
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new IllegalStateException(e.toString());
		}
	}

	public static byte[] encrypt(SecretKey skey, String plaintext)
	{
		/*
		 * Precond: skey is valid and GCM mode is available in the JRE;
		 * otherwise IllegalStateException will be thrown.
		 */
		try
		{
			//Set Up Initial Cipher
			byte[] ciphertext = null;
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			//Generates Random IV
			byte[] initVector = new byte[GCM_IV_LENGTH];
			(new SecureRandom()).nextBytes(initVector);
			//Creates necessary MetaData from IV
			GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
			//Initializes Cipher with Key and MetaData
			//ONLY GOOD for 1 encryption, Cipher must always be reinitalized with a new IV
			cipher.init(Cipher.ENCRYPT_MODE, skey, spec);
			//This conversion I guess if helpful for the Server > Client communication, but the reverse data flow will have to encrypt entire objects.
			byte[] encoded = plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			//Create array of size of what the cipher will create + the bytes needed for the Initialization Vector
			ciphertext = new byte[initVector.length + cipher.getOutputSize(encoded.length)];
			//Puts the Initialization Vector in at the beginning
			for (int i = 0; i < initVector.length; i++)
			{
				ciphertext[i] = initVector[i];
			}
			// Perform encryption, but places bytes in ciphertext just after the included IV
			cipher.doFinal(encoded, 0, encoded.length, ciphertext, initVector.length);
			//ready to submit across the socket
			return ciphertext;
		}
		catch (NoSuchPaddingException | InvalidAlgorithmParameterException | ShortBufferException | BadPaddingException | IllegalBlockSizeException
				| InvalidKeyException | NoSuchAlgorithmException e)
		{
			/* None of these exceptions should be possible if precond is met. */
			throw new IllegalStateException(e.toString());
		}
	}

	public static String decrypt(SecretKey skey, byte[] ciphertext)
			throws BadPaddingException, IllegalBlockSizeException /* these indicate corrupt or malicious ciphertext */
	/* Note that AEADBadTagException may be thrown in GCM mode; this is a subclass of BadPaddingException */
	{
		/*
		 * Precond: skey is valid and GCM mode is available in the JRE;
		 * otherwise IllegalStateException will be thrown.
		 */
		try
		{
			//Set up initial cipher
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			//Gets the IV of the sent sealed object
			byte[] initVector = Arrays.copyOfRange(ciphertext, 0, GCM_IV_LENGTH);
			//Converts the IV to the MetaData of the object
			GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
			//completes the initialization of the decipher
			//Again, only good for one decryption, must reinit with the initVector supplied in the first couple bytes
			cipher.init(Cipher.DECRYPT_MODE, skey, spec);
			//actually deciphers the Encrypted object, making sure not to reread the IV
			byte[] plaintext = cipher.doFinal(ciphertext, GCM_IV_LENGTH, ciphertext.length - GCM_IV_LENGTH);
			//I am going to have to figure out how to convert these arrays to objects or something
			return new String(plaintext);
		}
		catch (NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException e)
		{
			/* None of these exceptions should be possible if precond is met. */
			throw new IllegalStateException(e.toString());
		}
	}
}